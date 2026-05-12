package com.sraddons.mixin

import com.sraddons.config.SRConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.world.entity.LivingEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(LivingEntityRenderer::class)
abstract class ShowOwnNametagMixin {

    @Inject(method = ["shouldShowName"], at = [At("HEAD")], cancellable = true)
    private fun onShouldShowName(entity: LivingEntity, distance: Double, cir: CallbackInfoReturnable<Boolean>) {
        if (!SRConfig.settings.general.showOwnNameInThirdPerson) return
        val mc = Minecraft.getInstance()
        if (entity === mc.cameraEntity && !mc.options.cameraType.isFirstPerson) {
            cir.returnValue = true
        }
    }
}
