package com.jellomakker.macfpsboost.mixin;

import com.jellomakker.macfpsboost.AdaptiveParticleGovernor;
import com.jellomakker.macfpsboost.MacFpsBoostMod;
import com.jellomakker.macfpsboost.FrameTimeMonitor;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    private final AdaptiveParticleGovernor governor = new AdaptiveParticleGovernor();
    private final FrameTimeMonitor ftm = MacFpsBoostMod.getFrameTimeMonitor();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // mark frame / tick time
        ftm.markFrame();
        double fps = 1000.0 / Math.max(1.0, ftm.getLastFrameMs());
        governor.tick((MinecraftClient) (Object) this, fps);
        // store governor on mod singleton for other mixins to read
        MacFpsBoostMod.setGovernor(governor);
    }
}
