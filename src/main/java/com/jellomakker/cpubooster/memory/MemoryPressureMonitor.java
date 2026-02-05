package com.jellomakker.cpubooster.memory;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;

/**
 * Lightweight memory pressure monitor.
 * Tracks used heap and growth rate; triggers "pressure" cooldown when growth
 * exceeds configured threshold.
 */
public class MemoryPressureMonitor {
    private long lastUsedBytes = 0;
    private long lastCheckTime = System.currentTimeMillis();
    private long cooldownEnd = 0;
    private int growthThresholdBytes = 128 * 1024 * 1024; // default 128MB
    private long memoryCooldownMs = 2000;
    private boolean enabled = true;

    public MemoryPressureMonitor() {
    }

    public void updateConfig(CpuBoosterConfig cfg) {
        this.enabled = cfg.memoryPressureEnabled;
        this.growthThresholdBytes = cfg.memoryGrowthThresholdMB * 1024 * 1024;
        this.memoryCooldownMs = cfg.memoryCooldownMs;
    }

    /**
     * Call from tick; checks allocation trend and sets cooldown if needed.
     */
    public void onTick() {
        if (!enabled) return;
        try {
            long now = System.currentTimeMillis();
            Runtime rt = Runtime.getRuntime();
            long used = rt.totalMemory() - rt.freeMemory();
            long dt = now - lastCheckTime;
            if (lastCheckTime > 0 && dt > 0) {
                long delta = used - lastUsedBytes;
                if (delta > growthThresholdBytes) {
                    cooldownEnd = now + memoryCooldownMs;
                    CpuBoosterMod.LOGGER.info("Memory pressure detected: +{} MB in {} ms; entering cooldown {} ms",
                            delta / (1024*1024), dt, memoryCooldownMs);
                }
            }
            lastUsedBytes = used;
            lastCheckTime = now;
        } catch (Throwable t) {
            // Disable on error
            this.enabled = false;
            CpuBoosterMod.LOGGER.warn("MemoryPressureMonitor disabled due to error: {}", t.getMessage());
        }
    }

    public boolean isUnderPressure() {
        return enabled && System.currentTimeMillis() < cooldownEnd;
    }

    public long getCooldownRemainingMs() {
        long rem = cooldownEnd - System.currentTimeMillis();
        return rem > 0 ? rem : 0;
    }
}
