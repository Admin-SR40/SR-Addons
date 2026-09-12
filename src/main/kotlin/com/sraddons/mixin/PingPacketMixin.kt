package com.sraddons.mixin

import com.sraddons.feature.partycommands.utils.ServerUtils
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl
import net.minecraft.network.protocol.common.ClientboundPingPacket
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * The server sends one [ClientboundPingPacket] per server tick, which makes it a
 * reliable server-tick signal independent of the client frame rate.
 *
 * Runs on the network thread — [ServerUtils.onServerTick] is thread-safe.
 */
@Mixin(ClientCommonPacketListenerImpl::class)
abstract class PingPacketMixin {

    @Inject(method = ["handlePing"], at = [At("HEAD")])
    private fun onPing(packet: ClientboundPingPacket, ci: CallbackInfo) {
        ServerUtils.onServerTick(packet.id)
    }
}
