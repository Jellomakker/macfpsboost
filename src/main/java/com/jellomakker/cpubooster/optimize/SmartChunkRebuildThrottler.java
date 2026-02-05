package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Smart chunk rebuild throttling that prioritizes chunks by distance and direction.
 * Ensures closer chunks rebuild first, reducing perceived latency.
 */
public class SmartChunkRebuildThrottler {
    private static class ChunkTask {
        long chunkHash;
        double distanceFromPlayer;
        boolean inFrontOfPlayer;

        ChunkTask(long hash, double dist, boolean inFront) {
            this.chunkHash = hash;
            this.distanceFromPlayer = dist;
            this.inFrontOfPlayer = inFront;
        }
    }

    // Priority queue: closer chunks and chunks in front have higher priority
    private final PriorityQueue<ChunkTask> chunkQueue = new PriorityQueue<>(
            Comparator.<ChunkTask>comparingDouble(t -> {
                // Prioritize chunks closer to player
                double priority = t.distanceFromPlayer;
                // Bonus for chunks in front of camera
                if (t.inFrontOfPlayer) priority *= 0.7;
                return priority;
            })
    );

    private int rebuildsThisTick = 0;

    public void reset() {
        rebuildsThisTick = 0;
    }

    public void onTick(CpuBoosterConfig cfg) {
        if (!cfg.enableSmartChunkRebuild) {
            return;
        }

        try {
            reset();
            // Actual chunk rebuild logic would be injected via patches
            // This class provides prioritization data structure and decision making
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("SmartChunkRebuildThrottler error: {}", e.getMessage());
        }
    }

    public boolean canRebuildChunk(CpuBoosterConfig cfg) {
        if (!cfg.enableSmartChunkRebuild) {
            return true;
        }
        return rebuildsThisTick < cfg.smartChunkMaxRebuildsPerTick;
    }

    public void recordChunkRebuild() {
        rebuildsThisTick++;
    }

    public void enqueueChunk(long chunkHash, double distanceFromPlayer, boolean inFrontOfCamera) {
        if (chunkQueue.size() < 100) { // Prevent unbounded queue growth
            chunkQueue.offer(new ChunkTask(chunkHash, distanceFromPlayer, inFrontOfCamera));
        }
    }

    public ChunkTask getNextChunk() {
        return chunkQueue.poll();
    }

    public int getQueueSize() {
        return chunkQueue.size();
    }
}
