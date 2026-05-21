package com.sraddons.util

import net.minecraft.network.protocol.game.ClientboundSoundPacket

object SoundPacketHandler {
    private val listeners = mutableListOf<(ClientboundSoundPacket) -> Unit>()

    fun register(listener: (ClientboundSoundPacket) -> Unit) {
        listeners += listener
    }

    fun dispatch(packet: ClientboundSoundPacket) {
        listeners.forEach { it(packet) }
    }
}
