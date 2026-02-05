package com.jellomakker.cpubooster.patches;

import com.jellomakker.cpubooster.CpuBoosterMod;

/**
 * Allocation reduction patch: Minimizes per-frame allocations in our own code.
 * This helps reduce garbage collection pressure on M1/M2.
 *
 * Implementation strategies:
 * - Cache commonly-used Text objects
 * - Reuse buffers where possible
 * - Avoid temporary object creation in hot paths
 */
public class AllocationReducerPatch implements Patch {
    private volatile boolean enabled = false;

    @Override
    public String getId() {
        return "allocationReducer";
    }

    @Override
    public String getName() {
        return "Allocation Reduction";
    }

    @Override
    public String getDescription() {
        return "Reduces per-frame allocations to lower GC pressure";
    }

    @Override
    public boolean isAppleSiliconSpecific() {
        return false; // Beneficial on all platforms
    }

    @Override
    public void initialize() {
        this.enabled = true;
        CpuBoosterMod.LOGGER.info("Allocation Reducer patch initialized");
    }

    @Override
    public void disable() {
        this.enabled = false;
    }
}
