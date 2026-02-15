package com.example.prog1learnapp.service.lsp;

import org.springframework.stereotype.Component;

@Component
public class LspBridgeFactory {
    public LspBridge create(String containerName,
                            String workspaceKey,
                            long connectTimeoutMs,
                            long startupGraceMs) {
        return new LspBridge(containerName, workspaceKey, connectTimeoutMs, startupGraceMs);
    }
}
