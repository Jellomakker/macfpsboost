package com.jellomakker.cpubooster.profiles;

/**
 * Optimization profile presets for different use cases.
 */
public enum OptimizationProfile {
    COMPETITIVE("High FPS priority", true, true, 2, 10000, true),
    EXPLORATION("Balanced for world travel", true, true, 1, 5000, true),
    BATTERY("Battery/power saving", true, true, 1, 3000, false),
    CUSTOM("User-defined settings", false, false, 1, 5000, true);

    private final String description;
    private final boolean aggressiveThrottling;
    private final boolean adaptiveChunkThrottle;
    private final int uiThrottleTickInterval;
    private final int deferredTaskBudgetMs;
    private final boolean hudOverlayEnabled;

    OptimizationProfile(String description, boolean aggressiveThrottling, 
                        boolean adaptiveChunkThrottle, int uiThrottleTickInterval,
                        int deferredTaskBudgetMs, boolean hudOverlayEnabled) {
        this.description = description;
        this.aggressiveThrottling = aggressiveThrottling;
        this.adaptiveChunkThrottle = adaptiveChunkThrottle;
        this.uiThrottleTickInterval = uiThrottleTickInterval;
        this.deferredTaskBudgetMs = deferredTaskBudgetMs;
        this.hudOverlayEnabled = hudOverlayEnabled;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAggressiveThrottling() {
        return aggressiveThrottling;
    }

    public boolean isAdaptiveChunkThrottle() {
        return adaptiveChunkThrottle;
    }

    public int getUiThrottleTickInterval() {
        return uiThrottleTickInterval;
    }

    public int getDeferredTaskBudgetMs() {
        return deferredTaskBudgetMs;
    }

    public boolean isHudOverlayEnabled() {
        return hudOverlayEnabled;
    }

    public static OptimizationProfile fromString(String name) {
        try {
            return OptimizationProfile.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OptimizationProfile.CUSTOM;
        }
    }
}
