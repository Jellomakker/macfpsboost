package com.jellomakker.cpubooster.profiles;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;

/**
 * Automatically detects current gameplay mode and suggests optimization profile.
 * Analyzes entity proximity, camera rotation speed, block placement frequency, etc.
 */
public class AdaptiveProfileDetector {
    private GameProfile currentProfile = GameProfile.EXPLORATION;
    private int tickCounter = 0;
    private final int DETECTION_INTERVAL = 100; // Re-detect every 100 ticks (~5 seconds)

    // Counters for behavior analysis
    private int lastBlockPlacementTick = 0;
    private int lastEntityInteractionTick = 0;
    private double lastCameraRotationDelta = 0;
    private int hostileEntitiesNearby = 0;

    public void onTick(CpuBoosterConfig cfg) {
        if (!cfg.enableProfiles || !"AUTO".equals(cfg.profileMode)) {
            return;
        }

        try {
            tickCounter++;
            if (tickCounter >= DETECTION_INTERVAL) {
                tickCounter = 0;
                detectProfile();
            }
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("AdaptiveProfileDetector error: {}", e.getMessage());
        }
    }

    private void detectProfile() {
        // Simple heuristic-based detection
        // In a real scenario, these would be populated by event hooks from the game

        // Check for active combat (hostile entities nearby + fast camera rotation)
        if (hostileEntitiesNearby > 2 && lastCameraRotationDelta > 10.0) {
            currentProfile = GameProfile.PVP;
            return;
        }

        // Check for active building (frequent block placements)
        if (lastBlockPlacementTick > 0 && (tickCounter - lastBlockPlacementTick) < 50) {
            currentProfile = GameProfile.BUILDING;
            return;
        }

        // Check for idle/AFK (no meaningful input for extended period)
        if (lastBlockPlacementTick == 0 && lastEntityInteractionTick == 0 && lastCameraRotationDelta < 1.0) {
            currentProfile = GameProfile.AFK;
            return;
        }

        // Default to exploration
        currentProfile = GameProfile.EXPLORATION;
    }

    public GameProfile getCurrentProfile() {
        return currentProfile;
    }

    public void recordBlockPlacement() {
        lastBlockPlacementTick = tickCounter;
    }

    public void recordEntityInteraction() {
        lastEntityInteractionTick = tickCounter;
    }

    public void setCameraRotationDelta(double delta) {
        lastCameraRotationDelta = delta;
    }

    public void setHostileEntitiesNearby(int count) {
        hostileEntitiesNearby = count;
    }

    public String getDetectionInfo() {
        return String.format("Profile=%s, Hostile=%d, CamRot=%.2f", 
                currentProfile.getDisplayName(), 
                hostileEntitiesNearby,
                lastCameraRotationDelta);
    }
}
