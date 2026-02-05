package com.jellomakker.cpubooster.hud;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.ConfigManager;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import com.jellomakker.cpubooster.metrics.PerformanceMetrics;
import com.jellomakker.cpubooster.optimize.ChunkRebuildThrottler;
import com.jellomakker.cpubooster.perf.FrameTimeTracker;
import com.jellomakker.cpubooster.perf.TickTimeTracker;
import com.jellomakker.cpubooster.optimize.StutterSmoother;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/**
 * Renders a debug overlay showing performance metrics including chunk throttling.
 */
public class DebugOverlayHud implements HudRenderCallback {
    private final FrameTimeTracker frameTimeTracker;
    private final TickTimeTracker tickTimeTracker;
    private final StutterSmoother stutterSmoother;
    private final PerformanceMetrics performanceMetrics;
    private final ChunkRebuildThrottler chunkThrottler;

    public DebugOverlayHud(FrameTimeTracker frameTimeTracker, TickTimeTracker tickTimeTracker, 
                           StutterSmoother stutterSmoother, PerformanceMetrics performanceMetrics,
                           ChunkRebuildThrottler chunkThrottler) {
        this.frameTimeTracker = frameTimeTracker;
        this.tickTimeTracker = tickTimeTracker;
        this.stutterSmoother = stutterSmoother;
        this.performanceMetrics = performanceMetrics;
        this.chunkThrottler = chunkThrottler;
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) return;

            CpuBoosterConfig cfg = ConfigManager.get();
            if (!cfg.debugOverlayEnabled) return;

            String enabled = cfg.enabled ? "ON" : "OFF";
            String smoothing = stutterSmoother.isSmoothing() ? " [SMOOTHING]" : "";
            String bottleneck = performanceMetrics.getBottleneckAnalysis();
            int chunkBudget = chunkThrottler.getChunkBudget();
            
            double avgFrameTime = frameTimeTracker.getAverageFrameTime();
            double p1FrameTime = frameTimeTracker.get1PercentLow();
            double avgTickTime = tickTimeTracker.getAverageTickTime();

            int x = 2;
            int y = 2;

            drawContext.drawTextWithBackground(
                    client.textRenderer,
                    Text.literal("MacFPSBoost: " + enabled + smoothing),
                    x, y,
                    0xFFFFFF,
                    0x000000
            );
            y += 10;

            drawContext.drawTextWithBackground(
                    client.textRenderer,
                    Text.literal(String.format("Avg Frame: %.1f ms", avgFrameTime)),
                    x, y,
                    0xFFFFFF,
                    0x000000
            );
            y += 10;

            drawContext.drawTextWithBackground(
                    client.textRenderer,
                    Text.literal(String.format("1%% Low: %.1f ms", p1FrameTime)),
                    x, y,
                    0xFFFFFF,
                    0x000000
            );
            y += 10;

            drawContext.drawTextWithBackground(
                    client.textRenderer,
                    Text.literal(String.format("Avg Tick: %.1f ms", avgTickTime)),
                    x, y,
                    0xFFFFFF,
                    0x000000
            );
            y += 10;

            drawContext.drawTextWithBackground(
                    client.textRenderer,
                    Text.literal("Bottleneck: " + bottleneck),
                    x, y,
                    0xFFFFFF,
                    0x000000
            );
            y += 10;

            drawContext.drawTextWithBackground(
                    client.textRenderer,
                    Text.literal("Chunk Budget: " + chunkBudget),
                    x, y,
                    0xFFFF00,
                    0x000000
            );
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.debug("Debug overlay render error: {}", e.getMessage());
        }
    }
}
