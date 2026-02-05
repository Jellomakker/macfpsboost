package com.jellomakker.cpubooster.config;

import com.jellomakker.cpubooster.profiles.OptimizationProfile;
import java.util.HashMap;
import java.util.Map;

public class CpuBoosterConfig {
    // Core toggle
    public boolean enabled = true;

    // Optimization profile (COMPETITIVE, EXPLORATION, BATTERY, CUSTOM)
    public String optimizationProfile = "CUSTOM";

    // Debug overlay
    public boolean debugOverlayEnabled = false;

    // Stutter smoother
    public boolean stutterSmootherEnabled = true;
    public double spikeThresholdMs = 28.0;
    public long smoothingWindowMs = 1500L;
    public boolean gcHintEnabled = false;
    public long gcMinIntervalMs = 30000L;

    // Adaptive chunk throttling
    public boolean adaptiveChunkThrottle = true;
    public int maxChunkBuildsPerTick = 8;

    // HUD/UI throttling (ticks between updates)
    public int hudThrottleIntervalTicks = 4;

    // Deferred task system
    public int deferredTaskBudgetMs = 5000;

    // Performance metrics collection
    public boolean metricsEnabled = true;
    // Adaptive frame pacing throttler
    public boolean adaptiveThrottlingEnabled = true;
    public double frameTimeTargetMs = 16.6;
    public long throttleCooldownMs = 750;

    // Chunk rebuild limiter (token-bucket)
    public int maxChunkRebuildsPerSecondNormal = 60;
    public int maxChunkRebuildsPerSecondThrottled = 15;

    // Debug logging
    public boolean debugLogging = false;

    // Memory pressure / GC optimization
    public boolean memoryPressureEnabled = true;
    public int memoryGrowthThresholdMB = 128;
    public long memoryCooldownMs = 2000;

    // Cache cleanup
    public boolean enableCacheCleanup = true;
    public int cacheCleanupIntervalSeconds = 30;

    // Block entity update limiter
    public boolean blockEntityUpdateLimiterEnabled = true;
    public int maxBlockEntityUpdatesPerTick = 50;

    // State change minimization
    public boolean stateChangeThresholdEnabled = true;
    public double cameraDeltaThreshold = 0.01; // squared distance
    public double rotationDeltaThreshold = 0.25; // degrees

    // ===== 10 MAJOR OPTIMIZATION FEATURES =====

    // FEATURE 1: Frame-time variance adaptive optimizer
    public boolean enableFrameTimeVarianceOptimizer = true;
    public double frameTimeVarianceSpikeThreshold = 5.0; // ms above running average
    public int deferralDurationTicks = 2;

    // FEATURE 2: Smart chunk rebuild throttling
    public boolean enableSmartChunkRebuild = true;
    public int smartChunkMaxRebuildsPerTick = 4;
    public boolean adaptiveChunkBudgetEnabled = true;
    public int maxChunkQueueSize = 100;

    // FEATURE 3: Render state deduplication
    public boolean enableRenderStateDedup = true;

    // FEATURE 4: Invisible entity freezing
    public boolean enableEntityFreezing = true;
    public double entityFreezeDistance = 64.0; // blocks from player
    public double entitySafetyRadius = 24.0; // never freeze within this distance
    public boolean freezeHostileMobs = false; // conservative default

    // FEATURE 5: GPU-friendly render batching
    public boolean enableGPUBatching = true;

    // FEATURE 6: Allocation pressure reduction / object pooling
    public boolean enableAllocationPooling = true;
    public int allocationPoolSize = 256;

    // FEATURE 7: Input-render sync decoupling
    public boolean enableInputRenderDecoupling = true;

    // FEATURE 8: Dynamic internal resolution scaling
    public boolean enableResolutionScaling = true;
    public double resolutionScaleMin = 0.85;
    public double resolutionScaleMax = 1.0;

    // FEATURE 9: Block entity cold storage (idle caching)
    public boolean enableBlockEntityColdStorage = true;
    public int blockEntityIdleThreshold = 60; // ticks before cache

    // FEATURE 10: Multi-profile adaptive optimizer
    public boolean enableProfiles = true;
    public String profileMode = "AUTO"; // "AUTO" or "MANUAL"
    public String manualProfile = "EXPLORATION"; // EXPLORATION, PVP, BUILDING, AFK

    // Diagnostics
    public boolean diagnosticsEnabled = true;

    // Performance patches (each can be toggled independently)
    // If not present in config, defaults to all enabled
    public Map<String, Boolean> patches = new HashMap<>();

    public CpuBoosterConfig() {
        // Default patch config: all enabled
        patches.put("framePacing", true);
        patches.put("uboPrealloc", true);
        patches.put("allocationReducer", true);
    }

    public boolean isPatchEnabled(String patchId) {
        return patches.getOrDefault(patchId, true);
    }

    public void setPatchEnabled(String patchId, boolean enabled) {
        patches.put(patchId, enabled);
    }

    public void applyProfile(OptimizationProfile profile) {
        if (profile == OptimizationProfile.CUSTOM) {
            return; // Skip for CUSTOM
        }

        this.optimizationProfile = profile.name();
        this.adaptiveChunkThrottle = profile.isAdaptiveChunkThrottle();
        this.hudThrottleIntervalTicks = profile.getUiThrottleTickInterval();
        this.deferredTaskBudgetMs = profile.getDeferredTaskBudgetMs();
        this.debugOverlayEnabled = profile.isHudOverlayEnabled();
    }
}
