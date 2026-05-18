package com.sraddons.mixin

import com.sraddons.config.SRConfig
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(LivingEntityRenderer::class)
abstract class HiddenEntityFireMixin {

    @Inject(method = ["submit"], at = [At("HEAD")])
    private fun onSubmit(
        state: LivingEntityRenderState,
        matrices: com.mojang.blaze3d.vertex.PoseStack,
        collector: net.minecraft.client.renderer.SubmitNodeCollector,
        cameraState: net.minecraft.client.renderer.state.CameraRenderState,
        ci: CallbackInfo
    ) {
        if (SRConfig.settings.entityFire.hiddenFire) {
            (state as EntityRenderState).displayFireAnimation = false
        }
    }
}
