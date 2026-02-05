package com.jellomakker.macfpsboost.mixin;

import com.jellomakker.macfpsboost.MacFpsBoostMod;
import com.jellomakker.macfpsboost.FrameTimeMonitor;
import com.jellomakker.macfpsboost.DynamicScaler;

// NOTE: This is a mixin stub. The @Mixin target and injection points
// must be adjusted to match mappings for Minecraft 1.21.8.

public class RenderLoopMixin {
    // Example pseudo-hook called every frame by an injected callback.
    private void onRenderFrame() {
        FrameTimeMonitor ftm = MacFpsBoostMod.getFrameTimeMonitor();
        ftm.markFrame();

        double frameMs = ftm.getLastFrameMs();
        double targetMs = 16.6;
        DynamicScaler scaler = MacFpsBoostMod.getDynamicScaler();

        if (frameMs > targetMs * 1.10) {
            scaler.decreaseScale(0.02);
            MacFpsBoostMod.getRebuildBudgeter().decreaseBudget(1);
        } else if (frameMs < targetMs * 0.90) {
            scaler.increaseScale(0.01);
            MacFpsBoostMod.getRebuildBudgeter().increaseBudget(1);
        }

        scaler.applyScale();
        MacFpsBoostMod.getRebuildBudgeter().drainAndRun();
    }
}
