package com.example.prog1learnapp.service.lsp;

import com.example.prog1learnapp.config.lsp.LspProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class LspPrewarmService {
    private static final Logger log = LoggerFactory.getLogger(LspPrewarmService.class);

    private final LspProperties lspProperties;
    private final LspSessionManager lspSessionManager;
    private final JdtLsContainerService containerService;
    private final Executor prewarmExecutor;
    private final Set<String> inFlightWorkspaceKeys = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Long> lastPrewarmEpochMsByWorkspaceKey = new ConcurrentHashMap<>();

    public LspPrewarmService(LspProperties lspProperties,
                             LspSessionManager lspSessionManager,
                             JdtLsContainerService containerService,
                             @Qualifier("lspPrewarmExecutor") Executor prewarmExecutor) {
        this.lspProperties = lspProperties;
        this.lspSessionManager = lspSessionManager;
        this.containerService = containerService;
        this.prewarmExecutor = prewarmExecutor;
    }

    public boolean scheduleLoginPrewarm(String principalName, String httpSessionId) {
        if (!lspProperties.isEnabled() || !lspProperties.isPrewarmOnLogin()) {
            return false;
        }
        if (principalName == null || principalName.isBlank() || httpSessionId == null || httpSessionId.isBlank()) {
            log.debug("LSP prewarm skip reason=missing-session-or-principal principal={} httpSessionId={}",
                    principalName, httpSessionId);
            return false;
        }

        String workspaceKey = lspSessionManager.workspaceKeyForSession(principalName, httpSessionId);
        if (isInCooldown(workspaceKey)) {
            log.debug("LSP prewarm skip workspaceKey={} reason=cooldown", workspaceKey);
            return false;
        }
        if (isNearSaturation()) {
            log.debug("LSP prewarm skip workspaceKey={} reason=near-saturation", workspaceKey);
            return false;
        }
        if (lspSessionManager.hasBridgeForWorkspaceKey(workspaceKey)) {
            log.debug("LSP prewarm skip workspaceKey={} reason=already-warm", workspaceKey);
            return false;
        }
        if (!inFlightWorkspaceKeys.add(workspaceKey)) {
            log.debug("LSP prewarm skip workspaceKey={} reason=in-flight", workspaceKey);
            return false;
        }

        prewarmExecutor.execute(() -> runPrewarm(workspaceKey));
        lastPrewarmEpochMsByWorkspaceKey.put(workspaceKey, System.currentTimeMillis());
        return true;
    }

    private void runPrewarm(String workspaceKey) {
        long startedNs = System.nanoTime();
        try {
            log.info("LSP prewarm start workspaceKey={} timeoutMs={}", workspaceKey, lspProperties.getPrewarmTimeoutMs());
            lspSessionManager.prewarmWorkspace(workspaceKey);
            log.info("LSP prewarm success workspaceKey={} durationMs={}", workspaceKey, elapsedMs(startedNs));
        } catch (IOException e) {
            log.warn("LSP prewarm fail workspaceKey={} durationMs={} error={}",
                    workspaceKey, elapsedMs(startedNs), e.getMessage());
        } catch (RuntimeException e) {
            log.warn("LSP prewarm fail workspaceKey={} durationMs={} error={}",
                    workspaceKey, elapsedMs(startedNs), e.getMessage());
        } finally {
            inFlightWorkspaceKeys.remove(workspaceKey);
        }
    }

    private long elapsedMs(long startedAtNs) {
        return (System.nanoTime() - startedAtNs) / 1_000_000;
    }

    private boolean isInCooldown(String workspaceKey) {
        long cooldownSec = Math.max(0L, lspProperties.getPrewarmCooldownSeconds());
        if (cooldownSec <= 0L) {
            return false;
        }
        Long last = lastPrewarmEpochMsByWorkspaceKey.get(workspaceKey);
        if (last == null) {
            return false;
        }
        return (System.currentTimeMillis() - last) < (cooldownSec * 1000L);
    }

    private boolean isNearSaturation() {
        JdtLsContainerService.SaturationSnapshot snapshot = containerService.getSaturationSnapshot();
        int max = snapshot.getMaxSessions();
        if (max <= 0) {
            return false;
        }
        int thresholdPercent = Math.max(1, Math.min(100, lspProperties.getPrewarmSkipSaturationPercent()));
        int active = snapshot.getActiveSessions();
        return (active * 100) >= (max * thresholdPercent);
    }
}
