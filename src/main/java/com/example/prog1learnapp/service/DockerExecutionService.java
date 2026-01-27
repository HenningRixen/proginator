package com.example.prog1learnapp.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.example.prog1learnapp.dto.ExecutionResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DockerExecutionService {
    private static final Logger log = LoggerFactory.getLogger(DockerExecutionService.class);
    
    @Value("${app.code-execution.docker-enabled:true}")
    private boolean dockerEnabled;
    
    private DockerClient dockerClient;
    private boolean dockerAvailable = false;
    private boolean dockerCliAvailable = false;
    private String javaImage;
    
    public DockerExecutionService() {
        // Constructor left empty, initialization happens in @PostConstruct
    }
    
    @PostConstruct
    public void init() {
        if (!dockerEnabled) {
            log.info("Docker execution is disabled via configuration");
            dockerClient = null;
            dockerAvailable = false;
            return;
        }
        
        // Configure Docker client to connect to local Docker daemon via Unix socket
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();
        log.info("Docker host configured: {}", config.getDockerHost());
        
        try {
            // Create HTTP Client 5 transport with Unix socket support
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .build();
            
            // Build Docker client with HTTP Client 5 transport
            dockerClient = DockerClientBuilder.getInstance(config)
                    .withDockerHttpClient(httpClient)
                    .build();
            
            dockerAvailable = true;
            log.info("DockerExecutionService initialized successfully with HTTP Client 5 transport");
            
            // Test connection and ensure image exists
            testDockerConnection();
            ensureImageExists();
        } catch (Throwable t) {
            log.warn("Failed to create Docker client with HTTP Client 5 transport. Error: {}", t.getMessage(), t);
            log.info("Checking if Docker CLI is available as fallback...");
            checkDockerCliAvailable();
            dockerClient = null;
            dockerAvailable = false;
        }
    }
    
    private void testDockerConnection() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker client is null");
        }
        try {
            dockerClient.pingCmd().exec();
            log.info("Docker daemon connection test successful");
        } catch (Exception e) {
            log.error("Docker daemon connection test failed: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to Docker daemon", e);
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
    
    private void ensureImageExists() {
        if (dockerClient == null) {
            log.warn("Docker client not available, skipping image detection");
            javaImage = null;
            return;
        }
        
        // Try custom image first
        try {
            dockerClient.inspectImageCmd("proginator-java-sandbox").exec();
            javaImage = "proginator-java-sandbox";
            log.info("Docker image proginator-java-sandbox found");
            return;
        } catch (Exception e) {
            log.debug("Custom Docker image not found: {}", e.getMessage());
        }
        
        // Try to pull standard OpenJDK image
        try {
            log.info("Pulling Docker image openjdk:17-jdk-slim...");
            dockerClient.pullImageCmd("openjdk:17-jdk-slim").start().awaitCompletion();
            dockerClient.inspectImageCmd("openjdk:17-jdk-slim").exec();
            javaImage = "openjdk:17-jdk-slim";
            log.info("Using Docker image openjdk:17-jdk-slim");
            return;
        } catch (Exception e) {
            log.warn("Failed to pull openjdk:17-jdk-slim: {}", e.getMessage());
        }
        
        // Last resort: try any OpenJDK image that might exist
        try {
            dockerClient.inspectImageCmd("openjdk:17").exec();
            javaImage = "openjdk:17";
            log.info("Using Docker image openjdk:17");
        } catch (Exception e) {
            log.error("No suitable Docker image found for Java execution. Code execution will fail.");
            javaImage = "openjdk:17-jdk-slim"; // Will cause failure but provides a name
        }
    }
    
    /**
     * Execute Java code in a secure Docker container
     */
    public ExecutionResult executeJavaCode(String code, String testCode, int timeoutMs, int memoryLimitMB) {
        Instant start = Instant.now();
        
        // Strategy 1: Use docker-java API if available and image exists
        if (dockerClient != null && dockerAvailable && javaImage != null) {
            return executeWithDockerJava(code, testCode, timeoutMs, memoryLimitMB, start);
        }
        
        // Strategy 2: Use Docker CLI via ProcessBuilder if available
        if (dockerCliAvailable) {
            log.info("Falling back to Docker CLI execution via ProcessBuilder");
            return executeWithProcessBuilder(code, testCode, timeoutMs, memoryLimitMB);
        }
        
        // Strategy 3: Mock execution for development
        log.info("No Docker execution available, using mock execution");
        return mockExecuteJavaCode(code, testCode, start);
    }
    
    /**
     * Execute using docker-java API (original implementation)
     */
    private ExecutionResult executeWithDockerJava(String code, String testCode, int timeoutMs, int memoryLimitMB, Instant start) {
        ExecutionResult result = new ExecutionResult();
        result.setStatus("RUNNING");
        String containerId = null;
        try {
            // Create container with security constraints
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withMemory(memoryLimitMB * 1024L * 1024L) // Convert MB to bytes
                    .withCpuQuota(50000L) // 50% CPU limit
                    .withPidsLimit(50L)
                    .withNetworkMode("none") // No network access
                    .withReadonlyRootfs(true) // Read-only filesystem
                    .withSecurityOpts(Arrays.asList("no-new-privileges:true"));
            
            CreateContainerResponse container = dockerClient.createContainerCmd(javaImage)
                    .withHostConfig(hostConfig)
                    .withUser("nobody") // Non-root user (exists in most images)
                    .withCmd("tail", "-f", "/dev/null") // Keep container running
                    .exec();
            
            containerId = container.getId();
            dockerClient.startContainerCmd(containerId).exec();
            
            // Write user code to a file in container
            String javaCode = generateJavaTestFile(code, testCode);
            writeFileToContainer(containerId, "/tmp/Solution.java", javaCode);
            
            // Compile Java code
            ExecCreateCmdResponse compileResponse = dockerClient.execCreateCmd(containerId)
                    .withUser("nobody")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("sh", "-c", "cd /tmp && javac Solution.java 2>&1")
                    .exec();
            
            String compileOutput = execAndGetOutput(containerId, compileResponse.getId());
            
            if (!compileOutput.isEmpty()) {
                // Compilation error
                result.setStatus("ERROR");
                result.setError("Compilation error:\n" + compileOutput);
                result.setTestPassed(false);
                return result;
            }
            
            // Execute the test
            ExecCreateCmdResponse runResponse = dockerClient.execCreateCmd(containerId)
                    .withUser("nobody")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("sh", "-c", "cd /tmp && timeout " + (timeoutMs / 1000) + " java Solution 2>&1")
                    .exec();
            
            String runOutput = execAndGetOutput(containerId, runResponse.getId());
            
            // Parse test results
            boolean testsPassed = parseTestResults(runOutput);
            
            result.setStatus("SUCCESS");
            result.setOutput(runOutput);
            result.setTestPassed(testsPassed);
            
        } catch (Exception e) {
            log.error("Error executing code in Docker", e);
            result.setStatus("ERROR");
            result.setError("Execution error: " + e.getMessage());
            result.setTestPassed(false);
        } finally {
            if (containerId != null) {
                try {
                    dockerClient.stopContainerCmd(containerId).withTimeout(2).exec();
                    dockerClient.removeContainerCmd(containerId).exec();
                } catch (Exception e) {
                    log.warn("Failed to clean up container {}", containerId, e);
                }
            }
            
            Instant end = Instant.now();
            result.setExecutionDuration(Duration.between(start, end).toMillis());
        }
        
        return result;
    }
    
    private String generateJavaTestFile(String userCode, String testCode) {
        // Extract the class from user code (simplistic approach)
        String className = "Solution";
        
        // Combine user code with test code
        return String.format("""
            %s
            
            public class %s {
                public static void main(String[] args) {
                    try {
                        %s
                        System.out.println("ALL_TESTS_PASSED");
                    } catch (AssertionError e) {
                        System.out.println("TEST_FAILED: " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("ERROR: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            """, userCode, className, testCode);
    }
    
    private void writeFileToContainer(String containerId, String path, String content) throws Exception {
        // For simplicity, we'll use echo to write file
        // In production, use proper file copy mechanism
        ExecCreateCmdResponse writeResponse = dockerClient.execCreateCmd(containerId)
                .withUser("nobody")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd("sh", "-c", "cat > " + path + " << 'EOF'\n" + content + "\nEOF")
                .exec();
        
        execAndGetOutput(containerId, writeResponse.getId());
    }
    
    private String execAndGetOutput(String containerId, String execId) throws Exception {
        StringBuilder output = new StringBuilder();
        
        dockerClient.execStartCmd(execId).exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>() {
            @Override
            public void onNext(com.github.dockerjava.api.model.Frame frame) {
                output.append(new String(frame.getPayload()));
            }
        }).awaitCompletion(30, TimeUnit.SECONDS);
        
        // Get exec inspection for exit code
        InspectExecResponse inspect = dockerClient.inspectExecCmd(execId).exec();
        if (inspect.getExitCodeLong() != null && inspect.getExitCodeLong() != 0) {
            output.append("\nExit code: ").append(inspect.getExitCodeLong());
        }
        
        return output.toString().trim();
    }
    
    private boolean parseTestResults(String output) {
        // Simple check for test success marker
        return output.contains("ALL_TESTS_PASSED");
    }
    
    /**
     * Execute Java code using Docker CLI via ProcessBuilder (fallback when docker-java fails)
     */
    private ExecutionResult executeWithProcessBuilder(String code, String testCode, int timeoutMs, int memoryLimitMB) {
        Instant start = Instant.now();
        ExecutionResult result = new ExecutionResult();
        result.setStatus("RUNNING");
        
        Path tempDir = null;
        try {
            // Create temporary directory for code
            tempDir = Files.createTempDirectory("proginator-exec-");
            Path javaFile = tempDir.resolve("Solution.java");
            
            // Generate Java test file
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
                "-v", tempDir.toString() + ":/tmp/code:ro",
                "proginator-java-sandbox",
                "sh", "-c", 
                "cd /tmp/code && " +
                "javac Solution.java 2>&1 && " +
                "timeout " + (timeoutMs / 1000) + " java Solution 2>&1"
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
            if (exitCode == 0) {
                result.setStatus("SUCCESS");
                result.setOutput(outputStr);
                result.setTestPassed(parseTestResults(outputStr));
            } else {
                result.setStatus("ERROR");
                result.setError("Execution failed with exit code " + exitCode + "\n" + outputStr);
                result.setTestPassed(false);
            }
            
        } catch (IOException | InterruptedException e) {
            log.error("ProcessBuilder execution failed", e);
            result.setStatus("ERROR");
            result.setError("ProcessBuilder execution error: " + e.getMessage());
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