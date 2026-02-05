package com.jellomakker.macfpsboost.mixin;

import com.jellomakker.macfpsboost.AdaptiveParticleGovernor;
import com.jellomakker.macfpsboost.MacFpsBoostMod;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
    private void onAddParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        try {
            AdaptiveParticleGovernor.Level level = MacFpsBoostMod.getGovernor().getLevel();
            if (level == AdaptiveParticleGovernor.Level.MINIMAL) {
                ci.cancel();
            } else if (level == AdaptiveParticleGovernor.Level.DECREASED && Math.random() < 0.6) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
            // safe fail - allow particle to spawn
        }
    }
}
