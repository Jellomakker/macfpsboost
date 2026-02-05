package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;

/**
 * Adaptively throttles chunk rebuilds based on frame time.
 * If frames are stuttering, limits chunk work; if smooth, allows more.
 */
public class ChunkRebuildThrottler {
    private volatile boolean adaptiveEnabled = true;
    private volatile int maxChunkBuildsPerTick = 8;
    private volatile int currentChunkBudget = 8;
    private volatile double frameTimeThresholdMs = 30.0; // Stutter threshold
    private volatile double recoveryThresholdMs = 16.0; // Frame time target
    private int ticksSinceAdjustment = 0;

    public void setAdaptiveEnabled(boolean enabled) {
        this.adaptiveEnabled = enabled;
    }

    public void setMaxChunkBuildsPerTick(int max) {
        this.maxChunkBuildsPerTick = Math.max(1, max);
        this.currentChunkBudget = max;
    }

    public void onFrameTime(double frameTimeMs) {
        if (!adaptiveEnabled) {
            currentChunkBudget = maxChunkBuildsPerTick;
            return;
        }

        ticksSinceAdjustment++;

        // Adjust budget every ~10 ticks
        if (ticksSinceAdjustment >= 10) {
            if (frameTimeMs > frameTimeThresholdMs) {
                // Stutter detected: reduce chunk work
                currentChunkBudget = Math.max(1, currentChunkBudget - 1);
                CpuBoosterMod.LOGGER.debug("Chunk throttle: reduced budget to {} (frame spike: {:.1f}ms)",
                        currentChunkBudget, frameTimeMs);
            } else if (frameTimeMs < recoveryThresholdMs && currentChunkBudget < maxChunkBuildsPerTick) {
                // Smooth frames: gradually increase chunk work
                currentChunkBudget = Math.min(maxChunkBuildsPerTick, currentChunkBudget + 1);
                CpuBoosterMod.LOGGER.debug("Chunk throttle: increased budget to {} (recovery)", currentChunkBudget);
            }
            ticksSinceAdjustment = 0;
        }
    }

    public int getChunkBudget() {
        return currentChunkBudget;
    }

    public boolean isAdaptiveEnabled() {
        return adaptiveEnabled;
    }
}
