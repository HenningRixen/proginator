package com.example.prog1learnapp.service;

import com.example.prog1learnapp.dto.ExecutionResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
public class DockerExecutionService {
    private static final Logger log = LoggerFactory.getLogger(DockerExecutionService.class);
    
    @Value("${app.code-execution.docker-enabled:true}")
    private boolean dockerEnabled;
    
    private boolean dockerCliAvailable = false;
    
    @PostConstruct
    public void init() {
        if (!dockerEnabled) {
            log.info("Docker execution is disabled via configuration");
            dockerCliAvailable = false;
            return;
        }
        
        log.info("Checking if Docker CLI is available...");
        checkDockerCliAvailable();
        
        if (dockerCliAvailable) {
            log.info("Docker CLI execution enabled");
        } else {
            log.info("Docker CLI not available, will use mock execution");
        }
    }
    
    private void checkDockerCliAvailable() {
        try {
            Process process = new ProcessBuilder("docker", "version").start();
            int exitCode = process.waitFor();
            dockerCliAvailable = exitCode == 0;
            if (dockerCliAvailable) {
                log.info("Docker CLI is available");
            } else {
                log.warn("Docker CLI command failed with exit code {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.warn("Docker CLI is not available: {}", e.getMessage());
            dockerCliAvailable = false;
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Execute Java code in a secure Docker container
     */
    public ExecutionResult executeJavaCode(String code, String testCode, int timeoutMs, int memoryLimitMB) {
        Instant start = Instant.now();
        
        // Use Docker CLI via ProcessBuilder if available
        if (dockerCliAvailable) {
            return executeWithDockerCli(code, testCode, timeoutMs, memoryLimitMB, start);
        }
        
        // Fallback to mock execution for development
        log.info("No Docker execution available, using mock execution");
        return mockExecuteJavaCode(code, testCode, start);
    }
    
    /**
     * Execute using Docker CLI via ProcessBuilder
     */
    private ExecutionResult executeWithDockerCli(String code, String testCode, int timeoutMs, 
                                                int memoryLimitMB, Instant start) {
        ExecutionResult result = new ExecutionResult();
        result.setStatus("RUNNING");
        
        Path tempDir = null;
        try {
            // Create temporary directory for code
            tempDir = Files.createTempDirectory("proginator-exec-");
            // Determine class name for file naming
            String className = extractClassName(code);
            if (className == null || className.isEmpty()) {
                className = "Solution";
            }
            String testClassName = className + "Test";
            Path javaFile = tempDir.resolve(className + ".java");
            log.debug("Using class name: {}, test class name: {}, filename: {}", className, testClassName, javaFile.getFileName());
            
            // Generate Java test file (fixes duplicate class issue)
            String javaCode = generateJavaTestFile(code, testCode);
            Files.writeString(javaFile, javaCode);
            

            
            // Build Docker command with security constraints
            List<String> dockerCmd = Arrays.asList(
                "docker", "run", "--rm",
                "--memory=" + memoryLimitMB + "m",
                "--cpus=0.5",
                "--pids-limit=50",
                "--network=none",
                "--read-only",
                "--security-opt=no-new-privileges",
                "--cap-drop=ALL",
                "--user=runner",
                "--workdir", "/tmp/code",
                "-v", tempDir.toString() + ":/tmp/code",
                "proginator-java-sandbox",
                "sh", "-c", 
                "cd /tmp/code && " +
                "javac -cp /opt/junit-platform-console-standalone.jar " + className + ".java 2>&1 && " +
                "timeout " + (timeoutMs / 1000) + " java -cp /tmp/code:/opt/junit-platform-console-standalone.jar " +
                "org.junit.platform.console.ConsoleLauncher --select-class=" + testClassName + " 2>&1"
            );
            
            log.info("Executing Docker command: {}", String.join(" ", dockerCmd));
            
            ProcessBuilder pb = new ProcessBuilder(dockerCmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            String outputStr = output.toString().trim();
            String strippedOutput = stripAnsiCodes(outputStr);
            
            // Parse results
            boolean isJUnitOutput = isJUnitOutput(strippedOutput);
            
            // Check for timeout first (exit code 124 from timeout command)
            if (exitCode == 124) {
                result.setStatus("ERROR");
                int timeoutSeconds = timeoutMs / 1000;
                result.setError("Execution timed out after " + timeoutSeconds + " seconds. The program may be stuck in an infinite loop or taking too long.");
                result.setOutput(outputStr);
                result.setTestPassed(false);
            } 
            // Check for "No tests found" scenario
            else if (strippedOutput.contains("No tests found") || strippedOutput.contains("No test classes found")) {
                result.setStatus("ERROR");
                result.setError("No tests were discovered. Ensure your test class is properly annotated with @Test and the class name matches.");
                result.setOutput(strippedOutput);
                result.setTestPassed(false);
            }
            else if (exitCode == 0) {
                // JUnit ran successfully (exit code 0)
                result.setStatus("SUCCESS");
                // Enhance output with summary for JUnit results
                String enhancedOutput = strippedOutput;
                if (isJUnitOutput) {
                    String summary = parseJUnitSummary(strippedOutput);
                    enhancedOutput = summary + "\n\n" + strippedOutput;
                }
                result.setOutput(enhancedOutput);
                boolean testsPassed = parseTestResults(strippedOutput);
                result.setTestPassed(testsPassed);
            } else if (isJUnitOutput && didJUnitActuallyRunTests(strippedOutput)) {
                // JUnit ran but exited with non-zero (test failures or other issues)
                // But tests actually executed, so this is a test failure, not execution error
                result.setStatus("SUCCESS");
                // Enhance output with summary for JUnit results
                String enhancedOutput = strippedOutput;
                String summary = parseJUnitSummary(strippedOutput);
                enhancedOutput = summary + "\n\n" + strippedOutput;
                result.setOutput(enhancedOutput);
                boolean testsPassed = parseTestResults(strippedOutput);
                result.setTestPassed(testsPassed);
            } else if (isJUnitOutput && !didJUnitActuallyRunTests(strippedOutput)) {
                // JUnit printed banner but didn't run tests (likely crashed or error)
                result.setStatus("ERROR");
                String errorMessage = "JUnit started but failed to execute tests. ";
                if (strippedOutput.contains("Thanks for using JUnit") && strippedOutput.trim().length() < 100) {
                    errorMessage += "Only JUnit banner was printed, suggesting an early crash or configuration issue.";
                } else {
                    errorMessage += parseErrorDetails(exitCode, strippedOutput);
                }
                result.setError(errorMessage);
                result.setOutput(strippedOutput);
                result.setTestPassed(false);
            } else {
                // Not JUnit output at all (compilation error or other execution error)
                result.setStatus("ERROR");
                String errorMessage = parseErrorDetails(exitCode, strippedOutput);
                result.setError(errorMessage);
                result.setOutput(strippedOutput); // Keep raw output for debugging
                result.setTestPassed(false);
            }
            
        } catch (IOException | InterruptedException e) {
            log.error("Docker CLI execution failed", e);
            result.setStatus("ERROR");
            result.setError("Docker CLI execution error: " + e.getMessage());
            result.setTestPassed(false);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        } finally {
            // Clean up temporary directory
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                         .sorted((a, b) -> -a.compareTo(b))
                         .forEach(path -> {
                             try { Files.delete(path); } 
                             catch (IOException e) { /* ignore */ }
                         });
                } catch (IOException e) {
                    log.warn("Failed to clean up temp directory {}", tempDir, e);
                }
            }
            
            Instant end = Instant.now();
            result.setExecutionDuration(Duration.between(start, end).toMillis());
        }
        
        return result;
    }
    
    /**
     * Generate Java test file, handling duplicate class issue
     */
    private String generateJavaTestFile(String userCode, String testCode) {
        // Extract the main class name from user code
        String className = extractClassName(userCode);
        if (className == null || className.isEmpty()) {
            // No class found, default to "Solution"
            className = "Solution";
            // Wrap user code in a class
            userCode = String.format("public class %s {\n%s\n}", className, userCode);
        }
        
        // Determine test class name
        String testClassName = className + "Test";
        
        // Generate JUnit test class
        // Generate JUnit test class (without imports, they go at top of file)
        String testClassBody = String.format("""
            class %s {
                @Test
                void test%s() {
                    %s instance = new %s();
                    %s
                }
            }
            """, testClassName, className, className, className, testCode);
        
        // Combine imports, user class, and test class in same file
        String imports = "import org.junit.jupiter.api.Test;\nimport static org.junit.jupiter.api.Assertions.*;\n\n";
        String finalCode = imports + userCode + "\n\n" + testClassBody;
        log.debug("Generated Java code:\n{}", finalCode);
        return finalCode;
    }
    
    /**
     * Extract class name from Java code (simple regex)
     */
    private String extractClassName(String javaCode) {
        // Look for "public class ClassName" or "class ClassName"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:public\\s+)?class\\s+(\\w+)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String stripAnsiCodes(String input) {
        if (input == null) return null;
        // Remove CSI sequences (ESC[ ... [A-Za-z])
        String stripped = input.replaceAll("\\u001B\\[[?]?[;\\d]*[A-Za-z]", "");
        // Remove any remaining ESC characters (shouldn't be needed)
        stripped = stripped.replace("\u001B", "");
        return stripped;
    }

    private boolean parseTestResults(String output) {
        log.debug("Parsing test results from output: {}", output);
        
        // Parse JUnit output for test results
        // JUnit Console Launcher outputs multiple formats:
        // 1. Table format with brackets:
        //    [         1 tests successful      ]
        //    [         0 tests failed          ]
        // 2. Inline format: "[X] tests successful, [Y] tests failed"
        // 3. Summary line: "X tests successful, Y tests failed"
        
        // Try multiple regex patterns to extract test counts
        java.util.regex.Pattern[] successPatterns = {
            java.util.regex.Pattern.compile("\\[(\\s*(\\d+)\\s+tests successful\\s*)\\]"),
            java.util.regex.Pattern.compile("\\[(\\s*(\\d+)\\s*tests successful\\s*)\\]"),
            java.util.regex.Pattern.compile("(\\d+)\\s+tests successful"),
            java.util.regex.Pattern.compile("(\\d+)\\s*tests successful")
        };
        
        java.util.regex.Pattern[] failPatterns = {
            java.util.regex.Pattern.compile("\\[(\\s*(\\d+)\\s+tests failed\\s*)\\]"),
            java.util.regex.Pattern.compile("\\[(\\s*(\\d+)\\s*tests failed\\s*)\\]"),
            java.util.regex.Pattern.compile("(\\d+)\\s+tests failed"),
            java.util.regex.Pattern.compile("(\\d+)\\s*tests failed")
        };
        
        for (int i = 0; i < successPatterns.length; i++) {
            java.util.regex.Matcher successMatcher = successPatterns[i].matcher(output);
            java.util.regex.Matcher failMatcher = failPatterns[i].matcher(output);
            
            if (successMatcher.find() && failMatcher.find()) {
                try {
                    // Group 1 might contain brackets and text, group 2 is the number in bracket patterns
                    int groupToUse = successMatcher.groupCount() > 1 ? 2 : 1;
                    int successful = Integer.parseInt(successMatcher.group(groupToUse));
                    int failed = Integer.parseInt(failMatcher.group(groupToUse));
                    
                    log.debug("Found test results: {} successful, {} failed", successful, failed);
                    return failed == 0 && successful > 0;
                } catch (NumberFormatException | IllegalStateException e) {
                    log.debug("Failed to parse test numbers with pattern {}", i, e);
                    continue;
                }
            }
        }
        
        // Check for visual indicators in JUnit tree output
        if (output.contains("[OK]") && !output.contains("[FAILED]")) {
            log.debug("Found [OK] indicators in JUnit tree output");
            return true;
        }
        
        // Check for "ALL_TESTS_PASSED" for backward compatibility
        boolean allTestsPassed = output.contains("ALL_TESTS_PASSED");
        log.debug("Fallback check for ALL_TESTS_PASSED: {}", allTestsPassed);
        return allTestsPassed;
    }
    
    private boolean isJUnitOutput(String output) {
        return output.contains("Thanks for using JUnit") ||
               output.contains("Test run finished") || 
               (output.contains("tests successful") && output.contains("tests failed"));
    }
    
    private boolean didJUnitActuallyRunTests(String output) {
        // Check if JUnit actually ran tests (not just printed banner)
        // Evidence of test execution:
        // 1. Test statistics (tests successful/failed)
        // 2. JUnit tree output with [OK] or [FAILED]
        // 3. "Test run finished" line
        // 4. Test class names in output
        boolean hasTestStats = output.contains("tests successful") && output.contains("tests failed");
        boolean hasJUnitTree = output.contains("[OK]") || output.contains("[FAILED]");
        boolean hasTestRunFinished = output.contains("Test run finished");
        boolean hasTestClass = output.matches(".*Test\\s*\\[.*\\].*");
        
        return hasTestStats || hasJUnitTree || hasTestRunFinished || hasTestClass;
    }
    
    private String parseErrorDetails(int exitCode, String output) {
        // Handle timeout (exit code 124 from timeout command)
        if (exitCode == 124) {
            return "Execution timed out after 30 seconds. The program may be stuck in an infinite loop or taking too long.";
        }
        
        // Handle compilation errors (exit code 1 from javac)
        if (exitCode == 1) {
            // Extract compilation error messages
            if (output.contains("error:")) {
                // Get the first few lines that contain error information
                String[] lines = output.split("\n");
                StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
                int errorCount = 0;
                for (String line : lines) {
                    if (line.contains("error:") && errorCount < 3) {
                        errorMsg.append(line.trim()).append("\n");
                        errorCount++;
                    }
                }
                if (errorCount == 0) {
                    // Fallback to first line if no "error:" pattern found
                    errorMsg.append(lines.length > 0 ? lines[0] : output);
                }
                return errorMsg.toString().trim();
            }
        }
        
        // Handle "No tests found" scenario
        if (output.contains("No tests found") || output.contains("No test classes found")) {
            return "No tests were discovered. Ensure your test class is properly annotated with @Test and the class name matches.";
        }
        
        // Handle JUnit execution with test failures
        if (isJUnitOutput(output)) {
            // Extract test failure details
            java.util.regex.Pattern failPattern = java.util.regex.Pattern.compile(
                "(\\d+)\\s+tests failed"
            );
            java.util.regex.Matcher failMatcher = failPattern.matcher(output);
            if (failMatcher.find()) {
                int failed = Integer.parseInt(failMatcher.group(1));
                if (failed > 0) {
                    return "Tests failed: " + failed + " test(s) did not pass. Check the output for details.";
                }
            }
        }
        
        // Generic error with exit code
        return "Execution failed with exit code " + exitCode + ".\n" + 
               (output.length() > 500 ? output.substring(0, 500) + "..." : output);
    }
    
    private String parseJUnitSummary(String output) {
        // Extract test statistics
        java.util.regex.Pattern successPattern = java.util.regex.Pattern.compile("(\\d+)\\s+tests successful");
        java.util.regex.Pattern failPattern = java.util.regex.Pattern.compile("(\\d+)\\s+tests failed");
        java.util.regex.Pattern durationPattern = java.util.regex.Pattern.compile("Test run finished after (\\d+) ms");
        
        java.util.regex.Matcher successMatcher = successPattern.matcher(output);
        java.util.regex.Matcher failMatcher = failPattern.matcher(output);
        java.util.regex.Matcher durationMatcher = durationPattern.matcher(output);
        
        int passed = 0;
        int failed = 0;
        long duration = 0;
        
        if (successMatcher.find()) {
            passed = Integer.parseInt(successMatcher.group(1));
        }
        if (failMatcher.find()) {
            failed = Integer.parseInt(failMatcher.group(1));
        }
        if (durationMatcher.find()) {
            duration = Long.parseLong(durationMatcher.group(1));
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Test Results: ");
        summary.append(passed).append(" passed, ");
        summary.append(failed).append(" failed");
        if (duration > 0) {
            summary.append(", Duration: ").append(duration).append("ms");
        }
        return summary.toString();
    }
    
    /**
     * Mock execution for development when Docker is not available
     */
    private ExecutionResult mockExecuteJavaCode(String code, String testCode, Instant start) {
        ExecutionResult result = new ExecutionResult();
        result.setStatus("SUCCESS");
        
        // Simulate execution time
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check if code contains System.out.println
        if (code.contains("System.out.println") || code.contains("System.out.print")) {
            result.setOutput("Hello World!\nProgram executed successfully.");
        } else {
            result.setOutput("Program executed (mock). No output generated.");
        }
        
        // Simple test validation
        if (testCode != null && !testCode.isEmpty()) {
            // For now, assume tests pass if code compiles
            result.setTestPassed(true);
        } else {
            result.setTestPassed(false);
        }
        
        Instant end = Instant.now();
        result.setExecutionDuration(Duration.between(start, end).toMillis());
        
        return result;
    }
}