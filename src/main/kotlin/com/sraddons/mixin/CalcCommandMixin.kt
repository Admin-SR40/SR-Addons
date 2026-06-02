package com.sraddons.mixin

import com.sraddons.config.SRConfig
import com.sraddons.util.CalcUtil
import com.sraddons.util.Constants
import com.sraddons.util.TitleUtil.parseColorCodes
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.network.chat.Component
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ChatScreen::class)
class CalcCommandMixin {

    @Inject(method = ["handleChatInput"], at = [At("HEAD")], cancellable = true)
    private fun onChatInput(message: String, addToHistory: Boolean, ci: CallbackInfo) {
        if (!SRConfig.settings.general.enableStandaloneCalc) return
        if (!message.startsWith("/calc")) return
        if (message.length > 5 && message[5] != ' ') return

        val expr = message.removePrefix("/calc").trim()
        if (expr.isEmpty()) return

        if (addToHistory) {
            Minecraft.getInstance().gui.chat.addRecentChat(message)
        }

        try {
            val result = CalcUtil.evaluate(expr)
            val prefix = Constants.makePrefix()
            Minecraft.getInstance().gui?.chat?.addMessage(
                prefix.copy()
                    .append(Component.literal(parseColorCodes("&7$expr = &a${CalcUtil.format(result)}")))
            )
        } catch (_: Exception) {
            val prefix = Constants.makePrefix()
            Minecraft.getInstance().gui?.chat?.addMessage(
                prefix.copy()
                    .append(Component.translatable("sraddons.command.calc.error").withColor(0xFF5555))
            )
        }
        ci.cancel()
    }
}
