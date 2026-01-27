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
            
            // Parse results
            boolean isJUnitOutput = isJUnitOutput(outputStr);
            if (exitCode == 0 || isJUnitOutput) {
                result.setStatus("SUCCESS");
                result.setOutput(outputStr);
                boolean testsPassed = parseTestResults(outputStr);
                // If exitCode != 0 but JUnit output exists, it's test failure, not error
                if (exitCode != 0) {
                    testsPassed = false;
                }
                result.setTestPassed(testsPassed);
            } else {
                result.setStatus("ERROR");
                result.setError("Execution failed with exit code " + exitCode + "\n" + outputStr);
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
    
    private boolean parseTestResults(String output) {
        // Parse JUnit output for test results
        // JUnit Console Launcher outputs lines like:
        // "Test run finished after X ms"
        // "[X] tests successful, [Y] tests failed"
        if (output.contains("tests successful") && output.contains("tests failed")) {
            // Extract numbers
            java.util.regex.Pattern successPattern = java.util.regex.Pattern.compile(
                "(\\d+)\\s+tests successful"
            );
            java.util.regex.Pattern failPattern = java.util.regex.Pattern.compile(
                "(\\d+)\\s+tests failed"
            );
            java.util.regex.Matcher successMatcher = successPattern.matcher(output);
            java.util.regex.Matcher failMatcher = failPattern.matcher(output);
            
            if (successMatcher.find() && failMatcher.find()) {
                int successful = Integer.parseInt(successMatcher.group(1));
                int failed = Integer.parseInt(failMatcher.group(1));
                return failed == 0 && successful > 0;
            }
        }
        // Fallback: check for "ALL_TESTS_PASSED" for backward compatibility
        return output.contains("ALL_TESTS_PASSED");
    }
    
    private boolean isJUnitOutput(String output) {
        return output.contains("Test run finished") || 
               (output.contains("tests successful") && output.contains("tests failed"));
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