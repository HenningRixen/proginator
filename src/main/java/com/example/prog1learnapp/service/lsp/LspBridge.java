package com.example.prog1learnapp.service.lsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LspBridge {
    private static final Logger log = LoggerFactory.getLogger(LspBridge.class);

    private final String containerName;
    private final String workspaceKey;
    private final long connectTimeoutMs;
    private final long startupGraceMs;
    private final Map<String, WebSocketSession> attachedSessions = new ConcurrentHashMap<>();

    private Process process;
    private OutputStream lspInput;
    private ExecutorService ioExecutor;
    private volatile long startedAtNs;
    private volatile boolean firstServerMessageLogged;
    private volatile long lastAttachedEpochMs = System.currentTimeMillis();
    private final ConcurrentLinkedDeque<String> stderrTail = new ConcurrentLinkedDeque<>();
    private static final int STDERR_TAIL_LIMIT = 12;

    public LspBridge(String containerName,
                     String workspaceKey,
                     long connectTimeoutMs,
                     long startupGraceMs) {
        this.containerName = containerName;
        this.workspaceKey = workspaceKey;
        this.connectTimeoutMs = connectTimeoutMs;
        this.startupGraceMs = startupGraceMs;
    }

    public synchronized void start() throws IOException {
        long startNs = System.nanoTime();
        if (process != null && process.isAlive()) {
            log.debug("LSP bridge already running container={} workspaceKey={} totalStartMs={}",
                    containerName, workspaceKey, elapsedMs(startNs));
            return;
        }

        String workspaceDir = "/tmp/workspaces/" + workspaceKey;
        String startCommand = "mkdir -p " + workspaceDir + "/project/src" +
                " && if [ -x /opt/jdtls/bin/jdtls ]; then " +
                "exec env HOME=/tmp /opt/jdtls/bin/jdtls -data " + workspaceDir +
                "; elif [ -x /opt/jdtls/jdtls ]; then " +
                "exec env HOME=/tmp /opt/jdtls/jdtls -data " + workspaceDir +
                "; elif command -v jdtls >/dev/null 2>&1; then " +
                "exec env HOME=/tmp jdtls -data " + workspaceDir +
                "; else echo 'jdtls binary not found' >&2; exit 127; fi";

        List<String> command = List.of(
                "docker", "exec", "-i", "--user", "runner", containerName,
                "sh", "-c", startCommand
        );

        process = new ProcessBuilder(command).start();
        startedAtNs = System.nanoTime();
        firstServerMessageLogged = false;
        lspInput = process.getOutputStream();
        ioExecutor = Executors.newFixedThreadPool(2);

        ioExecutor.submit(this::forwardLspMessagesToClient);
        ioExecutor.submit(this::drainStderrLogs);

        waitForEarlyFailure(process, startupGraceMs);
        log.debug("LSP bridge started container={} workspaceKey={} totalStartMs={}",
                containerName, workspaceKey, elapsedMs(startNs));
    }

    public synchronized void sendClientMessage(String payload) throws IOException {
        if (process == null || !process.isAlive()) {
            throw new IOException("LSP backend process is not running");
        }
        LspJsonRpcFraming.writeMessage(lspInput, payload);
    }

    public synchronized void close() {
        try {
            if (lspInput != null) {
                lspInput.close();
            }
        } catch (IOException ignored) {
        }

        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (ioExecutor != null) {
            ioExecutor.shutdownNow();
        }
        attachedSessions.clear();
    }

    private void forwardLspMessagesToClient() {
        int forwardedMessages = 0;
        try {
            InputStream stdout = process.getInputStream();
            while (process.isAlive()) {
                String message = LspJsonRpcFraming.readMessage(stdout);
                if (message == null) {
                    break;
                }
                if (!firstServerMessageLogged) {
                    firstServerMessageLogged = true;
                    log.debug("LSP bridge first server message container={} workspaceKey={} firstMessageMs={}",
                            containerName, workspaceKey, elapsedMs(startedAtNs));
                }
                TextMessage textMessage = new TextMessage(message);
                for (WebSocketSession session : attachedSessions.values()) {
                    if (!session.isOpen()) {
                        detachSession(session.getId());
                        continue;
                    }
                    try {
                        synchronized (session) {
                            session.sendMessage(textMessage);
                        }
                        forwardedMessages++;
                    } catch (IOException sendError) {
                        log.debug("LSP bridge failed to send to wsId={} container={} error={}",
                                session.getId(), containerName, sendError.getMessage());
                        detachSession(session.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("LSP bridge output forwarding failed: {}", e.getMessage());
        } finally {
            log.debug("LSP bridge output loop finished container={} workspaceKey={} forwardedMessages={}",
                    containerName, workspaceKey, forwardedMessages);
            for (WebSocketSession session : attachedSessions.values()) {
                if (!session.isOpen()) {
                    continue;
                }
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void drainStderrLogs() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.warn("JDT LS [{}]: {}", containerName, line);
                appendStderrTail(line);
            }
        } catch (IOException e) {
            log.debug("LSP stderr reader stopped for {}: {}", containerName, e.getMessage());
        }
    }

    void waitForEarlyFailure(Process observedProcess, long graceMs) throws IOException {
        long effectiveGraceMs = Math.max(0L, graceMs);
        long deadline = System.currentTimeMillis() + effectiveGraceMs;
        while (System.currentTimeMillis() < deadline) {
            if (observedProcess == null || !observedProcess.isAlive()) {
                String stderrSummary = summarizeStderrTail();
                String runtimeDiagnostics = collectRuntimeDiagnostics();
                throw new IOException("JDT LS failed to start in container " + containerName +
                        " stderrTail=\"" + stderrSummary + "\"" +
                        " runtimeDiagnostics=\"" + runtimeDiagnostics + "\"");
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for JDT LS startup", e);
            }
        }
    }

    private long elapsedMs(long startedNs) {
        return (System.nanoTime() - startedNs) / 1_000_000;
    }

    public void attachSession(WebSocketSession session) {
        if (session == null) {
            return;
        }
        attachedSessions.put(session.getId(), session);
        lastAttachedEpochMs = System.currentTimeMillis();
    }

    public void detachSession(String webSocketId) {
        if (webSocketId == null) {
            return;
        }
        attachedSessions.remove(webSocketId);
        if (attachedSessions.isEmpty()) {
            lastAttachedEpochMs = System.currentTimeMillis();
        }
    }

    public int getAttachedSessionCount() {
        return attachedSessions.size();
    }

    public long getLastAttachedEpochMs() {
        return lastAttachedEpochMs;
    }

    private void appendStderrTail(String line) {
        stderrTail.addLast(line);
        while (stderrTail.size() > STDERR_TAIL_LIMIT) {
            stderrTail.pollFirst();
        }
    }

    private String summarizeStderrTail() {
        if (stderrTail.isEmpty()) {
            return "<empty>";
        }
        return String.join(" || ", stderrTail);
    }

    private String collectRuntimeDiagnostics() {
        List<String> command = List.of(
                "docker", "exec", "--user", "runner", containerName, "sh", "-lc",
                "echo JAVA_HOME=${JAVA_HOME:-<unset>} ; " +
                        "echo PATH=${PATH:-<unset>} ; " +
                        "which java 2>&1 ; " +
                        "java -version 2>&1 | head -n 2 ; " +
                        "ls -l /opt/java/openjdk/bin/java /opt/jdtls/bin/jdtls 2>&1"
        );

        try {
            Process proc = new ProcessBuilder(command).start();
            String output;
            try (BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
                 BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8))) {
                String stdout = out.lines().reduce("", (a, b) -> a + b + "\n");
                String stderr = err.lines().reduce("", (a, b) -> a + b + "\n");
                output = (stdout + stderr).trim();
            }
            int exit = proc.waitFor();
            if (output.isBlank()) {
                return "exit=" + exit + " output=<empty>";
            }
            return "exit=" + exit + " output=" + output.replace("\n", " || ");
        } catch (Exception e) {
            return "diagnostics-error=" + e.getMessage();
        }
    }
}
