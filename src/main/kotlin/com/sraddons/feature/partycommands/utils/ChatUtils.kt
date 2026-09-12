package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import com.sraddons.util.Constants
import com.sraddons.util.Scheduler
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

val mc: Minecraft
    get() = Minecraft.getInstance()

fun modMessage(message: Component) {
    mc.execute {
        val prefix = Constants.makePrefix()
        mc.gui.hud.chat.addClientSystemMessage(prefix.copy().append(message))
    }
}

fun rawMessage(message: Component) {
    mc.execute {
        mc.gui.hud.chat.addClientSystemMessage(message)
    }
}

/**
 * Spacing between two messages sent by the mod.
 *
 * A command such as `/party warp` used to be followed by its party-chat feedback
 * in the very same tick. Hypixel only processes one command per tick, so one of
 * the two silently failed. Everything the mod sends now goes through
 * [queueSend], which keeps at least this much distance between two sends.
 */
private val sendQueueLock = Any()
private var nextSendAt = 0L

private fun queueSend(action: () -> Unit) {
    val delayMs: Long
    synchronized(sendQueueLock) {
        val now = System.currentTimeMillis()
        val interval = SRConfig.settings.partyCommands.chatSendIntervalMs.coerceIn(0, 2000).toLong()
        val sendAt = maxOf(now, nextSendAt)
        nextSendAt = sendAt + interval
        delayMs = sendAt - now
    }
    if (delayMs <= 0L) {
        mc.execute(action)
    } else {
        Scheduler.schedule(delayMs) { mc.execute(action) }
    }
}

fun sendPartyChat(message: String) {
    val clean = message.toPlainChatMessage()
    if (clean.isEmpty()) return
    queueSend { mc.player?.connection?.sendCommand("pc $clean") }
}

fun sendChatMessage(message: String) {
    val clean = message.toPlainChatMessage()
    if (clean.isEmpty()) return
    queueSend { mc.player?.connection?.sendChat(clean) }
}

fun sendCommand(command: String) {
    queueSend { mc.player?.connection?.sendCommand(command) }
}

fun getPositionString(): String {
    val player = mc.player
        ?: return Component.translatable("sraddons.pc.position.unknown").string
    val pos = player.blockPosition()
    return "x: ${pos.x}, y: ${pos.y}, z: ${pos.z}"
}

fun Double.toFixed(decimals: Int = 1): String = String.format("%.${decimals}f", this)

fun respond(component: Component) {
    if (SRConfig.settings.partyCommands.respondInPartyChat && PartyUtils.isInParty) {
        sendPartyChat("CMD >> " + component.string)
    }
    if (SRConfig.settings.partyCommands.showResponseLocally) {
        modMessage(Component.literal("§f").append(component))
    }
}

fun respondDisabled(command: String) {
    val hasResponsePath = SRConfig.settings.partyCommands.showResponseLocally || SRConfig.settings.partyCommands.respondInPartyChat
    if (hasResponsePath) {
        respond(formatResponse(
            Component.translatable("sraddons.pc.label.error"),
            Component.translatable("sraddons.pc.error.disabled", Component.literal("!$command")).withColor(0xFF5555)
        ))
    } else {
        modMessage(formatResponse(
            Component.translatable("sraddons.pc.label.error"),
            Component.translatable("sraddons.pc.error.disabled", Component.literal("!$command")).withColor(0xFF5555)
        ))
    }
}

fun label(key: String) = Component.translatable("sraddons.pc.label.$key")
