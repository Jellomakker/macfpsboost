package com.jellomakker.cpubooster.block;

import com.jellomakker.cpubooster.CpuBoosterMod;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple per-tick budget limiter for client-side block-entity helper updates.
 * Other parts of this mod should call `requestUpdate()` before performing
 * optional visual-only updates.
 */
public class BlockEntityUpdateLimiter {
    private final AtomicInteger budget = new AtomicInteger(0);
    private volatile int maxPerTick = 50;
    private volatile boolean enabled = true;

    public BlockEntityUpdateLimiter() {}

    public void updateConfig(int maxPerTick, boolean enabled) {
        this.maxPerTick = maxPerTick;
        this.enabled = enabled;
    }

    /**
     * Reset budget at tick start.
     */
    public void resetBudget() {
        budget.set(maxPerTick);
    }

    /**
     * Request permission to run an optional block entity update.
     * Returns true if allowed, false otherwise.
     */
    public boolean requestUpdate() {
        if (!enabled) return true; // if disabled, always allow
        while (true) {
            int cur = budget.get();
            if (cur <= 0) return false;
            if (budget.compareAndSet(cur, cur - 1)) return true;
        }
    }

    public int getRemaining() {
        return Math.max(0, budget.get());
    }

    public int getMaxPerTick() { return maxPerTick; }

    public boolean isEnabled() { return enabled; }
}
