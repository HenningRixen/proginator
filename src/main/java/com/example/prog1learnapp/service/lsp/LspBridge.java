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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LspBridge {
    private static final Logger log = LoggerFactory.getLogger(LspBridge.class);

    private final String containerName;
    private final String workspaceKey;
    private final long connectTimeoutMs;
    private final WebSocketSession webSocketSession;

    private Process process;
    private OutputStream lspInput;
    private ExecutorService ioExecutor;

    public LspBridge(String containerName,
                     String workspaceKey,
                     long connectTimeoutMs,
                     WebSocketSession webSocketSession) {
        this.containerName = containerName;
        this.workspaceKey = workspaceKey;
        this.connectTimeoutMs = connectTimeoutMs;
        this.webSocketSession = webSocketSession;
    }

    public synchronized void start() throws IOException {
        if (process != null && process.isAlive()) {
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
        lspInput = process.getOutputStream();
        ioExecutor = Executors.newFixedThreadPool(2);

        ioExecutor.submit(this::forwardLspMessagesToClient);
        ioExecutor.submit(this::drainStderrLogs);

        waitForEarlyFailure();
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
    }

    private void forwardLspMessagesToClient() {
        try {
            InputStream stdout = process.getInputStream();
            while (process.isAlive() && webSocketSession.isOpen()) {
                String message = LspJsonRpcFraming.readMessage(stdout);
                if (message == null) {
                    break;
                }
                synchronized (webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(message));
                }
            }
        } catch (Exception e) {
            log.warn("LSP bridge output forwarding failed: {}", e.getMessage());
        } finally {
            if (webSocketSession.isOpen()) {
                try {
                    webSocketSession.close(CloseStatus.SERVER_ERROR);
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
            }
        } catch (IOException e) {
            log.debug("LSP stderr reader stopped for {}: {}", containerName, e.getMessage());
        }
    }

    private void waitForEarlyFailure() throws IOException {
        long deadline = System.currentTimeMillis() + connectTimeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (process == null || !process.isAlive()) {
                throw new IOException("JDT LS failed to start in container " + containerName);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for JDT LS startup", e);
            }
        }
    }
}
