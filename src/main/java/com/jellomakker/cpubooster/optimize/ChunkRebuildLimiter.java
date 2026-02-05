package com.jellomakker.cpubooster.optimize;

/**
 * Chunk rebuild limiter using token-bucket algorithm.
 *
 * Prevents excessive chunk rebuilds per second when frame pacing throttler
 * is active. This reduces CPU overhead by gating rebuild scheduler calls.
 *
 * Token bucket:
 * - Start with capacity = maxRebuilds (per second)
 * - Lose 1 token per rebuild request
 * - Gain tokens back at ~60/sec (as time passes)
 */
public class ChunkRebuildLimiter {
    private double tokens; // Current tokens in bucket
    private long lastRefillTime; // Last time we refilled tokens
    private int maxRebuildsPerSecondNormal = 60;
    private int maxRebuildsPerSecondThrottled = 15;
    private boolean throttleMode = false;

    public ChunkRebuildLimiter() {
        this.tokens = maxRebuildsPerSecondNormal;
        this.lastRefillTime = System.nanoTime();
    }

    /**
     * Update max rebuild rates from config.
     */
    public void updateConfig(int normalRate, int throttledRate) {
        this.maxRebuildsPerSecondNormal = normalRate;
        this.maxRebuildsPerSecondThrottled = throttledRate;
        // Refill tokens with new capacity in case we changed
        refillTokens();
    }

    /**
     * Set throttle mode (called by FramePacingThrottler when active).
     */
    public void setThrottleMode(boolean active) {
        this.throttleMode = active;
        if (active) {
            // When entering throttle mode, reset to throttled capacity
            tokens = maxRebuildsPerSecondThrottled;
        }
    }

    /**
     * Check if this rebuild request is allowed.
     * Returns true if we have tokens available.
     */
    public boolean canRebuild() {
        refillTokens();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    /**
     * Refill tokens based on elapsed time.
     * We gain tokens at a rate of (max) tokens per second.
     */
    private void refillTokens() {
        long now = System.nanoTime();
        long elapsedNanos = now - lastRefillTime;
        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;

        int maxRate = throttleMode ? maxRebuildsPerSecondThrottled : maxRebuildsPerSecondNormal;
        double tokensGained = elapsedSeconds * maxRate;

        tokens = Math.min(tokens + tokensGained, maxRate); // Cap at max capacity
        lastRefillTime = now;
    }

    /**
     * Get current token count (for debugging).
     */
    public double getTokens() {
        refillTokens();
        return tokens;
    }

    /**
     * Get max rate currently in effect.
     */
    public int getCurrentMaxRate() {
        return throttleMode ? maxRebuildsPerSecondThrottled : maxRebuildsPerSecondNormal;
    }

    /**
     * Get whether throttle mode is active.
     */
    public boolean isThrottleMode() {
        return throttleMode;
    }
}
