package com.jellomakker.macfpsboost;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MacFpsBoostMod implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("macfpsboost");
    private static final FrameTimeMonitor FRAME_TIME_MONITOR = new FrameTimeMonitor();
    private static final AdaptiveParticleGovernor GOVERNOR = new AdaptiveParticleGovernor();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Mac FPS Boost loaded! Frame time monitoring active.");
    }

    public static FrameTimeMonitor getFrameTimeMonitor() {
        return FRAME_TIME_MONITOR;
    }

    public static AdaptiveParticleGovernor getGovernor() {
        return GOVERNOR;
    }
}
