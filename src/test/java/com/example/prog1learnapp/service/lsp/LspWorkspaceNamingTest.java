package com.example.prog1learnapp.service.lsp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LspWorkspaceNamingTest {

    @Test
    void workspaceKey_prefersHttpSessionIdWhenPresent() {
        assertEquals("alice:http-1", LspWorkspaceNaming.workspaceKey("alice", "http-1", "ws-1"));
    }

    @Test
    void workspaceKey_fallsBackToWebSocketId() {
        assertEquals("alice:ws-1", LspWorkspaceNaming.workspaceKey("alice", null, "ws-1"));
    }

    @Test
    void workspaceUri_usesSanitizedWorkspaceKey() {
        assertEquals("file:///tmp/workspaces/alice_http-1/project", LspWorkspaceNaming.workspaceUri("alice:http-1"));
    }
}
