package com.jellomakker.cpubooster.metrics;

import com.jellomakker.cpubooster.CpuBoosterMod;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Detects other optimization mods and disables overlapping patches.
 * Ensures no conflicts with Sodium, Lithium, ImmediatelyFast, etc.
 */
public class ModCompatibilityDetector {
    private static final boolean HAS_SODIUM = isModLoaded("sodium");
    private static final boolean HAS_LITHIUM = isModLoaded("lithium");
    private static final boolean HAS_IMMEDIATELY_FAST = isModLoaded("immediatelyfast");
    private static final boolean HAS_FERRITECORE = isModLoaded("ferrite-core");
    private static final boolean HAS_KRYPTON = isModLoaded("krypton");

    public static void logDetectedMods() {
        CpuBoosterMod.LOGGER.info("=== Mod Compatibility Check ===");
        logMod("Sodium", HAS_SODIUM, "chunk rendering, memory optimization");
        logMod("Lithium", HAS_LITHIUM, "general optimization, tick speedup");
        logMod("ImmediatelyFast", HAS_IMMEDIATELY_FAST, "immediate mode rendering");
        logMod("FerriteCore", HAS_FERRITECORE, "memory optimization");
        logMod("Krypton", HAS_KRYPTON, "network/tick optimization");
        
        int count = (HAS_SODIUM ? 1 : 0) + (HAS_LITHIUM ? 1 : 0) + 
                    (HAS_IMMEDIATELY_FAST ? 1 : 0) + (HAS_FERRITECORE ? 1 : 0) + 
                    (HAS_KRYPTON ? 1 : 0);
        
        if (count > 0) {
            CpuBoosterMod.LOGGER.info("Detected {} optimization mod(s). MacFPSBoost will avoid conflicts.", count);
        } else {
            CpuBoosterMod.LOGGER.info("No other optimization mods detected. All patches enabled.");
        }
    }

    private static void logMod(String name, boolean loaded, String purpose) {
        if (loaded) {
            CpuBoosterMod.LOGGER.info("  ✓ {} loaded ({})", name, purpose);
        }
    }

    public static boolean hasSodium() {
        return HAS_SODIUM;
    }

    public static boolean hasLithium() {
        return HAS_LITHIUM;
    }

    public static boolean hasImmediatelyFast() {
        return HAS_IMMEDIATELY_FAST;
    }

    public static boolean hasFerriteCore() {
        return HAS_FERRITECORE;
    }

    public static boolean hasKrypton() {
        return HAS_KRYPTON;
    }

    private static boolean isModLoaded(String modId) {
        try {
            return FabricLoader.getInstance().isModLoaded(modId);
        } catch (Exception e) {
            return false;
        }
    }
}
