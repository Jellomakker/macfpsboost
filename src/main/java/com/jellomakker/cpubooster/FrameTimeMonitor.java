package com.jellomakker.cpubooster;

public class FrameTimeMonitor {
    private long lastFrameNs = System.nanoTime();
    private double lastFrameMs = 0.0;

    public void markFrame() {
        long now = System.nanoTime();
        long delta = now - lastFrameNs;
        lastFrameMs = delta / 1_000_000.0;
        lastFrameNs = now;
    }

    public double getLastFrameMs() {
        return lastFrameMs;
    }
}
