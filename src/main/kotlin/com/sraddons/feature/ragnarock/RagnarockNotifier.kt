package com.sraddons.feature.ragnarock

import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.modMessage
import com.sraddons.feature.partycommands.utils.sendPartyChat
import com.sraddons.util.TitleUtil
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemLore

object RagnarockNotifier {
    private val config get() = SRConfig.settings.helper.ragnarock

    private val cancelRegex = Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")

    private const val RAGNAROCK_PITCH = 1.4920635f
    private const val RAGNAROCK_AXE_ID = "RAGNAROCK_AXE"
    private const val CAST_COOLDOWN_MS = 1000L

    private var lastCastTime = 0L

    private val strengthRegex = Regex("Strength: \\+(\\d+)")

    private val ItemStack.itemId: String
        get() {
            val customData = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            return customData.copyTag().getString("id").orElse("")
        }

    private val ItemStack.strength: Int?
        get() {
            val lore = getOrDefault(DataComponents.LORE, ItemLore.EMPTY)
            return lore.styledLines().firstOrNull { it.string.startsWith("Strength:") }
                ?.let { line -> strengthRegex.find(line.string)?.groupValues?.get(1)?.toIntOrNull() }
        }

    fun init() {
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            handleChatMessage(message.string)
        }
    }

    fun onSoundPacket(packet: ClientboundSoundPacket) {
        if (!config.enabled || !config.castNotification) return
        if (packet.pitch != RAGNAROCK_PITCH) return

        val now = System.currentTimeMillis()
        if (now - lastCastTime < CAST_COOLDOWN_MS) return

        val player = Minecraft.getInstance().player ?: return
        val mainHand = player.mainHandItem
        if (mainHand.itemId != RAGNAROCK_AXE_ID) return

        val isWolfDeath = SoundEvents.WOLF_SOUNDS.entries.any {
            it.value.deathSound().value().location == packet.sound.value().location
        }
        if (!isWolfDeath) return

        lastCastTime = now

        showNotification(config.castMessage)

        val strength = mainHand.strength ?: return
        val gained = (strength * 1.5).toInt()

        if (config.showStrengthGained) {
            modMessage(Component.literal("§6Gained strength: §c$gained"))
            if (config.announceStrengthInParty) {
                sendPartyChat("Gained strength from Ragnarock: $gained")
            }
        }
    }

    private fun handleChatMessage(text: String) {
        if (!config.enabled || !config.cancelNotification) return
        if (cancelRegex.containsMatchIn(text)) {
            showNotification(config.cancelMessage)
        }
    }

    private fun showNotification(text: String) {
        TitleUtil.showSubtitle(text)
        if (config.playSound) {
            val mc = Minecraft.getInstance()
            mc.soundManager.play(
                SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f)
            )
        }
    }
}
