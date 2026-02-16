package com.example.prog1learnapp.controller.lsp;

import com.example.prog1learnapp.service.lsp.JdtLsContainerService;
import com.example.prog1learnapp.service.lsp.LspSessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lsp")
public class LspHealthController {
    private final JdtLsContainerService containerService;
    private final LspSessionManager sessionManager;

    public LspHealthController(JdtLsContainerService containerService, LspSessionManager sessionManager) {
        this.containerService = containerService;
        this.sessionManager = sessionManager;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        JdtLsContainerService.SaturationSnapshot saturation = containerService.getSaturationSnapshot();
        Map<String, Object> payload = Map.of(
                "enabled", containerService.isEnabled(),
                "dockerAvailable", containerService.isDockerAvailable(),
                "imageAvailable", containerService.isImageAvailable(),
                "activeContainers", containerService.getActiveSessionCount(),
                "activeBridges", sessionManager.getActiveBridgeCount(),
                "saturation", Map.of(
                        "acquireAttempts", saturation.getAcquireAttempts(),
                        "acquireReuseCount", saturation.getAcquireReuseCount(),
                        "acquireCreateCount", saturation.getAcquireCreateCount(),
                        "acquireFailureCount", saturation.getAcquireFailureCount(),
                        "saturationRejectCount", saturation.getSaturationRejectCount(),
                        "activeSessions", saturation.getActiveSessions(),
                        "maxSessions", saturation.getMaxSessions(),
                        "lastSaturationEpochMs", saturation.getLastSaturationEpochMs(),
                        "lastSaturationSessionKey", saturation.getLastSaturationSessionKey() == null ? "" : saturation.getLastSaturationSessionKey()
                )
        );

        boolean healthy = containerService.isEnabled() && containerService.isDockerAvailable() && containerService.isImageAvailable();
        return healthy ? ResponseEntity.ok(payload) : ResponseEntity.status(503).body(payload);
    }
}
