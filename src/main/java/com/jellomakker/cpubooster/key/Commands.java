package com.jellomakker.cpubooster.key;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.compat.AppleSiliconDetector;
import com.jellomakker.cpubooster.config.ConfigManager;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import com.jellomakker.cpubooster.optimize.FramePacingThrottler;
import com.jellomakker.cpubooster.optimize.ChunkRebuildLimiter;
import com.jellomakker.cpubooster.patches.PatchManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

/**
 * Registers client-side commands for cpubooster.
 */
public class Commands {
    public static void registerCommands() {
        try {
            ClientCommandRegistrationCallback.EVENT.register(Commands::register);
            CpuBoosterMod.LOGGER.debug("Command event handler registered");
        } catch (NoClassDefFoundError e) {
            CpuBoosterMod.LOGGER.debug("Command API unavailable (non-critical)");
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("Failed to register command event (non-critical): {}", e.getMessage());
            CpuBoosterMod.LOGGER.debug("Command registration error:", e);
        }
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                  CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(
                com.mojang.brigadier.builder.LiteralArgumentBuilder
                        .<FabricClientCommandSource>literal("cpubooster")
                        .then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                                .<FabricClientCommandSource>literal("status")
                                .executes(Commands::statusCommand))
        );
    }

    private static int statusCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();

        try {
            CpuBoosterConfig cfg = ConfigManager.get();
            boolean modEnabled = cfg.enabled;
            boolean appleSilicon = AppleSiliconDetector.isAppleSilicon();

            source.sendFeedback(Text.literal("§6=== CPU Booster Status ==="));
            source.sendFeedback(Text.literal(String.format("§fMod Enabled: %s", modEnabled ? "§aYES" : "§cNO")));
            source.sendFeedback(Text.literal(String.format("§fApple Silicon Detected: %s", appleSilicon ? "§aYES" : "§cNO")));
            
            // Optimization profile
            source.sendFeedback(Text.literal(String.format("§fOptimization Profile: §e%s", cfg.optimizationProfile)));

            // Frame pacing throttler status
            FramePacingThrottler fpt = CpuBoosterMod.getFramePacingThrottler();
            source.sendFeedback(Text.literal("§6Frame Pacing Throttler:"));
            String throttleStatus = cfg.adaptiveThrottlingEnabled ? "§aEnabled" : "§cDisabled";
            source.sendFeedback(Text.literal(String.format("  Status: %s", throttleStatus)));
            if (fpt != null) {
                source.sendFeedback(Text.literal(String.format("  EMA Frame Time: §e%.1f ms§f (target: %.1f ms)", 
                    fpt.getFrameTimeEma(), cfg.frameTimeTargetMs)));
                source.sendFeedback(Text.literal(String.format("  Spikes Detected: §e%d", fpt.getSpikesDetected())));
                long throttleRemaining = fpt.getThrottleTimeRemaining();
                String throttleState = throttleRemaining > 0 ? String.format("§c%d ms remaining", throttleRemaining) : "§aReady";
                source.sendFeedback(Text.literal(String.format("  Throttle Mode: %s", throttleState)));
            }

            // Chunk rebuild limiter status
            ChunkRebuildLimiter crl = CpuBoosterMod.getChunkRebuildLimiter();
            source.sendFeedback(Text.literal("§6Chunk Rebuild Limiter:"));
            int maxRate = crl.getCurrentMaxRate();
            String rateLabel = crl.isThrottleMode() ? "§c(THROTTLED)" : "§a(NORMAL)";
            source.sendFeedback(Text.literal(String.format("  Max Rate: §e%d/sec %s", maxRate, rateLabel)));
            if (crl != null) {
                source.sendFeedback(Text.literal(String.format("  Available Tokens: §e%.1f", crl.getTokens())));
            }

            // Patches
            source.sendFeedback(Text.literal("§6Patches:"));
            PatchManager patchManager = CpuBoosterMod.getPatchManager();
            for (String id : patchManager.getPatchInfo().keySet()) {
                String info = patchManager.getPatchInfo().get(id);
                boolean isEnabled = patchManager.getPatchState(id);
                String color = isEnabled ? "§a" : "§c";
                source.sendFeedback(Text.literal(String.format("  %s%s", color, info)));
            }

            // Additional system info
            source.sendFeedback(Text.literal("§6System Features:"));
            source.sendFeedback(Text.literal(String.format("  Debug Logging: %s", cfg.debugLogging ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Metrics Collection: %s", cfg.metricsEnabled ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Debug Overlay: %s", cfg.debugOverlayEnabled ? "§aON" : "§cOFF")));
            // Memory pressure
            try {
                com.jellomakker.cpubooster.memory.MemoryPressureMonitor monitor = com.jellomakker.cpubooster.CpuBoosterMod.getMemoryPressureMonitor();
                if (monitor != null) {
                    source.sendFeedback(Text.literal(String.format("  Memory Pressure: %s", monitor.isUnderPressure() ? "§cACTIVE" : "§aOK")));
                    source.sendFeedback(Text.literal(String.format("  Memory Cooldown Remaining: %d ms", monitor.getCooldownRemainingMs())));
                }
            } catch (Throwable t) {
                // ignore safe diagnostics
            }

            // Cache registry
            try {
                var snap = com.jellomakker.cpubooster.cache.CacheRegistry.snapshot();
                source.sendFeedback(Text.literal("§6Caches:"));
                if (snap.isEmpty()) {
                    source.sendFeedback(Text.literal("  (no registered caches)"));
                } else {
                    for (String name : snap.keySet()) {
                        com.jellomakker.cpubooster.cache.CacheRegistry.CacheHandle h = snap.get(name);
                        source.sendFeedback(Text.literal(String.format("  %s: %d items", name, h.size())));
                    }
                }
            } catch (Throwable t) {
                // ignore
            }

            // Block entity limiter
            try {
                com.jellomakker.cpubooster.block.BlockEntityUpdateLimiter bel = com.jellomakker.cpubooster.CpuBoosterMod.getBlockEntityUpdateLimiter();
                if (bel != null) {
                    source.sendFeedback(Text.literal(String.format("§6Block Entity Limiter: Enabled=%s MaxPerTick=%d Remaining=%d",
                            bel.isEnabled() ? "YES" : "NO", bel.getMaxPerTick(), bel.getRemaining())));
                }
            } catch (Throwable t) {
                // ignore
            }

            // Compatibility detectors
            try {
                source.sendFeedback(Text.literal("§6Detected Mods:"));
                source.sendFeedback(Text.literal(String.format("  Sodium: %s", com.jellomakker.cpubooster.compat.AdvancedCompatibilityDetector.sodium ? "YES" : "NO")));
                source.sendFeedback(Text.literal(String.format("  Embeddium: %s", com.jellomakker.cpubooster.compat.AdvancedCompatibilityDetector.embeddium ? "YES" : "NO")));
                source.sendFeedback(Text.literal(String.format("  ModernFix: %s", com.jellomakker.cpubooster.compat.AdvancedCompatibilityDetector.modernfix ? "YES" : "NO")));
                source.sendFeedback(Text.literal(String.format("  MemoryLeakFix: %s", com.jellomakker.cpubooster.compat.AdvancedCompatibilityDetector.memoryleakfix ? "YES" : "NO")));
                source.sendFeedback(Text.literal(String.format("  EnhancedBE: %s", com.jellomakker.cpubooster.compat.AdvancedCompatibilityDetector.enhancedbe ? "YES" : "NO")));
            } catch (Throwable t) {
                // ignore
            }

            // ===== 10 MAJOR OPTIMIZATION FEATURES STATUS =====
            source.sendFeedback(Text.literal("§6FEATURE STATUS:"));
            source.sendFeedback(Text.literal(String.format("  Frame-Time Variance Optimizer: %s", cfg.enableFrameTimeVarianceOptimizer ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Smart Chunk Rebuild: %s", cfg.enableSmartChunkRebuild ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Entity Freezing: %s", cfg.enableEntityFreezing ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Allocation Pooling: %s", cfg.enableAllocationPooling ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Input-Render Decoupling: %s", cfg.enableInputRenderDecoupling ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Adaptive Profiles: %s (%s)", 
                cfg.enableProfiles ? "§aON" : "§cOFF", 
                cfg.profileMode)));
            source.sendFeedback(Text.literal(String.format("  Render State Dedup (EXP): %s", cfg.enableRenderStateDedup ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  GPU Batching (EXP): %s", cfg.enableGPUBatching ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Resolution Scaling (EXP): %s", cfg.enableResolutionScaling ? "§aON" : "§cOFF")));
            source.sendFeedback(Text.literal(String.format("  Block Entity Cold Storage: %s", cfg.enableBlockEntityColdStorage ? "§aON" : "§cOFF")));
        } catch (Exception e) {
            source.sendError(Text.literal("Error getting status: " + e.getMessage()));
        }

        return 1;
    }
}
