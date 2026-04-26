package com.sraddons.mixin;

import com.sraddons.config.SRConfig;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class HiddenEntityFireMixin {

    @Inject(method = "submit", at = @At("HEAD"))
    private void onSubmit(LivingEntityRenderState state,
                          com.mojang.blaze3d.vertex.PoseStack matrices,
                          net.minecraft.client.renderer.SubmitNodeCollector collector,
                          net.minecraft.client.renderer.state.CameraRenderState cameraState,
                          CallbackInfo ci) {
        if (SRConfig.INSTANCE.getSettings().getEntityFire().getHiddenFire()) {
            ((EntityRenderState) state).displayFireAnimation = false;
        }
    }
}
