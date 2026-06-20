package com.sraddons.util

import net.minecraft.network.protocol.game.ClientboundSoundPacket
import java.util.concurrent.ConcurrentHashMap

object SoundPacketHandler {
    private val listeners = ConcurrentHashMap.newKeySet<(ClientboundSoundPacket) -> Unit>()

    fun register(listener: (ClientboundSoundPacket) -> Unit) {
        listeners += listener
    }

    fun unregister(listener: (ClientboundSoundPacket) -> Unit) {
        listeners -= listener
    }

    fun dispatch(packet: ClientboundSoundPacket) {
        listeners.forEach { it(packet) }
    }
}
