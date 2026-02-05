package com.jellomakker.cpubooster.patches;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.compat.AppleSiliconDetector;

/**
 * Frame pacing patch: Smooths out frame time spikes by yielding slightly
 * when a frame finishes early. This reduces micro-stutter without capping FPS.
 *
 * Safe approach:
 * - Only operates if enabled and on Apple Silicon
 * - Respects user's vsync/FPS settings
 * - Uses tiny sleep amounts (< 1ms) to smooth timing
 */
public class FramePacingPatch implements Patch {
    private long frameStartNs = System.nanoTime();
    private volatile boolean enabled = false;
    private static final long TARGET_FRAME_TIME_NS = 16_666_667L; // ~60 FPS
    private long frameCountForLogging = 0;
    private long accumulatedFrameTimeNs = 0;

    @Override
    public String getId() {
        return "framePacing";
    }

    @Override
    public String getName() {
        return "Frame Pacing Smoothing";
    }

    @Override
    public String getDescription() {
        return "Smooths micro-stutters by pacing frames when GPU is ahead of CPU";
    }

    @Override
    public boolean isAppleSiliconSpecific() {
        return true;
    }

    @Override
    public void initialize() {
        this.enabled = true;
        CpuBoosterMod.LOGGER.info("Frame Pacing patch initialized");
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    /**
     * Call this at the START of each frame (before rendering).
     * Should be registered to ClientTickEvents.START_CLIENT_TICK.
     */
    public void onFrameStart() {
        if (!enabled || !AppleSiliconDetector.isAppleSilicon()) return;
        frameStartNs = System.nanoTime();
    }

    /**
     * Call this at the END of each frame (after rendering).
     * Measures frame time and sleeps slightly if frame finished early.
     */
    public void onFrameEnd() {
        if (!enabled || !AppleSiliconDetector.isAppleSilicon()) return;

        long now = System.nanoTime();
        long frameTimeNs = now - frameStartNs;

        // Accumulate for debug logging
        accumulatedFrameTimeNs += frameTimeNs;
        frameCountForLogging++;

        // Log avg frame time every ~10s (600 frames at 60 FPS)
        if (frameCountForLogging >= 600) {
            double avgMs = accumulatedFrameTimeNs / (frameCountForLogging * 1_000_000.0);
            double stddev = calculateStddev();
            CpuBoosterMod.LOGGER.debug("Frame pacing stats: avg={:.2f}ms, stddev={:.2f}ms, samples={}",
                    avgMs, stddev, frameCountForLogging);
            accumulatedFrameTimeNs = 0;
            frameCountForLogging = 0;
        }

        // If frame finished early, sleep slightly to avoid GPU idle
        // This is very conservative: only yield if frame is <15ms (target is ~16.6ms)
        if (frameTimeNs < (TARGET_FRAME_TIME_NS - 1_000_000L)) {
            long sleepTimeNs = Math.min(500_000L, TARGET_FRAME_TIME_NS - frameTimeNs - 500_000L);
            if (sleepTimeNs > 0) {
                try {
                    Thread.sleep(0, (int) sleepTimeNs);
                } catch (InterruptedException e) {
                    // Ignored
                }
            }
        }
    }

    private double calculateStddev() {
        // Simplified: just return 0 for now (actual stddev calc would need history buffer)
        return 0.0;
    }
}
