package com.jellomakker.macfpsboost;

import net.fabricmc.api.ClientModInitializer;

public class MacFpsBoostMod implements ClientModInitializer {

    private static final FrameTimeMonitor FRAME_TIME_MONITOR = new FrameTimeMonitor();
    private static final DynamicScaler DYNAMIC_SCALER = new DynamicScaler();
    private static final ChunkRebuildBudgeter REBUILD_BUDGETER = new ChunkRebuildBudgeter();
    private static AdaptiveParticleGovernor GOVERNOR = new AdaptiveParticleGovernor();

    @Override
    public void onInitializeClient() {
        // TODO: register tick callbacks / mixin hooks where needed.
        // Mixins will inject into render loop and chunk rebuild scheduler.
    }

    public static FrameTimeMonitor getFrameTimeMonitor() { return FRAME_TIME_MONITOR; }
    public static DynamicScaler getDynamicScaler() { return DYNAMIC_SCALER; }
    public static ChunkRebuildBudgeter getRebuildBudgeter() { return REBUILD_BUDGETER; }
    public static AdaptiveParticleGovernor getGovernor() { return GOVERNOR; }
    public static void setGovernor(AdaptiveParticleGovernor g) { GOVERNOR = g; }
}
