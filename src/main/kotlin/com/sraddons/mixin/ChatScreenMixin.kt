package com.sraddons.mixin

import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.commands.Commands
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ChatScreen
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ChatScreen::class)
class ChatScreenMixin {

    private val LOGGER = LogManager.getLogger("SR-Addons-ChatScreen")

    @Inject(method = ["handleChatInput"], at = [At("HEAD")], cancellable = true)
    private fun onSendMessage(message: String, addToHistory: Boolean, ci: CallbackInfo) {
        val prefix = SRConfig.settings.partyCommands.prefix
        if (message.startsWith(prefix)) {
            if (addToHistory && message.isNotEmpty()) {
                Minecraft.getInstance().gui.chat.addRecentChat(message)
            }

            try {
                Commands.dispatch(message.substring(prefix.length))
                ci.cancel()
            } catch (e: CommandSyntaxException) {
                LOGGER.warn("Invalid party command: {}", message, e)
            }
        }
    }
}
