package com.sraddons.mixin

import com.sraddons.feature.partycommands.utils.ServerUtils
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ClientPacketListener::class)
class ClientPacketListenerMixin {

    @Inject(method = ["handlePongResponse"], at = [At("HEAD")])
    private fun onPongResponse(packet: ClientboundPongResponsePacket, ci: CallbackInfo) {
        ServerUtils.onPongResponse(packet.time)
    }
}
