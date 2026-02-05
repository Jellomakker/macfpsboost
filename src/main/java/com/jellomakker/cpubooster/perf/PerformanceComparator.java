package com.jellomakker.cpubooster.perf;

/**
 * Measures FPS difference between mod enabled and disabled states.
 * Tracks baseline frame times and compares current performance.
 */
public class PerformanceComparator {
    private double baselineFps = 0;     // FPS when mod disabled
    private double currentFps = 0;      // FPS when mod enabled
    private long measurementStartTime = 0;
    private int frameCount = 0;
    private final int MEASUREMENT_FRAMES = 300; // ~5 seconds at 60 FPS
    private boolean measuring = false;
    private boolean modWasEnabled = false;

    public void startMeasurement(boolean modCurrentlyEnabled) {
        measuring = true;
        frameCount = 0;
        measurementStartTime = System.currentTimeMillis();
        modWasEnabled = modCurrentlyEnabled;
    }

    public void onFrame(double frameTimeMs) {
        if (!measuring) return;
        frameCount++;
        if (frameCount >= MEASUREMENT_FRAMES) {
            endMeasurement();
        }
    }

    private void endMeasurement() {
        long elapsedMs = System.currentTimeMillis() - measurementStartTime;
        if (elapsedMs > 0) {
            double fps = (frameCount / (elapsedMs / 1000.0));
            if (modWasEnabled) {
                currentFps = fps;
            } else {
                baselineFps = fps;
            }
        }
        measuring = false;
    }

    public double getBaselineFps() { return baselineFps; }
    public double getCurrentFps() { return currentFps; }

    public double getImprovement() {
        if (baselineFps <= 0) return 0;
        return ((currentFps - baselineFps) / baselineFps) * 100.0;
    }

    public boolean isMeasuring() { return measuring; }
    public int getMeasurementProgress() { return Math.min(100, (frameCount * 100) / MEASUREMENT_FRAMES); }
}
