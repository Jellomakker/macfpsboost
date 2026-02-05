package com.jellomakker.macfpsboost.mixin;

import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderBlockEntity(BlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        // compute distance squared to camera and cancel if too far
        try {
            Vec3d cam = ((BlockEntityRenderDispatcher) (Object) this).camera.getPos();
            double dx = cam.x - (blockEntity.getPos().getX() + 0.5);
            double dy = cam.y - (blockEntity.getPos().getY() + 0.5);
            double dz = cam.z - (blockEntity.getPos().getZ() + 0.5);
            double dist2 = dx*dx + dy*dy + dz*dz;
            double threshold = 48.0 * 48.0;
            if (dist2 > threshold) ci.cancel();
        } catch (Throwable t) {
            // ignore and allow render to proceed if reflection fails
        }
    }
}
