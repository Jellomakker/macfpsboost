package com.jellomakker.cpubooster.optimize;

/**
 * Throttles expensive HUD/UI operations to reduce per-frame CPU work.
 * Skips rendering of certain overlays or updates every N ticks.
 */
public class HudThrottler {
    private volatile int throttleIntervalTicks = 4; // Update every 4 ticks (~200ms at 20 TPS)
    private volatile int tickCounter = 0;
    private volatile boolean shouldUpdateThisFrame = true;

    public void setThrottleInterval(int ticks) {
        this.throttleIntervalTicks = Math.max(1, ticks);
    }

    public void onTick() {
        tickCounter++;
        shouldUpdateThisFrame = (tickCounter % throttleIntervalTicks) == 0;
    }

    public boolean shouldUpdateHud() {
        return shouldUpdateThisFrame;
    }

    public void reset() {
        tickCounter = 0;
        shouldUpdateThisFrame = true;
    }

    /**
     * Returns the approximate milliseconds between HUD updates.
     *  Based on server tick rate (typically 20 TPS = 50ms per tick).
     */
    public int getUpdateIntervalMs() {
        return throttleIntervalTicks * 50; // Rough estimate
    }
}
