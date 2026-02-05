package com.jellomakker.cpubooster.perf;

/**
 * Tracks server tick times to measure game logic overhead.
 */
public class TickTimeTracker {
    private final int maxSize;
    private final double[] tickTimes;
    private int currentIndex = 0;
    private int count = 0;
    private long lastTickNs = System.nanoTime();

    public TickTimeTracker(int maxSize) {
        this.maxSize = maxSize;
        this.tickTimes = new double[maxSize];
    }

    public void markTick() {
        long now = System.nanoTime();
        long delta = now - lastTickNs;
        double ms = delta / 1_000_000.0;
        
        tickTimes[currentIndex] = ms;
        currentIndex = (currentIndex + 1) % maxSize;
        if (count < maxSize) count++;
        
        lastTickNs = now;
    }

    public double getAverageTickTime() {
        if (count == 0) return 0.0;
        double sum = 0.0;
        for (int i = 0; i < count; i++) {
            sum += tickTimes[i];
        }
        return sum / count;
    }

    public int getCount() {
        return count;
    }

    public boolean isFull() {
        return count == maxSize;
    }

    public void reset() {
        currentIndex = 0;
        count = 0;
        lastTickNs = System.nanoTime();
    }
}
