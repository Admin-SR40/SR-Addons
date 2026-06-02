package com.sraddons.mixin

import com.sraddons.config.SRConfig
import net.minecraft.client.Camera
import net.minecraft.world.level.material.FogType
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(Camera::class)
abstract class BetterFovCameraMixin {

    @Inject(method = ["getFluidInCamera"], at = [At("TAIL")], cancellable = true)
    private fun onGetFluidInCamera(cir: CallbackInfoReturnable<FogType>) {
        if (SRConfig.settings.general.betterFov) {
            cir.returnValue = FogType.NONE
        }
    }
}
