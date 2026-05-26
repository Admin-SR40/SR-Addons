package com.sraddons.mixin

import com.sraddons.feature.helper.TextReplacer
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyVariable

@Mixin(Font::class)
abstract class TextReplaceMixin {

    @ModifyVariable(
        method = ["prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font${'\$'}PreparedText;"],
        at = At("HEAD"),
        argsOnly = true
    )
    private fun onPrepareTextString(text: String): String {
        return TextReplacer.replace(text)
    }

    @ModifyVariable(
        method = ["prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font${'\$'}PreparedText;"],
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

    @ModifyVariable(
        method = ["width(Lnet/minecraft/network/chat/FormattedText;)I"],
        at = At("HEAD"),
        argsOnly = true
    )
    private fun onWidthText(text: FormattedText): FormattedText {
        return if (text is Component)
            Component.literal(TextReplacer.replace(text.string))
        else text
    }

    @ModifyVariable(
        method = ["width(Ljava/lang/String;)I"],
        at = At("HEAD"),
        argsOnly = true
    )
    private fun onWidthString(text: String): String {
        return TextReplacer.replace(text)
    }
}
