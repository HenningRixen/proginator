package com.example.prog1learnapp.controller.lsp;

import com.example.prog1learnapp.service.lsp.JdtLsContainerService;
import com.example.prog1learnapp.service.lsp.LspSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class LspWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(LspWebSocketHandler.class);

    private final LspSessionManager sessionManager;
    private final JdtLsContainerService containerService;

    public LspWebSocketHandler(LspSessionManager sessionManager, JdtLsContainerService containerService) {
        this.sessionManager = sessionManager;
        this.containerService = containerService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        long startedAtNs = System.nanoTime();
        String wsId = session.getId();

        if (!containerService.isEnabled()) {
            log.debug("LSP websocket connect rejected wsId={} reason=disabled durationMs={}", wsId, elapsedMs(startedAtNs));
            session.close(CloseStatus.POLICY_VIOLATION.withReason("LSP disabled"));
            return;
        }

        if (session.getPrincipal() == null) {
            log.debug("LSP websocket connect rejected wsId={} reason=unauthenticated durationMs={}", wsId, elapsedMs(startedAtNs));
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication required"));
            return;
        }

        try {
            sessionManager.open(session);
            log.debug("LSP websocket connect established wsId={} durationMs={}", wsId, elapsedMs(startedAtNs));
        } catch (IOException e) {
            log.warn("Failed to establish LSP bridge wsId={} durationMs={} error={}", wsId, elapsedMs(startedAtNs), e.getMessage());
            session.close(CloseStatus.SERVER_ERROR.withReason("LSP backend unavailable"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            sessionManager.forwardClientMessage(session, message.getPayload());
        } catch (IOException e) {
            log.warn("LSP message forwarding failed: {}", e.getMessage());
            String error = e.getMessage();
            if (error != null && error.contains("payload exceeds max size")) {
                session.close(CloseStatus.BAD_DATA.withReason("Invalid or oversized LSP payload"));
            } else {
                session.close(CloseStatus.SERVER_ERROR.withReason("LSP backend unavailable"));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("LSP websocket transport error: {}", exception.getMessage());
        sessionManager.close(session);
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.close(session);
    }

    private long elapsedMs(long startedAtNs) {
        return (System.nanoTime() - startedAtNs) / 1_000_000;
    }
}
