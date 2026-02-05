package com.jellomakker.cpubooster.compat;

import net.fabricmc.loader.api.FabricLoader;
import com.jellomakker.cpubooster.CpuBoosterMod;

/**
 * Detector for VulkanMod compatibility.
 *
 * If VulkanMod is detected, we log it and leave renderer optimization untouched
 * (since VulkanMod replaces the renderer entirely with Vulkan).
 */
public class VulkanModDetector {
    private static boolean vulkanModDetected = false;
    private static boolean detectionDone = false;

    /**
     * Check if VulkanMod is present and log.
     */
    public static void detectAndLog() {
        if (detectionDone) return;
        detectionDone = true;

        try {
            vulkanModDetected = FabricLoader.getInstance().isModLoaded("vulkanmod");
            if (vulkanModDetected) {
                CpuBoosterMod.LOGGER.info("VulkanMod detected; MacFPSBoost leaving renderer untouched");
            }
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.debug("Could not check for VulkanMod: {}", e.getMessage());
        }
    }

    /**
     * Check if VulkanMod is loaded.
     */
    public static boolean isVulkanModLoaded() {
        if (!detectionDone) {
            detectAndLog();
        }
        return vulkanModDetected;
    }
}
