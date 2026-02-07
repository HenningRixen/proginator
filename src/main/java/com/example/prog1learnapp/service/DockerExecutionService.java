package com.example.prog1learnapp.service;

import com.example.prog1learnapp.dto.ExecutionResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DockerExecutionService {
    private static final Logger log = LoggerFactory.getLogger(DockerExecutionService.class);

    private static final String JUNIT_JAR = "/opt/junit-platform-console-standalone.jar";
    private static final String WARM_CONTAINER_PREFIX = "proginator-exec-warm-";
    private static final String CUSTOM_RUNNER_CLASS = "Prog1InternalTestRunner";

    @Value("${app.code-execution.docker-enabled:true}")
    private boolean dockerEnabled;

    @Value("${app.code-execution.mode:warm-container}")
    private String executionMode;

    @Value("${app.code-execution.pool-size:1}")
    private int poolSize;

    @Value("${app.code-execution.warm-image:proginator-java-sandbox}")
    private String warmImage;

    @Value("${app.code-execution.default-memory-mb:512}")
    private int defaultMemoryMb;

    @Value("${app.code-execution.default-cpus:2.0}")
    private String defaultCpus;

    private boolean dockerCliAvailable = false;
    private final List<String> warmContainerNames = new ArrayList<>();
    private final AtomicInteger containerCursor = new AtomicInteger(0);
    private Semaphore executionSlots = new Semaphore(1, true);

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
            executionSlots = new Semaphore(Math.max(poolSize, 1), true);
            if (useWarmContainerMode()) {
                initializeWarmContainers();
            }
            log.info("Docker CLI execution enabled (mode={}, poolSize={})", executionMode, Math.max(poolSize, 1));
        } else {
            log.info("Docker CLI not available, will use mock execution");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (!dockerCliAvailable || warmContainerNames.isEmpty()) {
            return;
        }

        for (String containerName : warmContainerNames) {
            runCommand(Arrays.asList("docker", "rm", "-f", containerName));
        }
    }

    private void checkDockerCliAvailable() {
        CommandResult result = runCommand(Arrays.asList("docker", "version"));
        dockerCliAvailable = result.exitCode() == 0;

        if (dockerCliAvailable) {
            log.info("Docker CLI is available");
        } else {
            log.warn("Docker CLI command failed with exit code {}", result.exitCode());
        }
    }

    public ExecutionResult executeJavaCode(String code, String testCode, int timeoutMs, int memoryLimitMB) {
        Instant start = Instant.now();

        if (dockerCliAvailable) {
            if (useWarmContainerMode()) {
                return executeWithWarmContainer(code, testCode, timeoutMs, start);
            }
            return executeWithDockerCliLegacy(code, testCode, timeoutMs, memoryLimitMB, start);
        }

        log.info("No Docker execution available, using mock execution");
        return mockExecuteJavaCode(code, testCode, start);
    }

    private boolean useWarmContainerMode() {
        return "warm-container".equalsIgnoreCase(executionMode);
    }

    private void initializeWarmContainers() {
        warmContainerNames.clear();
        int size = Math.max(poolSize, 1);

        for (int i = 0; i < size; i++) {
            String containerName = WARM_CONTAINER_PREFIX + i;
            if (recreateWarmContainer(containerName)) {
                warmContainerNames.add(containerName);
            }
        }

        if (warmContainerNames.isEmpty()) {
            log.warn("No warm container could be started. Falling back to legacy docker run mode.");
            executionMode = "legacy-run";
        }
    }

    private String nextWarmContainer() {
        if (warmContainerNames.isEmpty()) {
            return null;
        }
        int index = Math.floorMod(containerCursor.getAndIncrement(), warmContainerNames.size());
        return warmContainerNames.get(index);
    }

    private boolean ensureWarmContainerRunning(String containerName) {
        CommandResult inspect = runCommand(Arrays.asList("docker", "inspect", "-f", "{{.State.Running}}", containerName));
        if (inspect.exitCode() == 0 && "true".equalsIgnoreCase(inspect.output().trim())) {
            return true;
        }

        log.warn("Warm container {} not healthy, recreating", containerName);
        return recreateWarmContainer(containerName);
    }

    private boolean recreateWarmContainer(String containerName) {
        runCommand(Arrays.asList("docker", "rm", "-f", containerName));

        List<String> createCmd = Arrays.asList(
                "docker", "create",
                "--name", containerName,
                "--memory=" + defaultMemoryMb + "m",
                "--cpus=" + defaultCpus,
                "--pids-limit=50",
                "--network=none",
                "--read-only",
                "--tmpfs", "/tmp:rw,size=256m,mode=1777",
                "--security-opt=no-new-privileges",
                "--cap-drop=ALL",
                "--user=runner",
                warmImage,
                "tail", "-f", "/dev/null"
        );

        CommandResult createResult = runCommand(createCmd);
        if (createResult.exitCode() != 0) {
            log.error("Failed to create warm container {}: {}", containerName, createResult.output());
            return false;
        }

        CommandResult startResult = runCommand(Arrays.asList("docker", "start", containerName));
        if (startResult.exitCode() != 0) {
            log.error("Failed to start warm container {}: {}", containerName, startResult.output());
            runCommand(Arrays.asList("docker", "rm", "-f", containerName));
            return false;
        }

        return true;
    }

    private ExecutionResult executeWithWarmContainer(String code, String testCode, int timeoutMs, Instant start) {
        ExecutionResult result = new ExecutionResult();
        result.setStatus("RUNNING");

        Path tempDir = null;
        String selectedContainer = null;
        String jobDir = null;

        try {
            executionSlots.acquire();
            selectedContainer = nextWarmContainer();

            if (selectedContainer == null) {
                result.setStatus("ERROR");
                result.setError("No warm container available");
                result.setTestPassed(false);
                return result;
            }

            if (!ensureWarmContainerRunning(selectedContainer)) {
                result.setStatus("ERROR");
                result.setError("Warm container is not available");
                result.setTestPassed(false);
                return result;
            }

            tempDir = Files.createTempDirectory("proginator-exec-");
            String className = extractClassName(code);
            if (className == null || className.isEmpty()) {
                className = "Solution";
            }
            Path javaFile = tempDir.resolve(className + ".java");
            String javaCode = generateJavaTestFile(code, testCode);
            Files.writeString(javaFile, javaCode);

            Instant setupStart = Instant.now();
            jobDir = "/tmp/jobs/" + UUID.randomUUID();

            CommandResult mkdirResult = runCommand(Arrays.asList(
                    "docker", "exec", "--user", "runner", selectedContainer,
                    "sh", "-c", "mkdir -p " + jobDir
            ));

            if (mkdirResult.exitCode() != 0) {
                result.setStatus("ERROR");
                result.setError("Failed to initialize execution workspace: " + mkdirResult.output());
                result.setTestPassed(false);
                return result;
            }

            CommandResult writeResult = runCommandWithInput(Arrays.asList(
                    "docker", "exec", "-i", "--user", "runner", selectedContainer,
                    "sh", "-c",
                    "cat > " + jobDir + "/" + className + ".java"
            ), javaCode);

            if (writeResult.exitCode() != 0) {
                result.setStatus("ERROR");
                result.setError("Failed to transfer source file: " + writeResult.output());
                result.setTestPassed(false);
                return result;
            }

            result.setDockerStartupMs(Duration.between(setupStart, Instant.now()).toMillis());

            Instant compileStart = Instant.now();
            CommandResult compileResult = runCommand(Arrays.asList(
                    "docker", "exec", "--user", "runner", selectedContainer,
                    "sh", "-c",
                    "cd " + jobDir + " && javac -J-XX:TieredStopAtLevel=1 -J-Xms64m -J-Xmx256m -proc:none -cp " +
                            JUNIT_JAR + " " + className + ".java 2>&1"
            ));
            result.setCompileMs(Duration.between(compileStart, Instant.now()).toMillis());

            String compileOutput = stripAnsiCodes(compileResult.output());
            if (compileResult.exitCode() != 0) {
                result.setStatus("ERROR");
                result.setError(parseErrorDetails(compileResult.exitCode(), compileOutput));
                result.setOutput(compileOutput);
                result.setTestPassed(false);
                return result;
            }

            Instant testStart = Instant.now();
            CommandResult testResult = runCommand(Arrays.asList(
                    "docker", "exec", "--user", "runner", selectedContainer,
                    "sh", "-c",
                    "cd " + jobDir + " && timeout " + (timeoutMs / 1000) + " java " +
                            "-XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xms64m -Xmx256m " +
                            "-cp " + jobDir + ":" + JUNIT_JAR + " " +
                            CUSTOM_RUNNER_CLASS + " 2>&1"
            ));
            result.setTestRunMs(Duration.between(testStart, Instant.now()).toMillis());

            applyExecutionResult(result, testResult.exitCode(), testResult.output());
        } catch (IOException | InterruptedException e) {
            log.error("Warm container execution failed", e);
            result.setStatus("ERROR");
            result.setError("Docker warm container execution error: " + e.getMessage());
            result.setTestPassed(false);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        } finally {
            if (jobDir != null && selectedContainer != null) {
                runCommand(Arrays.asList(
                        "docker", "exec", "--user", "runner", selectedContainer,
                        "sh", "-c", "rm -rf " + jobDir
                ));
            }

            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted((a, b) -> -a.compareTo(b))
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException ignored) {
                                }
                            });
                } catch (IOException e) {
                    log.warn("Failed to clean up temp directory {}", tempDir, e);
                }
            }

            if (executionSlots.availablePermits() < Math.max(poolSize, 1)) {
                executionSlots.release();
            }

            result.setExecutionDuration(Duration.between(start, Instant.now()).toMillis());
            logPerformance(result);
        }

        return result;
    }

    private ExecutionResult executeWithDockerCliLegacy(String code, String testCode, int timeoutMs,
                                                       int memoryLimitMB, Instant start) {
        ExecutionResult result = new ExecutionResult();
        result.setStatus("RUNNING");

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("proginator-exec-");
            String className = extractClassName(code);
            if (className == null || className.isEmpty()) {
                className = "Solution";
            }
            Path javaFile = tempDir.resolve(className + ".java");
            String javaCode = generateJavaTestFile(code, testCode);
            Files.writeString(javaFile, javaCode);

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
                    "-v", tempDir + ":/tmp/code",
                    warmImage,
                    "sh", "-c",
                    "cd /tmp/code && " +
                            "javac -J-XX:TieredStopAtLevel=1 -J-Xms64m -J-Xmx256m -proc:none -cp " +
                            JUNIT_JAR + " " + className + ".java 2>&1 && " +
                            "timeout " + (timeoutMs / 1000) + " java " +
                            "-XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xms64m -Xmx256m " +
                            "-cp /tmp/code:" + JUNIT_JAR + " " +
                            CUSTOM_RUNNER_CLASS + " 2>&1"
            );

            CommandResult processResult = runCommand(dockerCmd);
            applyExecutionResult(result, processResult.exitCode(), processResult.output());
        } catch (IOException e) {
            log.error("Docker legacy execution failed", e);
            result.setStatus("ERROR");
            result.setError("Docker legacy execution error: " + e.getMessage());
            result.setTestPassed(false);
        } finally {
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted((a, b) -> -a.compareTo(b))
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException ignored) {
                                }
                            });
                } catch (IOException e) {
                    log.warn("Failed to clean up temp directory {}", tempDir, e);
                }
            }

            result.setExecutionDuration(Duration.between(start, Instant.now()).toMillis());
            logPerformance(result);
        }

        return result;
    }

    private void applyExecutionResult(ExecutionResult result, int exitCode, String output) {
        String strippedOutput = stripAnsiCodes(output == null ? "" : output.trim());
        boolean isJUnitOutput = isJUnitOutput(strippedOutput);

        if (strippedOutput.contains("ALL_TESTS_PASSED")) {
            result.setStatus("SUCCESS");
            result.setOutput(parseCustomRunnerSummary(strippedOutput) + "\n\n" + strippedOutput);
            result.setTestPassed(true);
            return;
        }

        if (strippedOutput.contains("TEST_FAILED:")) {
            result.setStatus("SUCCESS");
            result.setOutput(strippedOutput);
            result.setTestPassed(false);
            return;
        }

        if (exitCode == 124) {
            result.setStatus("ERROR");
            result.setError("Execution timed out after 30 seconds. The program may be stuck in an infinite loop or taking too long.");
            result.setOutput(strippedOutput);
            result.setTestPassed(false);
            return;
        }

        if (strippedOutput.contains("No tests found") || strippedOutput.contains("No test classes found")) {
            result.setStatus("ERROR");
            result.setError("No tests were discovered. Ensure your test class is properly annotated with @Test and the class name matches.");
            result.setOutput(strippedOutput);
            result.setTestPassed(false);
            return;
        }

        if (exitCode == 0) {
            result.setStatus("SUCCESS");
            String enhancedOutput = strippedOutput;
            if (isJUnitOutput) {
                enhancedOutput = parseJUnitSummary(strippedOutput) + "\n\n" + strippedOutput;
            }
            result.setOutput(enhancedOutput);
            result.setTestPassed(parseTestResults(strippedOutput));
            return;
        }

        if (isJUnitOutput && didJUnitActuallyRunTests(strippedOutput)) {
            result.setStatus("SUCCESS");
            result.setOutput(parseJUnitSummary(strippedOutput) + "\n\n" + strippedOutput);
            result.setTestPassed(parseTestResults(strippedOutput));
            return;
        }

        if (isJUnitOutput) {
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
            return;
        }

        result.setStatus("ERROR");
        result.setError(parseErrorDetails(exitCode, strippedOutput));
        result.setOutput(strippedOutput);
        result.setTestPassed(false);
    }

    private void logPerformance(ExecutionResult result) {
        log.info("Execution performance mode={} totalMs={} startupMs={} compileMs={} testMs={} status={}",
                executionMode,
                valueOrMinusOne(result.getExecutionDuration()),
                valueOrMinusOne(result.getDockerStartupMs()),
                valueOrMinusOne(result.getCompileMs()),
                valueOrMinusOne(result.getTestRunMs()),
                result.getStatus());
    }

    private long valueOrMinusOne(Long value) {
        return value == null ? -1L : value;
    }

    private CommandResult runCommand(List<String> command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            return new CommandResult(exitCode, output.toString().trim());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new CommandResult(1, e.getMessage());
        }
    }

    private CommandResult runCommandWithInput(List<String> command, String input) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(input.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            return new CommandResult(exitCode, output.toString().trim());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new CommandResult(1, e.getMessage());
        }
    }

    private String generateJavaTestFile(String userCode, String testCode) {
        String className = extractClassName(userCode);
        if (className == null || className.isEmpty()) {
            className = "Solution";
            userCode = String.format("public class %s {\n%s\n}", className, userCode);
        }

        String testClassName = className + "Test";
        String testClassBody = String.format("""
            class %s {
                void runTest() {
                    %s instance = new %s();
                    %s
                }
            }
            """, testClassName, className, className, testCode);

        String runnerClass = String.format("""
            class %s {
                public static void main(String[] args) {
                    long start = System.currentTimeMillis();
                    %s testInstance = new %s();
                    try {
                        testInstance.runTest();
                        long duration = System.currentTimeMillis() - start;
                        System.out.println("ALL_TESTS_PASSED");
                        System.out.println("TEST_DURATION_MS=" + duration);
                    } catch (AssertionError e) {
                        System.out.println("TEST_FAILED: " + (e.getMessage() == null ? "Assertion failed" : e.getMessage()));
                        e.printStackTrace(System.out);
                        System.exit(1);
                    } catch (Throwable e) {
                        System.out.println("EXECUTION_ERROR: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
                        e.printStackTrace(System.out);
                        System.exit(1);
                    }
                }
            }
            """, CUSTOM_RUNNER_CLASS, testClassName, testClassName);

        String imports = "import static org.junit.jupiter.api.Assertions.*;\n\n";
        return imports + userCode + "\n\n" + testClassBody + "\n\n" + runnerClass;
    }

    private String extractClassName(String javaCode) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:public\\s+)?class\\s+(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String stripAnsiCodes(String input) {
        if (input == null) {
            return null;
        }
        String stripped = input.replaceAll("\\u001B\\[[?]?[;\\d]*[A-Za-z]", "");
        return stripped.replace("\u001B", "");
    }

    private boolean parseTestResults(String output) {
        log.debug("Parsing test results from output: {}", output);

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
                    int groupToUse = successMatcher.groupCount() > 1 ? 2 : 1;
                    int successful = Integer.parseInt(successMatcher.group(groupToUse));
                    int failed = Integer.parseInt(failMatcher.group(groupToUse));

                    log.debug("Found test results: {} successful, {} failed", successful, failed);
                    return failed == 0 && successful > 0;
                } catch (NumberFormatException | IllegalStateException e) {
                    log.debug("Failed to parse test numbers with pattern {}", i, e);
                }
            }
        }

        if (output.contains("[OK]") && !output.contains("[FAILED]")) {
            log.debug("Found [OK] indicators in JUnit tree output");
            return true;
        }

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
        boolean hasTestStats = output.contains("tests successful") && output.contains("tests failed");
        boolean hasJUnitTree = output.contains("[OK]") || output.contains("[FAILED]");
        boolean hasTestRunFinished = output.contains("Test run finished");
        boolean hasTestClass = output.matches(".*Test\\s*\\[.*\\].*");

        return hasTestStats || hasJUnitTree || hasTestRunFinished || hasTestClass;
    }

    private String parseErrorDetails(int exitCode, String output) {
        if (exitCode == 124) {
            return "Execution timed out after 30 seconds. The program may be stuck in an infinite loop or taking too long.";
        }

        if (exitCode == 1 && output.contains("error:")) {
            String[] lines = output.split("\\n");
            StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
            int errorCount = 0;
            for (String line : lines) {
                if (line.contains("error:") && errorCount < 3) {
                    errorMsg.append(line.trim()).append("\n");
                    errorCount++;
                }
            }
            if (errorCount == 0 && lines.length > 0) {
                errorMsg.append(lines[0]);
            }
            return errorMsg.toString().trim();
        }

        if (output.contains("No tests found") || output.contains("No test classes found")) {
            return "No tests were discovered. Ensure your test class is properly annotated with @Test and the class name matches.";
        }

        if (isJUnitOutput(output)) {
            java.util.regex.Pattern failPattern = java.util.regex.Pattern.compile("(\\d+)\\s+tests failed");
            java.util.regex.Matcher failMatcher = failPattern.matcher(output);
            if (failMatcher.find()) {
                int failed = Integer.parseInt(failMatcher.group(1));
                if (failed > 0) {
                    return "Tests failed: " + failed + " test(s) did not pass. Check the output for details.";
                }
            }
        }

        return "Execution failed with exit code " + exitCode + ".\n" +
                (output.length() > 500 ? output.substring(0, 500) + "..." : output);
    }

    private String parseJUnitSummary(String output) {
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

    private String parseCustomRunnerSummary(String output) {
        java.util.regex.Pattern durationPattern = java.util.regex.Pattern.compile("TEST_DURATION_MS=(\\d+)");
        java.util.regex.Matcher matcher = durationPattern.matcher(output);
        if (matcher.find()) {
            return "Test Results: 1 passed, 0 failed, Duration: " + matcher.group(1) + "ms";
        }
        return "Test Results: 1 passed, 0 failed";
    }

    private ExecutionResult mockExecuteJavaCode(String code, String testCode, Instant start) {
        ExecutionResult result = new ExecutionResult();
        result.setStatus("SUCCESS");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (code.contains("System.out.println") || code.contains("System.out.print")) {
            result.setOutput("Hello World!\nProgram executed successfully.");
        } else {
            result.setOutput("Program executed (mock). No output generated.");
        }

        result.setTestPassed(testCode != null && !testCode.isEmpty());
        result.setExecutionDuration(Duration.between(start, Instant.now()).toMillis());
        logPerformance(result);

        return result;
    }

    private record CommandResult(int exitCode, String output) {
    }
}
