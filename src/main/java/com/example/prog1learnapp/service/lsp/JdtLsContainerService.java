package com.example.prog1learnapp.service.lsp;

import com.example.prog1learnapp.config.lsp.LspProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JdtLsContainerService {
    private static final Logger log = LoggerFactory.getLogger(JdtLsContainerService.class);
    private static final String CONTAINER_PREFIX = "proginator-jdtls-";

    private final LspProperties lspProperties;
    private final Map<String, ContainerSession> sessions = new HashMap<>();
    private boolean dockerAvailable;

    public JdtLsContainerService(LspProperties lspProperties) {
        this.lspProperties = lspProperties;
    }

    @PostConstruct
    public void init() {
        if (!lspProperties.isEnabled()) {
            dockerAvailable = false;
            log.info("LSP is disabled by configuration");
            return;
        }

        dockerAvailable = runCommand(List.of("docker", "version")).exitCode == 0;
        if (!dockerAvailable) {
            log.warn("Docker CLI is not available. LSP container mode cannot start.");
        }
    }

    @PreDestroy
    public synchronized void shutdown() {
        for (ContainerSession session : sessions.values()) {
            removeContainer(session.containerName);
        }
        sessions.clear();
    }

    public synchronized Optional<String> acquireContainer(String sessionKey) {
        if (!lspProperties.isEnabled() || !dockerAvailable) {
            return Optional.empty();
        }

        ContainerSession existing = sessions.get(sessionKey);
        if (existing != null) {
            existing.refCount++;
            existing.lastUsedEpochMs = Instant.now().toEpochMilli();
            return Optional.of(existing.containerName);
        }

        if (sessions.size() >= lspProperties.getMaxSessions()) {
            log.warn("Reached maximum LSP session limit: {}", lspProperties.getMaxSessions());
            return Optional.empty();
        }

        String containerName = CONTAINER_PREFIX + Integer.toHexString(sessionKey.hashCode());
        if (!createAndStartContainer(containerName)) {
            return Optional.empty();
        }

        ContainerSession created = new ContainerSession(containerName);
        created.refCount = 1;
        sessions.put(sessionKey, created);
        return Optional.of(containerName);
    }

    public synchronized void releaseContainer(String sessionKey) {
        ContainerSession session = sessions.get(sessionKey);
        if (session == null) {
            return;
        }

        if (session.refCount > 0) {
            session.refCount--;
        }
        session.lastUsedEpochMs = Instant.now().toEpochMilli();
    }

    public synchronized void forceRemove(String sessionKey) {
        ContainerSession removed = sessions.remove(sessionKey);
        if (removed != null) {
            removeContainer(removed.containerName);
        }
    }

    public synchronized int getActiveSessionCount() {
        return sessions.size();
    }

    public boolean isDockerAvailable() {
        return dockerAvailable;
    }

    public boolean isEnabled() {
        return lspProperties.isEnabled();
    }

    public boolean isImageAvailable() {
        if (!dockerAvailable) {
            return false;
        }
        CommandResult result = runCommand(List.of("docker", "image", "inspect", lspProperties.getImage()));
        return result.exitCode == 0;
    }

    @Scheduled(fixedDelayString = "${app.lsp.cleanup-interval-ms:30000}")
    public synchronized void cleanupIdleContainers() {
        if (sessions.isEmpty()) {
            return;
        }

        long now = Instant.now().toEpochMilli();
        long ttlMs = lspProperties.getIdleTtlSeconds() * 1000;
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, ContainerSession> entry : sessions.entrySet()) {
            ContainerSession session = entry.getValue();
            if (session.refCount == 0 && now - session.lastUsedEpochMs > ttlMs) {
                toRemove.add(entry.getKey());
            }
        }

        for (String sessionKey : toRemove) {
            ContainerSession removed = sessions.remove(sessionKey);
            if (removed != null) {
                log.debug("Removing idle LSP container {} for session {}", removed.containerName, sessionKey);
                removeContainer(removed.containerName);
            }
        }
    }

    private boolean createAndStartContainer(String containerName) {
        runCommand(List.of("docker", "rm", "-f", containerName));

        List<String> createCommand = List.of(
                "docker", "create",
                "--name", containerName,
                "--memory=" + lspProperties.getMemoryMb() + "m",
                "--cpus=" + lspProperties.getCpus(),
                "--pids-limit=100",
                "--network=none",
                "--read-only",
                "--tmpfs", "/tmp:rw,size=256m,mode=1777",
                "--tmpfs", "/home/runner/.eclipse:rw,size=128m,mode=1777",
                "--security-opt=no-new-privileges",
                "--cap-drop=ALL",
                "--user=runner",
                lspProperties.getImage(),
                "tail", "-f", "/dev/null"
        );

        CommandResult createResult = runCommand(createCommand);
        if (createResult.exitCode != 0) {
            log.error("Failed to create LSP container {}: {}", containerName, createResult.output);
            return false;
        }

        CommandResult startResult = runCommand(List.of("docker", "start", containerName));
        if (startResult.exitCode != 0) {
            log.error("Failed to start LSP container {}: {}", containerName, startResult.output);
            removeContainer(containerName);
            return false;
        }

        return true;
    }

    private void removeContainer(String containerName) {
        runCommand(List.of("docker", "rm", "-f", containerName));
    }

    private CommandResult runCommand(List<String> command) {
        try {
            Process process = new ProcessBuilder(command).start();
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                 BufferedReader errorReader = new BufferedReader(
                         new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String stdout = reader.lines().reduce("", (a, b) -> a + b + "\n");
                String stderr = errorReader.lines().reduce("", (a, b) -> a + b + "\n");
                output = (stdout + stderr).trim();
            }

            int exitCode = process.waitFor();
            return new CommandResult(exitCode, output);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CommandResult(1, e.getMessage());
        } catch (IOException e) {
            return new CommandResult(1, e.getMessage());
        }
    }

    private static final class ContainerSession {
        private final String containerName;
        private int refCount;
        private long lastUsedEpochMs = Instant.now().toEpochMilli();

        private ContainerSession(String containerName) {
            this.containerName = containerName;
        }
    }

    private static final class CommandResult {
        private final int exitCode;
        private final String output;

        private CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
