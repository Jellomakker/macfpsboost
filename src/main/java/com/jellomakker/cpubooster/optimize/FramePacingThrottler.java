package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;

/**
 * Frame pacing system inspired by VulkanMod's frame queue concepts.
 * 
 * Uses exponential moving average (EMA) to smooth jitter and detect spikes.
 * When spikes are detected, triggers adaptive throttling mode to reduce CPU overhead.
 */
public class FramePacingThrottler {
    private final double emaAlpha = 0.3; // Smoothing factor for EMA (30% new, 70% old)
    private double frameTimeEma = 16.6; // Initial estimate: 60 FPS
    private long lastFrameTimeMs = System.currentTimeMillis();
    private long throttleModeEndTime = 0;
    private int spikesDetected = 0;

    private double frameTimeTargetMs = 16.6;
    private double spikeThresholdMs = 28.0;
    private long throttleCooldownMs = 750;

    public FramePacingThrottler() {
    }

    /**
     * Update configuration parameters from config at runtime.
     */
    public void updateConfig(double targetMs, double spikeThresholdMs, long cooldownMs) {
        this.frameTimeTargetMs = targetMs;
        this.spikeThresholdMs = spikeThresholdMs;
        this.throttleCooldownMs = cooldownMs;
    }

    /**
     * Record a frame time and update EMA.
     * Returns true if throttling is currently active due to spike detection.
     */
    public boolean onFrameTime(double frameTimeMs) {
        // Update EMA
        frameTimeEma = (emaAlpha * frameTimeMs) + ((1.0 - emaAlpha) * frameTimeEma);

        // Detect spike
        boolean isSpike = frameTimeMs > spikeThresholdMs;
        if (isSpike) {
            spikesDetected++;
            throttleModeEndTime = System.currentTimeMillis() + throttleCooldownMs;
            CpuBoosterMod.LOGGER.debug("Frame spike detected: {:.1f}ms (threshold: {:.1f}ms), entering throttle mode for {}ms",
                    frameTimeMs, spikeThresholdMs, throttleCooldownMs);
        }

        return isThrottlingActive();
    }

    /**
     * Check if we're currently in throttle mode due to recent spike(s).
     */
    public boolean isThrottlingActive() {
        return System.currentTimeMillis() < throttleModeEndTime;
    }

    /**
     * Get the exponential moving average frame time.
     */
    public double getFrameTimeEma() {
        return frameTimeEma;
    }

    /**
     * Get the number of spikes detected so far.
     */
    public int getSpikesDetected() {
        return spikesDetected;
    }

    /**
     * Get time remaining in throttle cooldown (ms), or 0 if not throttling.
     */
    public long getThrottleTimeRemaining() {
        long remaining = throttleModeEndTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Reset spike counter (call periodically, e.g., every minute).
     */
    public void resetSpikeCounter() {
        spikesDetected = 0;
    }
}
