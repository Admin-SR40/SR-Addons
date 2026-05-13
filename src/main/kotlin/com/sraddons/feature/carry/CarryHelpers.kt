package com.sraddons.feature.carry

import com.sraddons.config.SRConfig
import com.sraddons.util.Constants
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

fun FabricClientCommandSource.feedback(component: Component) {
    sendFeedback(Constants.makePrefix().copy().append(component))
}

fun FabricClientCommandSource.requireEnabled(): Boolean {
    if (!SRConfig.settings.carry.enabled) {
        feedback(Component.translatable("sraddons.carry.disabled").withColor(0xFF5555))
        return false
    }
    return true
}

fun FabricClientCommandSource.lookupClient(name: String, saveUndo: Boolean = false): CarryClient? {
    if (saveUndo) CarryState.saveUndo()
    val client = CarryState.clients[name.lowercase()]
    if (client == null) {
        feedback(Component.translatable("sraddons.carry.client_not_found",
            Component.literal(name).withColor(0xFF55FF)).withColor(0xFF5555))
    }
    return client
}

fun FabricClientCommandSource.lookupType(name: String): CarryType? {
    val type = CarryState.types[name.lowercase()]
    if (type == null) {
        feedback(Component.translatable("sraddons.carry.type_not_found",
            Component.literal(name).withColor(0x55FFFF)).withColor(0xFF5555))
    }
    return type
}

fun FabricClientCommandSource.requireType(name: String): CarryType? {
    CarryState.saveUndo()
    return lookupType(name)
}

inline fun FabricClientCommandSource.withSingleClient(action: (CarryClient) -> Unit) {
    val count = CarryState.clients.size
    when {
        count == 0 -> feedback(Component.translatable("sraddons.carry.client_zero").withColor(0xFF5555))
        count > 1 -> feedback(Component.translatable("sraddons.carry.specify_player",
            Component.literal(count.toString()).withColor(0xFFFFFF)).withColor(0xFF5555))
        else -> action(CarryState.clients.values.first())
    }
}
