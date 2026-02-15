package com.example.prog1learnapp.service.lsp;

import com.example.prog1learnapp.config.lsp.LspProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LspSessionManager {
    private static final Logger log = LoggerFactory.getLogger(LspSessionManager.class);

    private final JdtLsContainerService containerService;
    private final LspProperties lspProperties;
    private final Map<String, LspBridge> bridgesByWebSocketId = new ConcurrentHashMap<>();
    private final Map<String, String> workspaceKeyByWebSocketId = new ConcurrentHashMap<>();

    public LspSessionManager(JdtLsContainerService containerService, LspProperties lspProperties) {
        this.containerService = containerService;
        this.lspProperties = lspProperties;
    }

    public void open(WebSocketSession webSocketSession) throws IOException {
        long openStartedNs = System.nanoTime();
        String wsId = webSocketSession.getId();
        String workspaceKey = resolveWorkspaceKey(webSocketSession);

        long acquireStartedNs = System.nanoTime();
        Optional<String> containerName = containerService.acquireContainer(workspaceKey);
        long acquireDurationMs = elapsedMs(acquireStartedNs);
        if (containerName.isEmpty()) {
            log.warn("LSP open failed wsId={} workspaceKey={} stage=acquireContainer durationMs={}", wsId, workspaceKey, acquireDurationMs);
            throw new IOException("No available LSP backend container");
        }

        LspBridge bridge = new LspBridge(
                containerName.get(),
                sanitize(workspaceKey),
                lspProperties.getConnectTimeoutMs(),
                lspProperties.getStartupGraceMs(),
                webSocketSession
        );

        long bridgeStartNs = System.nanoTime();
        try {
            bridge.start();
        } catch (IOException e) {
            containerService.forceRemove(workspaceKey);
            log.warn("LSP open failed wsId={} workspaceKey={} stage=bridgeStart acquireMs={} bridgeStartMs={} totalMs={} error={}",
                    wsId,
                    workspaceKey,
                    acquireDurationMs,
                    elapsedMs(bridgeStartNs),
                    elapsedMs(openStartedNs),
                    e.getMessage());
            throw e;
        }
        long bridgeStartMs = elapsedMs(bridgeStartNs);

        bridgesByWebSocketId.put(webSocketSession.getId(), bridge);
        workspaceKeyByWebSocketId.put(webSocketSession.getId(), workspaceKey);
        log.debug("LSP websocket session opened wsId={} workspaceKey={} container={} acquireMs={} bridgeStartMs={} totalOpenMs={}",
                wsId,
                workspaceKey,
                containerName.get(),
                acquireDurationMs,
                bridgeStartMs,
                elapsedMs(openStartedNs));
    }

    public void forwardClientMessage(WebSocketSession webSocketSession, String payload) throws IOException {
        if (payload != null && payload.getBytes(StandardCharsets.UTF_8).length > lspProperties.getMaxMessageBytes()) {
            throw new IOException("LSP payload exceeds max size");
        }

        LspBridge bridge = bridgesByWebSocketId.get(webSocketSession.getId());
        if (bridge == null) {
            throw new IOException("No active LSP bridge for websocket session");
        }

        bridge.sendClientMessage(payload);
    }

    public void close(WebSocketSession webSocketSession) {
        String webSocketId = webSocketSession.getId();

        LspBridge bridge = bridgesByWebSocketId.remove(webSocketId);
        if (bridge != null) {
            bridge.close();
        }

        String workspaceKey = workspaceKeyByWebSocketId.remove(webSocketId);
        if (workspaceKey != null) {
            containerService.releaseContainer(workspaceKey);
        }

        log.debug("LSP websocket session {} closed", webSocketId);
    }

    public int getActiveBridgeCount() {
        return bridgesByWebSocketId.size();
    }

    private String resolveWorkspaceKey(WebSocketSession webSocketSession) {
        Object httpSessionId = webSocketSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        Principal principal = webSocketSession.getPrincipal();
        String principalName = principal != null ? principal.getName() : "anonymous";

        if (httpSessionId != null) {
            return principalName + ":" + httpSessionId;
        }

        return principalName + ":" + webSocketSession.getId();
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private long elapsedMs(long startedAtNs) {
        return (System.nanoTime() - startedAtNs) / 1_000_000;
    }
}
