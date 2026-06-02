package com.sraddons.mixin

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.sraddons.config.SRConfig
import net.minecraft.client.renderer.LightTexture
import net.minecraft.util.ARGB
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(LightTexture::class)
abstract class FullbrightMixin {

    @Shadow
    @Final
    lateinit var texture: GpuTexture

    @Inject(method = ["updateLightTexture"], at = [At("TAIL")])
    private fun onUpdateLightTexture(tickDelta: Float, ci: CallbackInfo) {
        if (SRConfig.settings.general.fullbright) {
            RenderSystem.getDevice().createCommandEncoder()
                .clearColorTexture(texture, ARGB.color(255, 255, 255, 255))
        }
    }
}
