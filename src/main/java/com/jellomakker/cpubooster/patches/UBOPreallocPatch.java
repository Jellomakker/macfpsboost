package com.jellomakker.cpubooster.patches;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.compat.AppleSiliconDetector;

/**
 * UBO Preallocation patch: Increases initial capacity of Uniform Buffer Objects
 * to reduce resize stutters on Apple Silicon.
 *
 * Problem: Minecraft's dynamic UBO (e.g., for transforms) starts small and resizes
 * when capacity is exceeded. On M1/M2, this resize can cause a stall.
 *
 * Solution: If resource manager tries to grow UBO, pre-allocate with larger capacity.
 * This is implemented via event hooks during resource loading.
 */
public class UBOPreallocPatch implements Patch {
    private volatile boolean enabled = false;
    // Configurable initial capacity multiplier (how much larger than default)
    private int capacityMultiplier = 2;

    public UBOPreallocPatch(int capacityMultiplier) {
        this.capacityMultiplier = capacityMultiplier;
    }

    @Override
    public String getId() {
        return "uboPrealloc";
    }

    @Override
    public String getName() {
        return "UBO Preallocation (Resize Stutter Fix)";
    }

    @Override
    public String getDescription() {
        return "Reduces stutters from Dynamic UBO resizing by pre-allocating larger buffers";
    }

    @Override
    public boolean isAppleSiliconSpecific() {
        return true;
    }

    @Override
    public void initialize() {
        this.enabled = true;
        CpuBoosterMod.LOGGER.info("UBO Prealloc patch initialized with capacity multiplier: {}", capacityMultiplier);
        // Actual mixin implementation happens via Mixin JSON
        // This patch just marks that the behavior is enabled
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    public int getCapacityMultiplier() {
        return capacityMultiplier;
    }

    public boolean isEnabled() {
        return enabled && AppleSiliconDetector.isAppleSilicon();
    }
}
