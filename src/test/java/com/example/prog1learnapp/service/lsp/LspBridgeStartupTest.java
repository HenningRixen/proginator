package com.example.prog1learnapp.service.lsp;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class LspBridgeStartupTest {

    @Test
    void waitForEarlyFailure_processAliveAfterGrace_returnsQuickly() throws Exception {
        WebSocketSession ws = mock(WebSocketSession.class);
        LspBridge bridge = new LspBridge("dummy-container", "workspace", 15000, 200, ws);
        Process aliveProcess = new FakeProcess(true);

        long start = System.currentTimeMillis();
        bridge.waitForEarlyFailure(aliveProcess, 150);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 1000, "Startup grace wait should finish quickly without full timeout blocking");
    }

    @Test
    void waitForEarlyFailure_processExitsImmediately_failsFast() {
        WebSocketSession ws = mock(WebSocketSession.class);
        LspBridge bridge = new LspBridge("dummy-container", "workspace", 15000, 200, ws);
        Process deadProcess = new FakeProcess(false);

        IOException ex = assertThrows(IOException.class, () -> bridge.waitForEarlyFailure(deadProcess, 300));
        assertTrue(ex.getMessage().contains("JDT LS failed to start in container"));
    }

    private static final class FakeProcess extends Process {
        private final boolean alive;

        private FakeProcess(boolean alive) {
            this.alive = alive;
        }

        @Override
        public OutputStream getOutputStream() {
            return OutputStream.nullOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public InputStream getErrorStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public int waitFor() {
            return alive ? 0 : 1;
        }

        @Override
        public boolean waitFor(long timeout, java.util.concurrent.TimeUnit unit) {
            return !alive;
        }

        @Override
        public int exitValue() {
            return alive ? 0 : 1;
        }

        @Override
        public void destroy() {
        }

        @Override
        public Process destroyForcibly() {
            return this;
        }

        @Override
        public boolean isAlive() {
            return alive;
        }
    }
}
