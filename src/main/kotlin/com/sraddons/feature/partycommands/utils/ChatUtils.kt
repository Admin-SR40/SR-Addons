package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import com.sraddons.util.Constants
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

val mc: Minecraft
    get() = Minecraft.getInstance()

fun modMessage(message: String) {
    mc.execute {
        val prefix = Constants.makePrefix()
        mc.gui?.chat?.addMessage(prefix.copy().append(Component.literal(message)))
    }
}

fun rawMessage(message: String) {
    mc.execute {
        mc.gui?.chat?.addMessage(Component.literal(message))
    }
}

fun sendPartyChat(message: String) {
    mc.execute {
        mc.player?.connection?.sendCommand("pc $message")
    }
}

fun sendChatMessage(message: String) {
    mc.execute {
        mc.player?.connection?.sendChat(message)
    }
}

fun sendCommand(command: String) {
    mc.execute {
        mc.player?.connection?.sendCommand(command)
    }
}

fun getPositionString(): String {
    val player = mc.player ?: return "\u672a\u77e5\u4f4d\u7f6e"
    return "x: ${player.blockPosition().x}, y: ${player.blockPosition().y}, z: ${player.blockPosition().z}"
}

fun Double.toFixed(decimals: Int = 1): String = String.format("%.${decimals}f", this)

val String.noControlCodes: String
    get() = this.replace(Regex("\u00a7[0-9a-fk-or]"), "")

fun respond(message: String) {
    if (SRConfig.settings.partyCommands.respondInPartyChat && PartyUtils.isInParty) {
        sendPartyChat("CMD >> " + message.noControlCodes)
    }
    if (SRConfig.settings.partyCommands.showResponseLocally) {
        modMessage("\u00a7f$message")
    }
}

fun respondDisabled(command: String) {
    respond(formatResponse("Error", "\u00a7c!$command is disabled in config.", ""))
}
