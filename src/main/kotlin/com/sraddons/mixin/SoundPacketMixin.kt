package com.sraddons.mixin

import com.sraddons.feature.ragnarock.RagnarockNotifier
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ClientPacketListener::class)
class SoundPacketMixin {

    @Inject(method = ["handleSoundEvent"], at = [At("HEAD")])
    private fun onSoundEvent(packet: ClientboundSoundPacket, ci: CallbackInfo) {
        RagnarockNotifier.onSoundPacket(packet)
    }
}
