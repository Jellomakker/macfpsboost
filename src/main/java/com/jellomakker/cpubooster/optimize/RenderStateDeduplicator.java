package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;

/**
 * EXPERIMENTAL: Deduplicates render state changes.
 * Tracks the last applied render state and skips redundant state applications.
 * WARNING: This is experimental and disabled by default. Enable at your own risk.
 */
public class RenderStateDeduplicator {
    private static class RenderState {
        int blendFuncSrc;
        int blendFuncDst;
        int depthFunc;
        boolean depthTestEnabled;
        boolean blendEnabled;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RenderState)) return false;
            RenderState other = (RenderState) obj;
            return blendFuncSrc == other.blendFuncSrc &&
                    blendFuncDst == other.blendFuncDst &&
                    depthFunc == other.depthFunc &&
                    depthTestEnabled == other.depthTestEnabled &&
                    blendEnabled == other.blendEnabled;
        }

        @Override
        public int hashCode() {
            return blendFuncSrc ^ blendFuncDst ^ depthFunc ^ (depthTestEnabled ? 1 : 0) ^ (blendEnabled ? 2 : 0);
        }
    }

    private RenderState lastAppliedState = new RenderState();
    private int stateSavings = 0;

    public void onTick(CpuBoosterConfig cfg) {
        if (!cfg.enableRenderStateDedup) {
            return;
        }

        try {
            // This would be called periodically to reset state tracking
            if (stateSavings > 10000) {
                // Reset counter to prevent overflow
                stateSavings = 0;
            }
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("RenderStateDeduplicator error: {}", e.getMessage());
        }
    }

    /**
     * Checks if a state change would actually affect rendering.
     * Returns true if state is different from last applied state.
     */
    public boolean shouldApplyRenderState(int blendSrc, int blendDst, int depthFunc, 
                                         boolean depthTestEnabled, boolean blendEnabled) {
        RenderState newState = new RenderState();
        newState.blendFuncSrc = blendSrc;
        newState.blendFuncDst = blendDst;
        newState.depthFunc = depthFunc;
        newState.depthTestEnabled = depthTestEnabled;
        newState.blendEnabled = blendEnabled;

        if (newState.equals(lastAppliedState)) {
            stateSavings++;
            return false;
        }

        lastAppliedState = newState;
        return true;
    }

    public int getStateSavings() {
        return stateSavings;
    }

    public void reset() {
        stateSavings = 0;
    }

    public String getDebugInfo() {
        return "RenderStateDedup: " + stateSavings + " state changes skipped";
    }
}
