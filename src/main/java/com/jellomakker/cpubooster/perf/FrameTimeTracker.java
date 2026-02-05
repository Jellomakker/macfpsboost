package com.jellomakker.cpubooster.perf;

/**
 * Tracks frame times in a ring buffer to calculate averages and percentiles.
 */
public class FrameTimeTracker {
    private final int maxSize;
    private final double[] frameTimes;
    private int currentIndex = 0;
    private int count = 0;

    public FrameTimeTracker(int maxSize) {
        this.maxSize = maxSize;
        this.frameTimes = new double[maxSize];
    }

    public void recordFrameTime(double ms) {
        frameTimes[currentIndex] = ms;
        currentIndex = (currentIndex + 1) % maxSize;
        if (count < maxSize) count++;
    }

    public double getAverageFrameTime() {
        if (count == 0) return 0.0;
        double sum = 0.0;
        for (int i = 0; i < count; i++) {
            sum += frameTimes[i];
        }
        return sum / count;
    }

    public double getPercentile(double percentile) {
        if (count == 0) return 0.0;
        // Percentile: e.g., 0.99 = 99th percentile, 0.01 = 1st percentile
        int index = Math.max(0, (int) Math.ceil(count * (percentile / 100.0)) - 1);
        
        double[] sorted = new double[count];
        System.arraycopy(frameTimes, 0, sorted, 0, count);
        java.util.Arrays.sort(sorted);
        
        return sorted[index];
    }

    public double get1PercentLow() {
        // 1% low = 99th percentile (lowest 1% of frame times, i.e., slowest 1%)
        return getPercentile(1.0);
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
    }
}
