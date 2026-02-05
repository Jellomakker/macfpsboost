package com.jellomakker.cpubooster.optimize;

import net.minecraft.text.Text;

/**
 * Allocation reduction system: caches constant Text objects and provides
 * safe object reuse patterns to reduce garbage collection pressure.
 *
 * Avoids per-tick Text allocations in our own code.
 */
public class AllocationOptimizer {
    // Cached constant Text objects
    private static final Text TEXT_MOD_ENABLED = Text.literal("§6MacFPSBoost§r: §aON");
    private static final Text TEXT_MOD_DISABLED = Text.literal("§6MacFPSBoost§r: §cOFF");
    private static final Text TEXT_THROTTLING = Text.literal("§e[THROTTLING]");
    private static final Text TEXT_SMOOTHING = Text.literal("§e[SMOOTHING]");
    private static final Text TEXT_NO_THROTTLE = Text.literal("§a[Ready]");

    private static final Text TEXT_FPS_STABLE = Text.literal("§aFPS Stable");
    private static final Text TEXT_FPS_SPIKE = Text.literal("§cFPS Spike");
    private static final Text TEXT_CHUNK_REBUILD_NORMAL = Text.literal("§a[Normal]");
    private static final Text TEXT_CHUNK_REBUILD_THROTTLED = Text.literal("§c[Throttled]");

    // Debug labels (reused)
    private static final Text TEXT_LABEL_AVG_FRAME = Text.literal("Avg Frame: ");
    private static final Text TEXT_LABEL_1PCT_LOW = Text.literal("1%% Low: ");
    private static final Text TEXT_LABEL_AVG_TICK = Text.literal("Avg Tick: ");
    private static final Text TEXT_LABEL_BOTTLENECK = Text.literal("Bottleneck: ");
    private static final Text TEXT_LABEL_CHUNK_BUDGET = Text.literal("Chunk Budget: ");
    private static final Text TEXT_LABEL_STATUS = Text.literal("Status: ");

    /**
     * Get cached Text for mod enabled status.
     */
    public static Text getEnabledText(boolean enabled) {
        return enabled ? TEXT_MOD_ENABLED : TEXT_MOD_DISABLED;
    }

    /**
     * Get cached Text for throttling indicator.
     */
    public static Text getThrottleIndicator(boolean throttling) {
        return throttling ? TEXT_THROTTLING : TEXT_NO_THROTTLE;
    }

    /**
     * Get cached Text for smoothing indicator.
     */
    public static Text getSmoothingIndicator(boolean smoothing) {
        return smoothing ? TEXT_SMOOTHING : Text.literal("");
    }

    /**
     * Get cached Text for FPS status.
     */
    public static Text getFpsStatusText(boolean spike) {
        return spike ? TEXT_FPS_SPIKE : TEXT_FPS_STABLE;
    }

    /**
     * Get cached Text for chunk rebuild status.
     */
    public static Text getChunkRebuildStatusText(boolean throttled) {
        return throttled ? TEXT_CHUNK_REBUILD_THROTTLED : TEXT_CHUNK_REBUILD_NORMAL;
    }

    /**
     * Get cached label for debug output (reuse rather than creating new Text).
     */
    public static Text getLabelAvgFrame() {
        return TEXT_LABEL_AVG_FRAME;
    }

    public static Text getLabel1PctLow() {
        return TEXT_LABEL_1PCT_LOW;
    }

    public static Text getLabelAvgTick() {
        return TEXT_LABEL_AVG_TICK;
    }

    public static Text getLabelBottleneck() {
        return TEXT_LABEL_BOTTLENECK;
    }

    public static Text getLabelChunkBudget() {
        return TEXT_LABEL_CHUNK_BUDGET;
    }

    public static Text getLabelStatus() {
        return TEXT_LABEL_STATUS;
    }

    /**
     * Format a frame time value as Text without allocating a new object if possible.
     * Note: Value formatting always creates new Text; use sparingly.
     */
    public static Text formatFrameTime(double ms) {
        return Text.literal(String.format("%.1f ms", ms));
    }

    public static Text formatChunkBudget(int budget) {
        return Text.literal(String.valueOf(budget));
    }
}
