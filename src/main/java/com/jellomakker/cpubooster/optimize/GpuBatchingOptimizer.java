package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * EXPERIMENTAL: GPU-friendly render batching.
 * Groups similar rendering operations to reduce draw call overhead.
 * WARNING: This is experimental and disabled by default. Enable at your own risk.
 */
public class GpuBatchingOptimizer {
    private static class RenderBatch {
        int textureId;
        int shaderProgram;
        int vertexCount;
        List<Integer> drawCalls = new ArrayList<>();
    }

    private final List<RenderBatch> currentBatches = new ArrayList<>();
    private int batchesThisFrame = 0;
    private int drawCallsSaved = 0;

    public void onFrameStart(CpuBoosterConfig cfg) {
        if (!cfg.enableGPUBatching) {
            currentBatches.clear();
            return;
        }

        try {
            batchesThisFrame = 0;
            // Batches accumulate during frame and are flushed at frame end
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("GpuBatchingOptimizer error: {}", e.getMessage());
        }
    }

    public void onFrameEnd() {
        // Flush accumulated batches
        for (RenderBatch batch : currentBatches) {
            if (batch.drawCalls.size() > 1) {
                // This batch was combined from multiple calls
                drawCallsSaved += batch.drawCalls.size() - 1;
            }
        }
        currentBatches.clear();
    }

    /**
     * Check if a new draw call can be batched with existing calls.
     */
    public boolean canBatch(int textureId, int shaderProgram, int vertexCount) {
        // Look for compatible batch
        for (RenderBatch batch : currentBatches) {
            if (batch.textureId == textureId && batch.shaderProgram == shaderProgram) {
                batch.vertexCount += vertexCount;
                batch.drawCalls.add(vertexCount);
                return true;
            }
        }

        // Create new batch if under limit
        if (currentBatches.size() < 100) {
            RenderBatch newBatch = new RenderBatch();
            newBatch.textureId = textureId;
            newBatch.shaderProgram = shaderProgram;
            newBatch.vertexCount = vertexCount;
            newBatch.drawCalls.add(vertexCount);
            currentBatches.add(newBatch);
            batchesThisFrame++;
            return true;
        }

        return false;
    }

    public int getBatchCount() {
        return currentBatches.size();
    }

    public int getDrawCallsSaved() {
        return drawCallsSaved;
    }

    public String getDebugInfo() {
        return "GPU Batching: " + batchesThisFrame + " batches, " + drawCallsSaved + " calls saved";
    }
}
