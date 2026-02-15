package com.example.prog1learnapp.service.lsp;

import com.example.prog1learnapp.config.lsp.LspProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LspSessionManager {
    private static final Logger log = LoggerFactory.getLogger(LspSessionManager.class);

    private final JdtLsContainerService containerService;
    private final LspProperties lspProperties;
    private final LspBridgeFactory lspBridgeFactory;

    private final Map<String, LspBridge> bridgesByWebSocketId = new ConcurrentHashMap<>();
    private final Map<String, String> workspaceKeyByWebSocketId = new ConcurrentHashMap<>();
    private final Map<String, WorkspaceBridgeRecord> bridgeByWorkspaceKey = new ConcurrentHashMap<>();

    public LspSessionManager(JdtLsContainerService containerService,
                             LspProperties lspProperties,
                             LspBridgeFactory lspBridgeFactory) {
        this.containerService = containerService;
        this.lspProperties = lspProperties;
        this.lspBridgeFactory = lspBridgeFactory;
    }

    public void open(WebSocketSession webSocketSession) throws IOException {
        long openStartedNs = System.nanoTime();
        String wsId = webSocketSession.getId();
        String workspaceKey = resolveWorkspaceKey(webSocketSession);

        WorkspaceBridgeRecord existing = bridgeByWorkspaceKey.get(workspaceKey);
        if (existing != null) {
            existing.bridge.attachSession(webSocketSession);
            existing.lastUsedEpochMs = Instant.now().toEpochMilli();
            bridgesByWebSocketId.put(wsId, existing.bridge);
            workspaceKeyByWebSocketId.put(wsId, workspaceKey);
            log.debug("LSP websocket session attached wsId={} workspaceKey={} container={} attachedSessions={} totalOpenMs={}",
                    wsId,
                    workspaceKey,
                    existing.containerName,
                    existing.bridge.getAttachedSessionCount(),
                    elapsedMs(openStartedNs));
            return;
        }

        long acquireStartedNs = System.nanoTime();
        Optional<String> containerName = containerService.acquireContainer(workspaceKey);
        long acquireDurationMs = elapsedMs(acquireStartedNs);
        if (containerName.isEmpty()) {
            log.warn("LSP open failed wsId={} workspaceKey={} stage=acquireContainer durationMs={}", wsId, workspaceKey, acquireDurationMs);
            throw new IOException("No available LSP backend container");
        }

        LspBridge bridge = lspBridgeFactory.create(
                containerName.get(),
                sanitize(workspaceKey),
                lspProperties.getConnectTimeoutMs(),
                lspProperties.getStartupGraceMs()
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

        bridge.attachSession(webSocketSession);
        bridgeByWorkspaceKey.put(workspaceKey, new WorkspaceBridgeRecord(containerName.get(), bridge));
        bridgesByWebSocketId.put(wsId, bridge);
        workspaceKeyByWebSocketId.put(wsId, workspaceKey);
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
        String workspaceKey = workspaceKeyByWebSocketId.remove(webSocketId);
        LspBridge bridge = bridgesByWebSocketId.remove(webSocketId);

        if (bridge != null) {
            bridge.detachSession(webSocketId);
        }

        if (workspaceKey != null) {
            WorkspaceBridgeRecord record = bridgeByWorkspaceKey.get(workspaceKey);
            if (record != null) {
                record.lastUsedEpochMs = Instant.now().toEpochMilli();
            }
        }

        log.debug("LSP websocket session {} detached", webSocketId);
    }

    public int getActiveBridgeCount() {
        return bridgeByWorkspaceKey.size();
    }

    @Scheduled(fixedDelayString = "${app.lsp.cleanup-interval-ms:30000}")
    public void cleanupIdleBridges() {
        if (bridgeByWorkspaceKey.isEmpty()) {
            return;
        }

        long now = Instant.now().toEpochMilli();
        long ttlMs = lspProperties.getIdleTtlSeconds() * 1000;
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, WorkspaceBridgeRecord> entry : bridgeByWorkspaceKey.entrySet()) {
            WorkspaceBridgeRecord record = entry.getValue();
            if (record.bridge.getAttachedSessionCount() == 0 && now - record.lastUsedEpochMs > ttlMs) {
                toRemove.add(entry.getKey());
            }
        }

        for (String workspaceKey : toRemove) {
            WorkspaceBridgeRecord record = bridgeByWorkspaceKey.remove(workspaceKey);
            if (record == null) {
                continue;
            }
            record.bridge.close();
            containerService.forceRemove(workspaceKey);
            log.debug("Removed idle LSP bridge workspaceKey={} container={}", workspaceKey, record.containerName);
        }
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

    private static final class WorkspaceBridgeRecord {
        private final String containerName;
        private final LspBridge bridge;
        private volatile long lastUsedEpochMs = Instant.now().toEpochMilli();

        private WorkspaceBridgeRecord(String containerName, LspBridge bridge) {
            this.containerName = containerName;
            this.bridge = bridge;
        }
    }
}
