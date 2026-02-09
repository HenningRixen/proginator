package com.example.prog1learnapp.config.lsp;

import com.example.prog1learnapp.controller.lsp.LspWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class LspWebSocketConfig implements WebSocketConfigurer {
    private final LspWebSocketHandler lspWebSocketHandler;
    private final LspProperties lspProperties;

    public LspWebSocketConfig(LspWebSocketHandler lspWebSocketHandler, LspProperties lspProperties) {
        this.lspWebSocketHandler = lspWebSocketHandler;
        this.lspProperties = lspProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(lspWebSocketHandler, "/api/lsp/ws")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns(lspProperties.getAllowedOrigins().toArray(String[]::new));
    }
}
