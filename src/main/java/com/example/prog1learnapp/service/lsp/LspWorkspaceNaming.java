package com.example.prog1learnapp.service.lsp;

import java.util.Objects;

public final class LspWorkspaceNaming {

    private LspWorkspaceNaming() {
    }

    public static String workspaceKey(String principalName, String httpSessionId, String webSocketId) {
        String resolvedPrincipal = isBlank(principalName) ? "anonymous" : principalName;
        if (!isBlank(httpSessionId)) {
            return resolvedPrincipal + ":" + httpSessionId;
        }
        return resolvedPrincipal + ":" + Objects.requireNonNullElse(webSocketId, "unknown");
    }

    public static String sanitizeForPath(String value) {
        return value == null ? "workspace" : value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    public static String workspaceUri(String workspaceKey) {
        return "file:///tmp/workspaces/" + sanitizeForPath(workspaceKey) + "/project";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
