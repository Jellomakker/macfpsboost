package com.jellomakker.macfpsboost.mixin;

import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.Camera;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Shadow
    public Camera camera;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderBlockEntity(BlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        try {
            if (camera == null) return;
            Vec3d cam = camera.getPos();
            double dx = cam.x - (blockEntity.getPos().getX() + 0.5);
            double dy = cam.y - (blockEntity.getPos().getY() + 0.5);
            double dz = cam.z - (blockEntity.getPos().getZ() + 0.5);
            double dist2 = dx*dx + dy*dy + dz*dz;
            if (dist2 > 2304.0) { // 48 * 48
                ci.cancel();
            }
        } catch (Throwable ignored) {
            // safe fail
        }
    }
}
