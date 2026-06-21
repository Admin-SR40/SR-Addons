package com.sraddons.mixin

import com.sraddons.feature.tooltip.PinnedTooltipManager
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.Optional

@Mixin(GuiGraphicsExtractor::class)
abstract class TooltipCaptureMixin {

    @Inject(
        method = ["setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"],
        at = [At("HEAD")]
    )
    private fun onSetTooltip(
        font: Font,
        lines: List<Component>,
        tooltip: Optional<net.minecraft.world.inventory.tooltip.TooltipComponent>,
        x: Int,
        y: Int,
        bg: Identifier?,
        ci: CallbackInfo
    ) {
        if (PinnedTooltipManager.captureNext && lines.isNotEmpty()) {
            PinnedTooltipManager.capture(lines)
        }
    }
}
