package com.sraddons.mixin

import com.sraddons.feature.helper.TextReplacer
import net.minecraft.client.gui.Font
import net.minecraft.util.FormattedCharSequence
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyVariable

@Mixin(Font::class)
abstract class TextReplaceMixin {

    @ModifyVariable(
        method = ["prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font${'$'}PreparedText;"],
        at = At("HEAD"),
        argsOnly = true
    )
    private fun onPrepareTextSequence(seq: FormattedCharSequence): FormattedCharSequence {
        return TextReplacer.replaceFormattedSeq(seq)
    }

    @ModifyVariable(
        method = ["width(Lnet/minecraft/util/FormattedCharSequence;)I"],
        at = At("HEAD"),
        argsOnly = true
    )
    private fun onWidthSequence(seq: FormattedCharSequence): FormattedCharSequence {
        return TextReplacer.replaceFormattedSeq(seq)
    }
}