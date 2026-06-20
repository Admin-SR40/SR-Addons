package com.sraddons.mixin

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.sraddons.config.SRConfig
import net.minecraft.client.renderer.Lightmap
import net.minecraft.client.renderer.state.LightmapRenderState
import net.minecraft.util.ARGB
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(Lightmap::class)
abstract class FullbrightMixin {

    @Accessor("texture")
    abstract fun getLightmapTexture(): GpuTexture

    @Inject(method = ["render"], at = [At("TAIL")])
    private fun onRender(state: LightmapRenderState, ci: CallbackInfo) {
        if (SRConfig.settings.general.fullbright) {
            RenderSystem.getDevice().createCommandEncoder()
                .clearColorTexture(getLightmapTexture(), ARGB.color(255, 255, 255, 255))
        }
    }
}
