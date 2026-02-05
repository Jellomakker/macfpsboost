package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Object pool manager for allocation-heavy operations.
 * Reuses allocated objects to reduce garbage collection pressure.
 * Particularly helpful for reducing stutter on servers with high entity counts.
 */
public class AllocationPoolManager {
    /**
     * Generic object pool interface.
     */
    public interface ObjectPool<T> {
        T acquire();
        void release(T obj);
        void clear();
    }

    /**
     * Simple queue-based object pool implementation.
     */
    public static class SimplePool<T> implements ObjectPool<T> {
        private final Queue<T> available;
        private final ObjectFactory<T> factory;
        private final int maxSize;
        private int created = 0;

        public interface ObjectFactory<T> {
            T create();
            void reset(T obj);
        }

        public SimplePool(ObjectFactory<T> factory, int initialSize, int maxSize) {
            this.factory = factory;
            this.maxSize = maxSize;
            this.available = new LinkedList<>();
            for (int i = 0; i < initialSize; i++) {
                available.offer(factory.create());
                created++;
            }
        }

        @Override
        public T acquire() {
            T obj = available.poll();
            if (obj == null && created < maxSize) {
                obj = factory.create();
                created++;
            }
            return obj;
        }

        @Override
        public void release(T obj) {
            if (obj != null && available.size() < maxSize) {
                factory.reset(obj);
                available.offer(obj);
            }
        }

        @Override
        public void clear() {
            available.clear();
            created = 0;
        }

        public int getAvailableCount() {
            return available.size();
        }

        public int getTotalCreated() {
            return created;
        }
    }

    private int poolSize = 256;
    private boolean enabled = false;

    public AllocationPoolManager() {
    }

    public void updateConfig(CpuBoosterConfig cfg) {
        this.enabled = cfg.enableAllocationPooling;
        this.poolSize = cfg.allocationPoolSize;
    }

    public void onTick(CpuBoosterConfig cfg) {
        if (!cfg.enableAllocationPooling) {
            return;
        }

        try {
            // Periodic pool health check could happen here
            // For now, pools are managed individually by subsystems
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("AllocationPoolManager error: {}", e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getPoolSize() {
        return poolSize;
    }
}
