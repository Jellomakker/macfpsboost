package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Tracks frame-time variance and automatically defers heavy work during spikes.
 * Detects instability in frame times and signals to defer expensive operations.
 */
public class FrameTimeVarianceOptimizer {
    private final Queue<Double> frameHistory = new LinkedList<>();
    private static final int HISTORY_SIZE = 30;
    private double averageFrameTime = 16.0;
    private boolean deferralActive = false;
    private int deferralCountdown = 0;

    public void onFrame(double frameTimeMs, CpuBoosterConfig cfg) {
        if (!cfg.enableFrameTimeVarianceOptimizer) {
            return;
        }

        try {
            // Add frame time to history
            frameHistory.offer(frameTimeMs);
            if (frameHistory.size() > HISTORY_SIZE) {
                frameHistory.poll();
            }

            // Calculate average
            double sum = 0;
            for (double ft : frameHistory) {
                sum += ft;
            }
            averageFrameTime = sum / frameHistory.size();

            // Detect variance spike
            double variance = 0;
            for (double ft : frameHistory) {
                variance += Math.pow(ft - averageFrameTime, 2);
            }
            double stddev = Math.sqrt(variance / frameHistory.size());

            // If spike detected, activate deferral
            if (stddev > cfg.frameTimeVarianceSpikeThreshold) {
                deferralActive = true;
                deferralCountdown = cfg.deferralDurationTicks;
                if (cfg.debugLogging) {
                    CpuBoosterMod.LOGGER.debug("FrameTimeVarianceOptimizer: spike detected (stddev={}, avg={})",
                            String.format("%.2f", stddev), String.format("%.2f", averageFrameTime));
                }
            }

            // Decrement deferral countdown
            if (deferralCountdown > 0) {
                deferralCountdown--;
            } else if (deferralActive) {
                deferralActive = false;
            }
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("FrameTimeVarianceOptimizer disabled due to error: {}", e.getMessage());
        }
    }

    public boolean isDeferralActive() {
        return deferralActive;
    }

    public double getAverageFrameTime() {
        return averageFrameTime;
    }

    public int getHistorySize() {
        return frameHistory.size();
    }
}
