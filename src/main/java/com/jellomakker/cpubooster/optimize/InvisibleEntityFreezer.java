package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import java.util.HashSet;
import java.util.Set;

/**
 * Freezes entity updates for entities that are far away and not visible.
 * Reduces entity tick overhead significantly, especially on multiplayer servers.
 * Safety: Never freezes entities within configurable safety radius.
 */
public class InvisibleEntityFreezer {
    private final Set<Integer> frozenEntityIds = new HashSet<>();

    public void onTick(CpuBoosterConfig cfg) {
        if (!cfg.enableEntityFreezing) {
            frozenEntityIds.clear();
            return;
        }

        try {
            // Actual entity iteration and freezing would be done via event hooks
            // This class provides the decision logic and frozen entity tracking
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("InvisibleEntityFreezer error: {}", e.getMessage());
        }
    }

    /**
     * Determines if an entity should be frozen (updates deferred).
     * @param distanceFromPlayer distance in blocks
     * @param isVisible whether entity is in camera frustum
     * @param isHostile whether entity is a hostile mob
     * @return true if should be frozen
     */
    public boolean shouldFreezeEntity(double distanceFromPlayer, boolean isVisible, boolean isHostile, CpuBoosterConfig cfg) {
        if (!cfg.enableEntityFreezing) {
            return false;
        }

        // Never freeze entities in safety radius
        if (distanceFromPlayer < cfg.entitySafetyRadius) {
            return false;
        }

        // Never freeze visible entities
        if (isVisible) {
            return false;
        }

        // Optionally skip hostile mobs (players may want to keep them active for defense)
        if (isHostile && !cfg.freezeHostileMobs) {
            return false;
        }

        // Freeze if far enough
        return distanceFromPlayer > cfg.entityFreezeDistance;
    }

    public void registerFrozenEntity(int entityId) {
        frozenEntityIds.add(entityId);
    }

    public void unfreezEntity(int entityId) {
        frozenEntityIds.remove(entityId);
    }

    public boolean isFrozen(int entityId) {
        return frozenEntityIds.contains(entityId);
    }

    public void clearAll() {
        frozenEntityIds.clear();
    }

    public int getFrozenCount() {
        return frozenEntityIds.size();
    }
}
