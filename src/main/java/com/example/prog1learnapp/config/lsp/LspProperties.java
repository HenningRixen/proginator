package com.example.prog1learnapp.config.lsp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.lsp")
public class LspProperties {
    private boolean enabled = false;
    private String image = "proginator-jdtls";
    private long connectTimeoutMs = 15000;
    private long startupGraceMs = 750;
    private boolean prewarmOnLogin = false;
    private long prewarmTimeoutMs = 8000;
    private long prewarmCooldownSeconds = 120;
    private int prewarmSkipSaturationPercent = 90;
    private long idleTtlSeconds = 300;
    private int minIdleContainers = 0;
    private int maxSessions = 50;
    private int memoryMb = 512;
    private String cpus = "1.0";
    private int maxMessageBytes = 1_000_000;
    private long cleanupIntervalMs = 30000;
    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:*", "https://localhost:*"));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public long getStartupGraceMs() {
        return startupGraceMs;
    }

    public void setStartupGraceMs(long startupGraceMs) {
        this.startupGraceMs = startupGraceMs;
    }

    public boolean isPrewarmOnLogin() {
        return prewarmOnLogin;
    }

    public void setPrewarmOnLogin(boolean prewarmOnLogin) {
        this.prewarmOnLogin = prewarmOnLogin;
    }

    public long getPrewarmTimeoutMs() {
        return prewarmTimeoutMs;
    }

    public void setPrewarmTimeoutMs(long prewarmTimeoutMs) {
        this.prewarmTimeoutMs = prewarmTimeoutMs;
    }

    public long getPrewarmCooldownSeconds() {
        return prewarmCooldownSeconds;
    }

    public void setPrewarmCooldownSeconds(long prewarmCooldownSeconds) {
        this.prewarmCooldownSeconds = prewarmCooldownSeconds;
    }

    public int getPrewarmSkipSaturationPercent() {
        return prewarmSkipSaturationPercent;
    }

    public void setPrewarmSkipSaturationPercent(int prewarmSkipSaturationPercent) {
        this.prewarmSkipSaturationPercent = prewarmSkipSaturationPercent;
    }

    public long getIdleTtlSeconds() {
        return idleTtlSeconds;
    }

    public void setIdleTtlSeconds(long idleTtlSeconds) {
        this.idleTtlSeconds = idleTtlSeconds;
    }

    public int getMinIdleContainers() {
        return minIdleContainers;
    }

    public void setMinIdleContainers(int minIdleContainers) {
        this.minIdleContainers = minIdleContainers;
    }

    public int getMaxSessions() {
        return maxSessions;
    }

    public void setMaxSessions(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    public int getMemoryMb() {
        return memoryMb;
    }

    public void setMemoryMb(int memoryMb) {
        this.memoryMb = memoryMb;
    }

    public String getCpus() {
        return cpus;
    }

    public void setCpus(String cpus) {
        this.cpus = cpus;
    }

    public int getMaxMessageBytes() {
        return maxMessageBytes;
    }

    public void setMaxMessageBytes(int maxMessageBytes) {
        this.maxMessageBytes = maxMessageBytes;
    }

    public long getCleanupIntervalMs() {
        return cleanupIntervalMs;
    }

    public void setCleanupIntervalMs(long cleanupIntervalMs) {
        this.cleanupIntervalMs = cleanupIntervalMs;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
