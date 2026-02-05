package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches render data for idle (cold) block entities.
 * Reduces update frequency for block entities that haven't changed recently.
 * Similar to concepts from Enhanced Block Entities, but independently implemented.
 */
public class BlockEntityColdStorage {
    private static class CachedBlockEntity {
        long lastUpdateTick;
        Object cachedData; // Would be actual render cache in real implementation
        boolean isDirty;

        CachedBlockEntity() {
            this.lastUpdateTick = 0;
            this.isDirty = false;
        }
    }

    private final Map<Integer, CachedBlockEntity> entityCache = new HashMap<>();
    private long currentTick = 0;

    public void onTick(CpuBoosterConfig cfg) {
        if (!cfg.enableBlockEntityColdStorage) {
            entityCache.clear();
            return;
        }

        try {
            currentTick++;

            // Periodically clean up stale cache entries
            if (currentTick % 100 == 0) {
                entityCache.entrySet().removeIf(e -> 
                    (currentTick - e.getValue().lastUpdateTick) > (cfg.blockEntityIdleThreshold * 2)
                );
            }
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("BlockEntityColdStorage error: {}", e.getMessage());
        }
    }

    /**
     * Returns true if a block entity can use cached data.
     */
    public boolean canUseCachedData(int blockEntityId, long idleThresholdTicks) {
        CachedBlockEntity cached = entityCache.get(blockEntityId);
        if (cached == null || cached.isDirty) {
            return false;
        }
        return (currentTick - cached.lastUpdateTick) > idleThresholdTicks;
    }

    /**
     * Mark a block entity as having fresh data.
     */
    public void updateEntity(int blockEntityId) {
        CachedBlockEntity entry = entityCache.computeIfAbsent(blockEntityId, k -> new CachedBlockEntity());
        entry.lastUpdateTick = currentTick;
        entry.isDirty = false;
    }

    /**
     * Mark a block entity as dirty (needs recomputation).
     */
    public void markDirty(int blockEntityId) {
        CachedBlockEntity entry = entityCache.get(blockEntityId);
        if (entry != null) {
            entry.isDirty = true;
        }
    }

    public int getCacheSize() {
        return entityCache.size();
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public void clearCache() {
        entityCache.clear();
    }

    public String getDebugInfo() {
        return "BlockEntityColdStorage: " + entityCache.size() + " cached entities";
    }
}
