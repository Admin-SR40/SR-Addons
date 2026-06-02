package com.sraddons.mixin

import com.sraddons.config.SRConfig
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Items
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyArg

private const val SPRINT_MULTIPLIER = 1.30000001192092896

@Mixin(AbstractClientPlayer::class)
abstract class BetterFovPlayerMixin {

    @ModifyArg(
        method = ["getFieldOfViewModifier"],
        at = At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/Mth;lerp(FFF)F"
        ),
        index = 2
    )
    private fun modifyFovModifierArg(f: Float): Float {
        if (!SRConfig.settings.general.betterFov) return f
        val player = this as AbstractClientPlayer

        val attr = player.getAttribute(Attributes.MOVEMENT_SPEED) ?: return f
        val value = attr.value
        val walkingSpeed = player.abilities.walkingSpeed.toDouble()
        val hasSpeedEffect = value != walkingSpeed
        val isAiming = player.isUsingItem && player.useItem.`is`(Items.BOW)

        if (!hasSpeedEffect && !isAiming) return f

        var result = f.toDouble()

        if (hasSpeedEffect && walkingSpeed > 0.0) {
            val speedFactor = (value / walkingSpeed + 1.0) / 2.0
            if (speedFactor <= 0.0) return f

            result /= speedFactor

            val cleanValue = if (player.isSprinting) {
                walkingSpeed * SPRINT_MULTIPLIER
            } else {
                walkingSpeed
            }
            val cleanSpeedFactor = (cleanValue / walkingSpeed + 1.0) / 2.0
            result *= cleanSpeedFactor
        }

        if (isAiming) {
            val ticks = player.ticksUsingItem
            var g = ticks / 20.0f
            g = if (g > 1.0f) 1.0f else g * g
            val aimingFactor = 1.0 - g * 0.15
            if (aimingFactor > 0.0) {
                result /= aimingFactor
            }
        }

        return result.toFloat().coerceAtLeast(1.0f)
    }
}
