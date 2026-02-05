package com.jellomakker.cpubooster;

import com.jellomakker.cpubooster.compat.AppleSiliconDetector;
import com.jellomakker.cpubooster.compat.EnvironmentDetector;
import com.jellomakker.cpubooster.compat.VulkanModDetector;
import com.jellomakker.cpubooster.config.ConfigManager;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import com.jellomakker.cpubooster.hud.DebugOverlayHud;
import com.jellomakker.cpubooster.key.Commands;
import com.jellomakker.cpubooster.key.Keybinds;
import com.jellomakker.cpubooster.metrics.ModCompatibilityDetector;
import com.jellomakker.cpubooster.metrics.PerformanceMetrics;
import com.jellomakker.cpubooster.optimize.ChunkRebuildLimiter;
import com.jellomakker.cpubooster.optimize.ChunkRebuildThrottler;
import com.jellomakker.cpubooster.optimize.DeferredTaskQueue;
import com.jellomakker.cpubooster.optimize.FramePacingThrottler;
import com.jellomakker.cpubooster.optimize.HudThrottler;
import com.jellomakker.cpubooster.optimize.StutterSmoother;
import com.jellomakker.cpubooster.optimize.FrameTimeVarianceOptimizer;
import com.jellomakker.cpubooster.optimize.SmartChunkRebuildThrottler;
import com.jellomakker.cpubooster.optimize.InvisibleEntityFreezer;
import com.jellomakker.cpubooster.optimize.AllocationPoolManager;
import com.jellomakker.cpubooster.optimize.InputRenderDecoupler;
import com.jellomakker.cpubooster.optimize.RenderStateDeduplicator;
import com.jellomakker.cpubooster.optimize.GpuBatchingOptimizer;
import com.jellomakker.cpubooster.optimize.DynamicResolutionScaler;
import com.jellomakker.cpubooster.optimize.BlockEntityColdStorage;
import com.jellomakker.cpubooster.patches.FramePacingPatch;
import com.jellomakker.cpubooster.patches.PatchManager;
import com.jellomakker.cpubooster.perf.FrameTimeTracker;
import com.jellomakker.cpubooster.perf.TickTimeTracker;
import com.jellomakker.cpubooster.profiles.OptimizationProfile;
import com.jellomakker.cpubooster.profiles.GameProfile;
import com.jellomakker.cpubooster.profiles.AdaptiveProfileDetector;
import com.jellomakker.cpubooster.memory.MemoryPressureMonitor;
import com.jellomakker.cpubooster.cache.CacheRegistry;
import com.jellomakker.cpubooster.block.BlockEntityUpdateLimiter;
import com.jellomakker.cpubooster.state.StateChangeCache;
import com.jellomakker.cpubooster.compat.AdvancedCompatibilityDetector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class CpuBoosterMod implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("cpubooster");
    private static final FrameTimeMonitor FRAME_TIME_MONITOR = new FrameTimeMonitor();
    private static final AdaptiveParticleGovernor GOVERNOR = new AdaptiveParticleGovernor();
    
    // Performance tracking
    private static final FrameTimeTracker FRAME_TIME_TRACKER = new FrameTimeTracker(120);
    private static final TickTimeTracker TICK_TIME_TRACKER = new TickTimeTracker(200);
    private static final StutterSmoother STUTTER_SMOOTHER = new StutterSmoother();
    private static final PerformanceMetrics PERFORMANCE_METRICS = new PerformanceMetrics();
    
    // Optimization systems
    private static final ChunkRebuildThrottler CHUNK_THROTTLER = new ChunkRebuildThrottler();
    private static final HudThrottler HUD_THROTTLER = new HudThrottler();
    private static final DeferredTaskQueue DEFERRED_TASK_QUEUE = new DeferredTaskQueue();
    
    // Frame pacing and chunk rebuild limiting
    private static final FramePacingThrottler FRAME_PACING_THROTTLER = new FramePacingThrottler();
    private static final ChunkRebuildLimiter CHUNK_REBUILD_LIMITER = new ChunkRebuildLimiter();
    private static final MemoryPressureMonitor MEMORY_PRESSURE_MONITOR = new MemoryPressureMonitor();
    private static final BlockEntityUpdateLimiter BLOCK_ENTITY_UPDATE_LIMITER = new BlockEntityUpdateLimiter();
    private static final StateChangeCache STATE_CHANGE_CACHE = new StateChangeCache();
    private static long lastCacheCleanupTime = 0;
    
    // ===== 10 MAJOR OPTIMIZATION SYSTEMS =====
    // Core mandatory features
    private static final FrameTimeVarianceOptimizer FRAME_TIME_VARIANCE_OPTIMIZER = new FrameTimeVarianceOptimizer();
    private static final SmartChunkRebuildThrottler SMART_CHUNK_THROTTLER = new SmartChunkRebuildThrottler();
    private static final InvisibleEntityFreezer ENTITY_FREEZER = new InvisibleEntityFreezer();
    
    // Memory and optimization
    private static final AllocationPoolManager ALLOCATION_POOL_MANAGER = new AllocationPoolManager();
    private static final InputRenderDecoupler INPUT_RENDER_DECOUPLER = new InputRenderDecoupler();
    
    // Profiling and adaptation
    private static final AdaptiveProfileDetector PROFILE_DETECTOR = new AdaptiveProfileDetector();
    
    // Experimental features (OFF by default)
    private static final RenderStateDeduplicator RENDER_STATE_DEDUP = new RenderStateDeduplicator();
    private static final GpuBatchingOptimizer GPU_BATCHING = new GpuBatchingOptimizer();
    private static final DynamicResolutionScaler RESOLUTION_SCALER = new DynamicResolutionScaler();
    private static final BlockEntityColdStorage BLOCK_ENTITY_STORAGE = new BlockEntityColdStorage();
    
    // Patch system
    private static final PatchManager PATCH_MANAGER = new PatchManager();

    @Override
    public void onInitializeClient() {
        LOGGER.info("=== MacFpsBoost Optimization Suite Init START ===");
        
        try {
            LOGGER.info("INIT STEP 1: Loading config...");
            CpuBoosterConfig cfg = ConfigManager.load();
            LOGGER.info("✓ Config loaded. Enabled={}", cfg.enabled);
        } catch (Exception e) {
            LOGGER.error("✗ FAILED at config load", e);
            throw new RuntimeException("Config load failed", e);
        }

        try {
            LOGGER.info("INIT STEP 2: System detection...");
            AppleSiliconDetector.logDetectionInfo();
            EnvironmentDetector.detectAndLog();
            ModCompatibilityDetector.logDetectedMods();
            VulkanModDetector.detectAndLog();
            AdvancedCompatibilityDetector.detectAndLog();
            LOGGER.info("✓ System & environment detection complete");
        } catch (Exception e) {
            LOGGER.warn("✗ System detection warning (non-critical)", e);
        }

        try {
            LOGGER.info("INIT STEP 3: Applying optimization profile: {}", ConfigManager.get().optimizationProfile);
            OptimizationProfile profile = OptimizationProfile.fromString(ConfigManager.get().optimizationProfile);
            if (profile != OptimizationProfile.CUSTOM) {
                CpuBoosterConfig cfg = ConfigManager.get();
                cfg.applyProfile(profile);
                ConfigManager.set(cfg);
                LOGGER.info("✓ Applied profile: {} - {}", profile.name(), profile.getDescription());
            } else {
                LOGGER.info("✓ Using custom profile");
            }
        } catch (Exception e) {
            LOGGER.warn("✗ FAILED at profile application (non-critical)", e);
        }

        try {
            LOGGER.info("INIT STEP 4: Initializing frame pacing and chunk rebuild limiter...");
            CpuBoosterConfig cfg = ConfigManager.get();
            FRAME_PACING_THROTTLER.updateConfig(cfg.frameTimeTargetMs, cfg.spikeThresholdMs, cfg.throttleCooldownMs);
            CHUNK_REBUILD_LIMITER.updateConfig(cfg.maxChunkRebuildsPerSecondNormal, cfg.maxChunkRebuildsPerSecondThrottled);
            MEMORY_PRESSURE_MONITOR.updateConfig(cfg);
            BLOCK_ENTITY_UPDATE_LIMITER.updateConfig(cfg.maxBlockEntityUpdatesPerTick, cfg.blockEntityUpdateLimiterEnabled);
            STATE_CHANGE_CACHE.updateConfig(cfg.stateChangeThresholdEnabled, cfg.cameraDeltaThreshold, cfg.rotationDeltaThreshold);
            // Register cache cleanup task if enabled
            if (cfg.enableCacheCleanup) {
                // Schedule periodic cleanup by registering a dummy handle to indicate active cleanup
                // Actual caches should register themselves with CacheRegistry.register(handle)
                LOGGER.info("Cache cleanup enabled: interval {}s", cfg.cacheCleanupIntervalSeconds);
            }
            LOGGER.info("✓ Frame pacing throttler and chunk rebuild limiter initialized");
        } catch (Exception e) {
            LOGGER.warn("✗ FAILED at frame pacing init (non-critical)", e);
        }

        try {
            LOGGER.info("INIT STEP 5: Initializing patch system...");
            CpuBoosterConfig cfg = ConfigManager.load();
            initializePatches(cfg);
            LOGGER.info("✓ Patch system initialized");
        } catch (Exception e) {
            LOGGER.error("✗ FAILED at patch initialization", e);
            throw new RuntimeException("Patch system initialization failed", e);
        }

        try {
            LOGGER.info("INIT STEP 6: Registering keybinds...");
            Keybinds.register();
            LOGGER.info("✓ Keybinds registered");
        } catch (Exception e) {
            LOGGER.error("✗ FAILED at keybind registration", e);
            throw new RuntimeException("Keybind registration failed", e);
        }

        try {
            LOGGER.info("INIT STEP 7: Registering client commands...");
            Commands.registerCommands();
            LOGGER.info("✓ Client commands registered");
        } catch (Exception e) {
            LOGGER.warn("✗ FAILED at command registration (non-critical)", e);
        }

        try {
            LOGGER.info("INIT STEP 8: Registering HUD renderer...");
            DebugOverlayHud debugHud = new DebugOverlayHud(FRAME_TIME_TRACKER, TICK_TIME_TRACKER, STUTTER_SMOOTHER,
                    PERFORMANCE_METRICS, CHUNK_THROTTLER);
            HudRenderCallback.EVENT.register(debugHud);
            LOGGER.info("✓ Debug overlay HUD registered");
        } catch (NoClassDefFoundError e) {
            LOGGER.debug("HUD rendering API not available (non-critical)");
        } catch (Exception e) {
            LOGGER.warn("✗ FAILED at HUD registration (non-critical)", e);
        }

        try {
            LOGGER.info("INIT STEP 9: Registering performance tracking and adaptive throttling...");
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client == null || client.player == null) return;
                CpuBoosterConfig cfg2 = ConfigManager.get();
                if (!cfg2.enabled) return;

                double frameTimeMs = FRAME_TIME_MONITOR.getLastFrameMs();
                FRAME_TIME_TRACKER.recordFrameTime(frameTimeMs);
                TICK_TIME_TRACKER.markTick();
                STUTTER_SMOOTHER.onFrame(frameTimeMs);
                STUTTER_SMOOTHER.notifyUserIfNeeded(cfg2);

                PERFORMANCE_METRICS.onFrameTime(frameTimeMs);
                PERFORMANCE_METRICS.checkGcStatus();
                CHUNK_THROTTLER.onFrameTime(frameTimeMs);
                HUD_THROTTLER.onTick();
                DEFERRED_TASK_QUEUE.onTick();

                // Frame pacing throttler
                if (cfg2.adaptiveThrottlingEnabled) {
                    boolean throttleActive = FRAME_PACING_THROTTLER.onFrameTime(frameTimeMs);
                    CHUNK_REBUILD_LIMITER.setThrottleMode(throttleActive);
                    if (cfg2.debugLogging && throttleActive) {
                        LOGGER.debug("Adaptive throttle active, spikes detected: {}", FRAME_PACING_THROTTLER.getSpikesDetected());
                    }
                }

                // Memory pressure monitor
                try {
                    if (cfg2.memoryPressureEnabled) {
                        MEMORY_PRESSURE_MONITOR.onTick();
                        if (MEMORY_PRESSURE_MONITOR.isUnderPressure()) {
                            // If under memory pressure, ensure rebuild limiter is in throttled mode
                            CHUNK_REBUILD_LIMITER.setThrottleMode(true);
                            if (cfg2.debugLogging) {
                                LOGGER.debug("Memory pressure active, remaining ms: {}", MEMORY_PRESSURE_MONITOR.getCooldownRemainingMs());
                            }
                        }
                    }
                } catch (Throwable t) {
                    CpuBoosterMod.LOGGER.warn("MemoryPressureMonitor disabled due to error: {}", t.getMessage());
                }

                // ===== 10 MAJOR OPTIMIZATION SYSTEMS TICK =====
                try {
                    // Feature 1: Frame-time variance optimizer
                    if (cfg2.enableFrameTimeVarianceOptimizer) {
                        FRAME_TIME_VARIANCE_OPTIMIZER.onFrame(frameTimeMs, cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("FrameTimeVarianceOptimizer disabled: {}", t.getMessage());
                }

                try {
                    // Feature 2: Smart chunk rebuild throttler
                    if (cfg2.enableSmartChunkRebuild) {
                        SMART_CHUNK_THROTTLER.onTick(cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("SmartChunkRebuildThrottler disabled: {}", t.getMessage());
                }

                try {
                    // Feature 3: Invisible entity freezer
                    if (cfg2.enableEntityFreezing) {
                        ENTITY_FREEZER.onTick(cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("InvisibleEntityFreezer disabled: {}", t.getMessage());
                }

                try {
                    // Feature 4: Allocation pooling
                    if (cfg2.enableAllocationPooling) {
                        ALLOCATION_POOL_MANAGER.updateConfig(cfg2);
                        ALLOCATION_POOL_MANAGER.onTick(cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("AllocationPoolManager disabled: {}", t.getMessage());
                }

                try {
                    // Feature 5: Input-render decoupler
                    if (cfg2.enableInputRenderDecoupling) {
                        INPUT_RENDER_DECOUPLER.onTick(cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("InputRenderDecoupler disabled: {}", t.getMessage());
                }

                try {
                    // Feature 6: Adaptive profile detection
                    if (cfg2.enableProfiles) {
                        PROFILE_DETECTOR.onTick(cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("AdaptiveProfileDetector disabled: {}", t.getMessage());
                }

                try {
                    // Feature 7: Render state deduplication (EXPERIMENTAL)
                    if (cfg2.enableRenderStateDedup) {
                        RENDER_STATE_DEDUP.onTick(cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("RenderStateDeduplicator disabled: {}", t.getMessage());
                }

                try {
                    // Feature 8: GPU batching (EXPERIMENTAL)
                    if (cfg2.enableGPUBatching) {
                        GPU_BATCHING.onFrameStart(cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("GpuBatchingOptimizer disabled: {}", t.getMessage());
                }

                try {
                    // Feature 9: Dynamic resolution scaling (EXPERIMENTAL)
                    if (cfg2.enableResolutionScaling) {
                        RESOLUTION_SCALER.onFrame(frameTimeMs, cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("DynamicResolutionScaler disabled: {}", t.getMessage());
                }

                try {
                    // Feature 10: Block entity cold storage
                    if (cfg2.enableBlockEntityColdStorage) {
                        BLOCK_ENTITY_STORAGE.onTick(cfg2);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("BlockEntityColdStorage disabled: {}", t.getMessage());
                }

                // Periodic cache cleanup
                try {
                    if (cfg2.enableCacheCleanup) {
                        long now = System.currentTimeMillis();
                        if (lastCacheCleanupTime == 0) lastCacheCleanupTime = now;
                        long intervalMs = cfg2.cacheCleanupIntervalSeconds * 1000L;
                        if (now - lastCacheCleanupTime >= intervalMs) {
                            lastCacheCleanupTime = now;
                            com.jellomakker.cpubooster.cache.CacheRegistry.cleanupAll();
                            if (cfg2.debugLogging) {
                                LOGGER.debug("CacheRegistry: performed scheduled cleanup");
                            }
                        }
                    }
                } catch (Throwable t) {
                    CpuBoosterMod.LOGGER.warn("Cache cleanup disabled due to error: {}", t.getMessage());
                }

                // Reset block-entity update budget each tick (start-of-tick would be better)
                try {
                    if (cfg2.blockEntityUpdateLimiterEnabled) {
                        BLOCK_ENTITY_UPDATE_LIMITER.resetBudget();
                    }
                } catch (Throwable t) {
                    CpuBoosterMod.LOGGER.warn("BlockEntityUpdateLimiter disabled due to error: {}", t.getMessage());
                }

                FramePacingPatch fp = PATCH_MANAGER.getFramePacingPatch();
                if (fp != null) {
                    fp.onFrameEnd();
                }
            });
            LOGGER.info("✓ Performance tracking and adaptive throttling registered");
        } catch (NoClassDefFoundError e) {
            LOGGER.debug("Tick event API not available (non-critical)");
        } catch (Exception e) {
            LOGGER.warn("✗ FAILED at performance tracking registration (non-critical)", e);
        }

        try {
            LOGGER.info("INIT STEP 10: Registering frame start callback...");
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                if (client != null) {
                    FRAME_TIME_MONITOR.markFrame();
                    FramePacingPatch fp = PATCH_MANAGER.getFramePacingPatch();
                    if (fp != null) {
                        fp.onFrameStart();
                    }
                    if (client.world != null) {
                        DEFERRED_TASK_QUEUE.setWorldReady(true);
                    }
                }
            });
            LOGGER.info("✓ Frame monitor registered");
        } catch (NoClassDefFoundError e) {
            LOGGER.debug("Frame tick event API not available (non-critical)");
        } catch (Exception e) {
            LOGGER.warn("✗ FAILED at frame start registration (non-critical)", e);
        }

        LOGGER.info("=== MacFpsBoost Init COMPLETE ===");
        logEnabledFeatures();
    }

    private void initializePatches(CpuBoosterConfig cfg) {
        for (String patchId : cfg.patches.keySet()) {
            PATCH_MANAGER.setPatchState(patchId, cfg.patches.get(patchId));
        }
        PATCH_MANAGER.initializeEnabledPatches();
        LOGGER.info("Patch system initialized");
    }

    private void logEnabledFeatures() {
        CpuBoosterConfig cfg = ConfigManager.get();
        LOGGER.info("=== Enabled Features ===");
        LOGGER.info("Core Mod: {}", cfg.enabled ? "✓" : "✗");
        LOGGER.info("Debug Overlay: {}", cfg.debugOverlayEnabled ? "✓" : "✗");
        LOGGER.info("Stutter Smoother: {}", cfg.stutterSmootherEnabled ? "✓" : "✗");
        LOGGER.info("Adaptive Chunk Throttle: {}", cfg.adaptiveChunkThrottle ? "✓" : "✗");
        LOGGER.info("Adaptive Frame Pacing Throttler: {}", cfg.adaptiveThrottlingEnabled ? "✓" : "✗");
        LOGGER.info("Metrics Collection: {}", cfg.metricsEnabled ? "✓" : "✗");
        LOGGER.info("Debug Logging: {}", cfg.debugLogging ? "✓" : "✗");
        LOGGER.info("Optimization Profile: {}", cfg.optimizationProfile);
    }

    public static FrameTimeMonitor getFrameTimeMonitor() { return FRAME_TIME_MONITOR; }
    public static AdaptiveParticleGovernor getGovernor() { return GOVERNOR; }
    public static FrameTimeTracker getFrameTimeTracker() { return FRAME_TIME_TRACKER; }
    public static TickTimeTracker getTickTimeTracker() { return TICK_TIME_TRACKER; }
    public static StutterSmoother getStutterSmoother() { return STUTTER_SMOOTHER; }
    public static PerformanceMetrics getPerformanceMetrics() { return PERFORMANCE_METRICS; }
    public static PatchManager getPatchManager() { return PATCH_MANAGER; }
    public static ChunkRebuildThrottler getChunkThrottler() { return CHUNK_THROTTLER; }
    public static HudThrottler getHudThrottler() { return HUD_THROTTLER; }
    public static DeferredTaskQueue getDeferredTaskQueue() { return DEFERRED_TASK_QUEUE; }
    public static FramePacingThrottler getFramePacingThrottler() { return FRAME_PACING_THROTTLER; }
    public static ChunkRebuildLimiter getChunkRebuildLimiter() { return CHUNK_REBUILD_LIMITER; }
    public static com.jellomakker.cpubooster.memory.MemoryPressureMonitor getMemoryPressureMonitor() { return MEMORY_PRESSURE_MONITOR; }
    public static com.jellomakker.cpubooster.block.BlockEntityUpdateLimiter getBlockEntityUpdateLimiter() { return BLOCK_ENTITY_UPDATE_LIMITER; }
    public static com.jellomakker.cpubooster.state.StateChangeCache getStateChangeCache() { return STATE_CHANGE_CACHE; }
}
