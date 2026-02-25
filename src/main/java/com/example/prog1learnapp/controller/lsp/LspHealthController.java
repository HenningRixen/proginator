package com.example.prog1learnapp.controller.lsp;

import com.example.prog1learnapp.service.lsp.JdtLsContainerService;
import com.example.prog1learnapp.service.lsp.LspSessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import static java.util.Map.entry;

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
                "saturation", Map.ofEntries(
                        entry("acquireAttempts", saturation.getAcquireAttempts()),
                        entry("acquireReuseCount", saturation.getAcquireReuseCount()),
                        entry("acquireCreateCount", saturation.getAcquireCreateCount()),
                        entry("acquireIdlePoolHitCount", saturation.getAcquireIdlePoolHitCount()),
                        entry("acquireIdlePoolMissCount", saturation.getAcquireIdlePoolMissCount()),
                        entry("acquireFailureCount", saturation.getAcquireFailureCount()),
                        entry("saturationRejectCount", saturation.getSaturationRejectCount()),
                        entry("activeSessions", saturation.getActiveSessions()),
                        entry("idlePoolSize", saturation.getIdlePoolSize()),
                        entry("idlePoolCreateCount", saturation.getIdlePoolCreateCount()),
                        entry("idlePoolEvictCount", saturation.getIdlePoolEvictCount()),
                        entry("maxSessions", saturation.getMaxSessions()),
                        entry("lastSaturationEpochMs", saturation.getLastSaturationEpochMs()),
                        entry("lastSaturationSessionKey", saturation.getLastSaturationSessionKey() == null ? "" : saturation.getLastSaturationSessionKey())
                )
        );

        boolean healthy = containerService.isEnabled() && containerService.isDockerAvailable() && containerService.isImageAvailable();
        return healthy ? ResponseEntity.ok(payload) : ResponseEntity.status(503).body(payload);
    }
}
