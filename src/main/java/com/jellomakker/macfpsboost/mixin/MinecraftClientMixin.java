package com.jellomakker.macfpsboost.mixin;

import com.jellomakker.macfpsboost.MacFpsBoostMod;
import com.jellomakker.macfpsboost.FrameTimeMonitor;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        try {
            FrameTimeMonitor ftm = MacFpsBoostMod.getFrameTimeMonitor();
            ftm.markFrame();
            double fps = 1000.0 / Math.max(1.0, ftm.getLastFrameMs());
            MacFpsBoostMod.getGovernor().tick((MinecraftClient) (Object) this, fps);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
