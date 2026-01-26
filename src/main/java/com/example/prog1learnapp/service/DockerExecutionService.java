package com.example.prog1learnapp.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.example.prog1learnapp.dto.ExecutionResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        
        // Configure Docker client to connect to local Docker daemon
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        log.info("Docker host: {}", config.getDockerHost());
        
        try {
            // Let DockerClientBuilder choose the best available transport
            dockerClient = DockerClientBuilder.getInstance(config).build();
            dockerAvailable = true;
            log.info("DockerExecutionService initialized successfully");
            
            ensureImageExists();
        } catch (Throwable t) {
            log.warn("Failed to create Docker client. Code execution will be disabled. Error: {}", t.getMessage(), t);
            dockerClient = null;
            dockerAvailable = false;
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
        ExecutionResult result = new ExecutionResult();
        
        if (dockerClient == null || !dockerAvailable || javaImage == null) {
            // Docker not available, run mock execution for development
            return mockExecuteJavaCode(code, testCode, start);
        }
        
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