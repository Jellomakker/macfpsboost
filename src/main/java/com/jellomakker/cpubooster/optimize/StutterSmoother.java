package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.ConfigManager;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;

/**
 * Detects frame time spikes and applies safe, original optimizations to smooth them.
 */
public class StutterSmoother {
    private long smoothingEndTimeNs = 0;
    private long lastGcTimeNs = 0;
    private volatile boolean smoothingActive = false;
    private boolean notifiedThisActivation = false;

    public void onFrame(double frameTimeMs) {
        CpuBoosterConfig cfg = ConfigManager.get();
        
        if (!cfg.stutterSmootherEnabled) {
            smoothingActive = false;
            return;
        }

        long now = System.nanoTime();

        // Check if smoothing window has expired
        if (smoothingEndTimeNs > 0 && now >= smoothingEndTimeNs) {
            if (smoothingActive) {
                CpuBoosterMod.LOGGER.debug("StutterSmoother: smoothing window ended");
                smoothingActive = false;
                notifiedThisActivation = false;
            }
            smoothingEndTimeNs = 0;
        }

        // Detect spike
        if (frameTimeMs > cfg.spikeThresholdMs && !smoothingActive) {
            // Activate smoothing
            smoothingActive = true;
            notifiedThisActivation = false;
            smoothingEndTimeNs = now + (cfg.smoothingWindowMs * 1_000_000L);
            CpuBoosterMod.LOGGER.debug("StutterSmoother: spike detected ({}ms > {}ms), activating smoothing for {}ms",
                    frameTimeMs, cfg.spikeThresholdMs, cfg.smoothingWindowMs);

            // GC hint (rate-limited)
            if (cfg.gcHintEnabled) {
                long timeSinceLastGc = now - lastGcTimeNs;
                if (timeSinceLastGc > (cfg.gcMinIntervalMs * 1_000_000L)) {
                    System.gc();
                    lastGcTimeNs = now;
                    CpuBoosterMod.LOGGER.debug("StutterSmoother: GC hint triggered");
                }
            }
        }
    }

    public boolean isSmoothing() {
        return smoothingActive;
    }

    public void notifyUserIfNeeded(CpuBoosterConfig cfg) {
        // Notify player once per smoothing activation (if overlay is visible)
        if (smoothingActive && !notifiedThisActivation && cfg.debugOverlayEnabled) {
            // Message is shown via debug overlay, no chat spam
            notifiedThisActivation = true;
        }
    }

    public void reset() {
        smoothingActive = false;
        smoothingEndTimeNs = 0;
        notifiedThisActivation = false;
    }
}
