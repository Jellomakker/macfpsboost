package com.jellomakker.macfpsboost.mixin;

import com.jellomakker.macfpsboost.MacFpsBoostMod;

// NOTE: This mixin is a stub. Target class and method names need mapping.

public class ChunkRebuildMixin {
    // Example pseudo-hook: replace direct scheduling with enqueuing to our budgeter.
    private void scheduleChunkRebuild(Runnable rebuildTask) {
        MacFpsBoostMod.getRebuildBudgeter().enqueue(rebuildTask);
    }
}
