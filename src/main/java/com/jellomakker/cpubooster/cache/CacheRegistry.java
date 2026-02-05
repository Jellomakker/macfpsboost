package com.jellomakker.cpubooster.cache;

import com.jellomakker.cpubooster.CpuBoosterMod;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Central registry for caches used by the mod.
 * Caches must implement CacheRegistry.CacheHandle to be registered.
 */
public class CacheRegistry {
    public interface CacheHandle {
        String name();
        long size(); // approximate
        void cleanup();
    }

    private static final ConcurrentMap<String, CacheHandle> REGISTRY = new ConcurrentHashMap<>();

    public static void register(CacheHandle handle) {
        if (handle == null) return;
        REGISTRY.put(handle.name(), handle);
    }

    public static void unregister(String name) {
        REGISTRY.remove(name);
    }

    public static Map<String, CacheHandle> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(REGISTRY));
    }

    public static void cleanupAll() {
        try {
            for (CacheHandle h : REGISTRY.values()) {
                try {
                    h.cleanup();
                } catch (Throwable t) {
                    CpuBoosterMod.LOGGER.debug("Cache cleanup failed for {}: {}", h.name(), t.getMessage());
                }
            }
        } catch (Throwable t) {
            CpuBoosterMod.LOGGER.warn("CacheRegistry.cleanupAll disabled due to error: {}", t.getMessage());
        }
    }
}
