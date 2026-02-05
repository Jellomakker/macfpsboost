package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;

/**
 * EXPERIMENTAL: Dynamically reduces internal render resolution during frame-time spikes.
 * Can significantly boost FPS when the GPU is the bottleneck.
 * WARNING: This is experimental and disabled by default. Enable at your own risk.
 * Side effects: Slight image quality reduction when scaling is active.
 */
public class DynamicResolutionScaler {
    private double currentScale = 1.0;
    private int framesAtScale = 0;
    private int framesSinceScaleChange = 0;
    private static final int STABILIZATION_FRAMES = 30;

    public void onFrame(double frameTimeMs, CpuBoosterConfig cfg) {
        if (!cfg.enableResolutionScaling) {
            currentScale = 1.0;
            return;
        }

        try {
            double targetFrameTime = 16.6; // Target 60 FPS
            double spike = frameTimeMs - targetFrameTime;

            if (spike > 5.0) {
                // Frame time spiked, reduce resolution
                currentScale = currentScale * 0.95; // Reduce by 5%
                if (currentScale < cfg.resolutionScaleMin) {
                    currentScale = cfg.resolutionScaleMin;
                }
                framesAtScale = 0;
                if (cfg.debugLogging) {
                    CpuBoosterMod.LOGGER.debug("DynamicResolutionScaler: reducing to {}", String.format("%.2f", currentScale));
                }
            } else if (spike < -2.0) {
                // Frame time improved, increase resolution
                currentScale = currentScale * 1.03; // Increase by 3%
                if (currentScale > cfg.resolutionScaleMax) {
                    currentScale = cfg.resolutionScaleMax;
                }
                framesAtScale = 0;
                if (cfg.debugLogging) {
                    CpuBoosterMod.LOGGER.debug("DynamicResolutionScaler: increasing to {}", String.format("%.2f", currentScale));
                }
            } else {
                framesAtScale++;
            }

            framesSinceScaleChange++;
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("DynamicResolutionScaler error: {}", e.getMessage());
            currentScale = 1.0;
        }
    }

    public double getCurrentScale() {
        return currentScale;
    }

    public boolean isScalingActive() {
        return currentScale < 0.99;
    }

    public void reset() {
        currentScale = 1.0;
        framesAtScale = 0;
        framesSinceScaleChange = 0;
    }

    public String getDebugInfo() {
        return String.format("ResolutionScale: %.2f %s", currentScale, isScalingActive() ? "(ACTIVE)" : "");
    }
}
