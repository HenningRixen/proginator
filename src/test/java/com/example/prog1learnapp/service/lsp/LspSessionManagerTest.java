package com.example.prog1learnapp.service.lsp;

import com.example.prog1learnapp.config.lsp.LspProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LspSessionManagerTest {

    @Test
    void open_sameWorkspace_reusesExistingBridgeAndContainer() throws Exception {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.acquireContainer(anyString())).thenReturn(Optional.of("container-1"));

        LspBridge bridge = Mockito.mock(LspBridge.class);
        AtomicInteger attached = new AtomicInteger(0);
        doAnswer(invocation -> {
            attached.incrementAndGet();
            return null;
        }).when(bridge).attachSession(any());
        doAnswer(invocation -> {
            attached.decrementAndGet();
            return null;
        }).when(bridge).detachSession(anyString());
        when(bridge.getAttachedSessionCount()).thenAnswer(inv -> attached.get());
        when(bridge.getLastAttachedEpochMs()).thenReturn(System.currentTimeMillis());

        LspBridgeFactory bridgeFactory = Mockito.mock(LspBridgeFactory.class);
        when(bridgeFactory.create(anyString(), anyString(), anyLong(), anyLong())).thenReturn(bridge);

        LspProperties properties = new LspProperties();
        properties.setIdleTtlSeconds(0);
        LspSessionManager sessionManager = new LspSessionManager(containerService, properties, bridgeFactory);

        WebSocketSession ws1 = mockWs("ws-1", "alice", "http-1");
        WebSocketSession ws2 = mockWs("ws-2", "alice", "http-1");

        sessionManager.open(ws1);
        sessionManager.open(ws2);

        verify(containerService, times(1)).acquireContainer("alice:http-1");
        verify(bridgeFactory, times(1)).create(anyString(), anyString(), anyLong(), anyLong());
        verify(bridge, times(1)).start();
        verify(bridge, times(2)).attachSession(any());
        assertEquals(1, sessionManager.getActiveBridgeCount());

        ArgumentCaptor<String> bridgeWorkspaceCaptor = ArgumentCaptor.forClass(String.class);
        verify(bridgeFactory).create(anyString(), bridgeWorkspaceCaptor.capture(), anyLong(), anyLong());
        assertEquals("alice_http-1", bridgeWorkspaceCaptor.getValue());
    }

    @Test
    void close_detachesAndCleanupRemovesIdleBridge() throws Exception {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.acquireContainer(anyString())).thenReturn(Optional.of("container-1"));

        LspBridge bridge = Mockito.mock(LspBridge.class);
        AtomicInteger attached = new AtomicInteger(0);
        doAnswer(invocation -> {
            attached.incrementAndGet();
            return null;
        }).when(bridge).attachSession(any());
        doAnswer(invocation -> {
            attached.decrementAndGet();
            return null;
        }).when(bridge).detachSession(anyString());
        when(bridge.getAttachedSessionCount()).thenAnswer(inv -> attached.get());
        when(bridge.getLastAttachedEpochMs()).thenReturn(System.currentTimeMillis());

        LspBridgeFactory bridgeFactory = Mockito.mock(LspBridgeFactory.class);
        when(bridgeFactory.create(anyString(), anyString(), anyLong(), anyLong())).thenReturn(bridge);

        LspProperties properties = new LspProperties();
        properties.setIdleTtlSeconds(0);
        LspSessionManager sessionManager = new LspSessionManager(containerService, properties, bridgeFactory);

        WebSocketSession ws1 = mockWs("ws-1", "alice", "http-1");
        sessionManager.open(ws1);
        sessionManager.close(ws1);

        verify(bridge, times(1)).detachSession("ws-1");
        verify(bridge, never()).close();

        Thread.sleep(5);
        sessionManager.cleanupIdleBridges();

        verify(bridge, times(1)).close();
        verify(containerService, times(1)).forceRemove("alice:http-1");
        assertEquals(0, sessionManager.getActiveBridgeCount());
    }

    @Test
    void open_whenContainerUnavailable_throws() {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.acquireContainer(anyString())).thenReturn(Optional.empty());

        LspBridgeFactory bridgeFactory = Mockito.mock(LspBridgeFactory.class);
        LspSessionManager sessionManager = new LspSessionManager(containerService, new LspProperties(), bridgeFactory);

        WebSocketSession ws1 = mockWs("ws-1", "alice", "http-1");
        assertThrows(IOException.class, () -> sessionManager.open(ws1));
    }

    @Test
    void open_withoutHttpSession_usesWebSocketFallbackInWorkspaceKey() throws Exception {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.acquireContainer(anyString())).thenReturn(Optional.of("container-1"));

        LspBridge bridge = Mockito.mock(LspBridge.class);
        doAnswer(invocation -> null).when(bridge).attachSession(any());
        when(bridge.getAttachedSessionCount()).thenReturn(1);
        when(bridge.getLastAttachedEpochMs()).thenReturn(System.currentTimeMillis());

        LspBridgeFactory bridgeFactory = Mockito.mock(LspBridgeFactory.class);
        when(bridgeFactory.create(anyString(), anyString(), anyLong(), anyLong())).thenReturn(bridge);

        LspSessionManager sessionManager = new LspSessionManager(containerService, new LspProperties(), bridgeFactory);
        WebSocketSession ws = mockWsWithoutHttpSession("ws-fallback-1", "alice");

        sessionManager.open(ws);

        ArgumentCaptor<String> workspaceCaptor = ArgumentCaptor.forClass(String.class);
        verify(containerService).acquireContainer(workspaceCaptor.capture());
        assertEquals("alice:ws-fallback-1", workspaceCaptor.getValue());
    }

    @Test
    void getWorkspaceUriForWebSocket_returnsStableMappedUri() throws Exception {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.acquireContainer(anyString())).thenReturn(Optional.of("container-1"));

        LspBridge bridge = Mockito.mock(LspBridge.class);
        doAnswer(invocation -> null).when(bridge).attachSession(any());
        when(bridge.getAttachedSessionCount()).thenReturn(1);
        when(bridge.getLastAttachedEpochMs()).thenReturn(System.currentTimeMillis());

        LspBridgeFactory bridgeFactory = Mockito.mock(LspBridgeFactory.class);
        when(bridgeFactory.create(anyString(), anyString(), anyLong(), anyLong())).thenReturn(bridge);

        LspSessionManager sessionManager = new LspSessionManager(containerService, new LspProperties(), bridgeFactory);
        WebSocketSession ws = mockWs("ws-1", "alice", "http-1");

        sessionManager.open(ws);
        assertEquals(
                Optional.of("file:///tmp/workspaces/alice_http-1/project"),
                sessionManager.getWorkspaceUriForWebSocket("ws-1")
        );

        sessionManager.close(ws);
        assertEquals(Optional.empty(), sessionManager.getWorkspaceUriForWebSocket("ws-1"));
    }

    private WebSocketSession mockWs(String wsId, String principalName, String httpSessionId) {
        WebSocketSession ws = Mockito.mock(WebSocketSession.class);
        Principal principal = () -> principalName;
        when(ws.getId()).thenReturn(wsId);
        when(ws.getPrincipal()).thenReturn(principal);
        when(ws.getAttributes()).thenReturn(
                Map.of(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME, httpSessionId)
        );
        return ws;
    }

    private WebSocketSession mockWsWithoutHttpSession(String wsId, String principalName) {
        WebSocketSession ws = Mockito.mock(WebSocketSession.class);
        Principal principal = () -> principalName;
        when(ws.getId()).thenReturn(wsId);
        when(ws.getPrincipal()).thenReturn(principal);
        when(ws.getAttributes()).thenReturn(Map.of());
        return ws;
    }
}
