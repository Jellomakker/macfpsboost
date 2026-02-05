package com.jellomakker.cpubooster.compat;

import com.jellomakker.cpubooster.CpuBoosterMod;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Detects presence of other popular optimization mods to avoid interfering.
 */
public class AdvancedCompatibilityDetector {
    private static boolean detectionDone = false;
    public static boolean sodium = false;
    public static boolean embeddium = false;
    public static boolean modernfix = false;
    public static boolean memoryleakfix = false;
    public static boolean enhancedbe = false;

    public static void detectAndLog() {
        if (detectionDone) return;
        detectionDone = true;
        try {
            sodium = FabricLoader.getInstance().isModLoaded("sodium");
            embeddium = FabricLoader.getInstance().isModLoaded("embeddium");
            modernfix = FabricLoader.getInstance().isModLoaded("modernfix");
            memoryleakfix = FabricLoader.getInstance().isModLoaded("memoryleakfix");
            enhancedbe = FabricLoader.getInstance().isModLoaded("enhancedbe");

            CpuBoosterMod.LOGGER.info("=== Compatibility Detection ===");
            CpuBoosterMod.LOGGER.info("Sodium: {} | Embeddium: {} | ModernFix: {} | MemoryLeakFix: {} | EnhancedBE: {}",
                    sodium, embeddium, modernfix, memoryleakfix, enhancedbe);
        } catch (Throwable t) {
            CpuBoosterMod.LOGGER.debug("Compatibility detection failed: {}", t.getMessage());
        }
    }
}
