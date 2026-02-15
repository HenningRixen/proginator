package com.example.prog1learnapp.controller.lsp;

import com.example.prog1learnapp.config.lsp.LspProperties;
import com.example.prog1learnapp.service.lsp.JdtLsContainerService;
import com.example.prog1learnapp.service.lsp.LspBridgeFactory;
import com.example.prog1learnapp.service.lsp.LspSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class LspWebSocketObservabilityIntegrationTest {

    @Test
    void websocketHandler_closesConnectionWhenDisabled() throws Exception {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        LspSessionManager sessionManager = Mockito.mock(LspSessionManager.class);
        when(containerService.isEnabled()).thenReturn(false);

        WebSocketSession webSocketSession = Mockito.mock(WebSocketSession.class);
        when(webSocketSession.getId()).thenReturn("ws-disabled-1");

        LspWebSocketHandler handler = new LspWebSocketHandler(sessionManager, containerService);
        handler.afterConnectionEstablished(webSocketSession);

        verify(webSocketSession).close(org.springframework.web.socket.CloseStatus.POLICY_VIOLATION.withReason("LSP disabled"));
    }

    @Test
    void sessionManager_logsWorkspaceKeyOnAcquireFailure(CapturedOutput output) {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.acquireContainer(anyString())).thenReturn(Optional.empty());

        LspProperties lspProperties = new LspProperties();
        LspSessionManager sessionManager = new LspSessionManager(containerService, lspProperties, new LspBridgeFactory());

        WebSocketSession webSocketSession = Mockito.mock(WebSocketSession.class);
        when(webSocketSession.getId()).thenReturn("ws-open-1");
        Principal principal = () -> "alice";
        when(webSocketSession.getPrincipal()).thenReturn(principal);
        when(webSocketSession.getAttributes()).thenReturn(
                Map.of(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME, "http-session-1")
        );

        assertThrows(IOException.class, () -> sessionManager.open(webSocketSession));

        org.junit.jupiter.api.Assertions.assertTrue(output.getOut().contains("workspaceKey=alice:http-session-1"));
        org.junit.jupiter.api.Assertions.assertTrue(output.getOut().contains("stage=acquireContainer"));
        org.junit.jupiter.api.Assertions.assertTrue(output.getOut().contains("wsId=ws-open-1"));
    }

    @Test
    void websocketHandler_logsBridgeStartFailureReason(CapturedOutput output) throws Exception {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.isEnabled()).thenReturn(true);
        when(containerService.acquireContainer(anyString())).thenReturn(Optional.of("phase0-non-existing-container"));
        LspProperties lspProperties = new LspProperties();
        lspProperties.setConnectTimeoutMs(500);
        LspSessionManager sessionManager = new LspSessionManager(containerService, lspProperties, new LspBridgeFactory());

        LspWebSocketHandler handler = new LspWebSocketHandler(sessionManager, containerService);
        WebSocketSession webSocketSession = Mockito.mock(WebSocketSession.class);
        when(webSocketSession.getId()).thenReturn("ws-bridge-fail-1");
        when(webSocketSession.getPrincipal()).thenReturn(() -> "alice");
        when(webSocketSession.getAttributes()).thenReturn(
                Map.of(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME, "http-session-2")
        );

        handler.afterConnectionEstablished(webSocketSession);

        verify(webSocketSession).close(any());
        org.junit.jupiter.api.Assertions.assertTrue(output.getOut().contains("stage=bridgeStart"));
        org.junit.jupiter.api.Assertions.assertTrue(output.getOut().contains("runtimeDiagnostics="));
        org.junit.jupiter.api.Assertions.assertTrue(output.getOut().contains("wsId=ws-bridge-fail-1"));
    }

    @Test
    void websocketHandler_closesWithServerErrorWhenSessionOpenFails() throws Exception {
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        LspSessionManager sessionManager = Mockito.mock(LspSessionManager.class);
        when(containerService.isEnabled()).thenReturn(true);
        Mockito.doThrow(new IOException("backend unavailable")).when(sessionManager).open(any());

        WebSocketSession webSocketSession = Mockito.mock(WebSocketSession.class);
        when(webSocketSession.getId()).thenReturn("ws-fail-1");
        when(webSocketSession.getPrincipal()).thenReturn(() -> "alice");

        LspWebSocketHandler handler = new LspWebSocketHandler(sessionManager, containerService);
        handler.afterConnectionEstablished(webSocketSession);

        verify(webSocketSession).close(CloseStatus.SERVER_ERROR.withReason("LSP backend unavailable"));
    }
}
