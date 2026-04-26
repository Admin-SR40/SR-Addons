package com.sraddons.mixin

import com.sraddons.feature.partycommands.utils.PartyListHandler
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ClientPacketListener::class)
class ChatMixin {

    @Inject(method = ["handleSystemChat"], at = [At("HEAD")], cancellable = true)
    private fun onSystemChat(packet: ClientboundSystemChatPacket, ci: CallbackInfo) {
        val text = packet.content().string
        if (PartyListHandler.handleMessage(text)) {
            ci.cancel()
        }
    }
}
