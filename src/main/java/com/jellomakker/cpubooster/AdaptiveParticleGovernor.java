package com.jellomakker.cpubooster;

import net.minecraft.client.MinecraftClient;

public class AdaptiveParticleGovernor {
    public enum Level { ALL, DECREASED, MINIMAL }

    private Level level = Level.ALL;
    private double smoothedFps = 60.0;
    private final double alpha = 0.08; // smoothing factor
    private final int cooldownTicks = 40;
    private int cooldown = 0;
    private final double targetFps = 60.0;

    public synchronized void tick(MinecraftClient client, double currentFps) {
        smoothedFps = alpha * currentFps + (1.0 - alpha) * smoothedFps;
        if (cooldown > 0) cooldown--;

        if (cooldown == 0) {
            if (smoothedFps < targetFps * 0.85) {
                // degrade
                if (level == Level.ALL) { level = Level.DECREASED; cooldown = cooldownTicks; }
                else if (level == Level.DECREASED) { level = Level.MINIMAL; cooldown = cooldownTicks; }
            } else if (smoothedFps > targetFps * 1.05) {
                // restore
                if (level == Level.MINIMAL) { level = Level.DECREASED; cooldown = cooldownTicks; }
                else if (level == Level.DECREASED) { level = Level.ALL; cooldown = cooldownTicks; }
            }
        }
    }

    public synchronized Level getLevel() { return level; }
}
