package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.StringArgumentType
import com.sraddons.feature.carry.CarryCommand.suggestMinibossNames
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.minecraft.network.chat.Component

internal object CarryCommandMiniboss {

    private fun getQuotedName(context: com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>): String? {
        val argNode = context.nodes.find { it.node.name == "name" } ?: return null
        val rawArg = context.input.substring(argNode.range.start, argNode.range.end)
        if (!rawArg.startsWith("\"") || !rawArg.endsWith("\"")) {
            context.source.feedback(Component.translatable("sraddons.carry.quotes_required").withColor(0xFF5555))
            return null
        }
        return StringArgumentType.getString(context, "name")
    }

    fun addMinibossNode() = ClientCommandManager.literal("add-miniboss")
        .then(
            ClientCommandManager.argument("name", StringArgumentType.string())
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    val name = getQuotedName(context) ?: return@executes 1
                    if (CarryState.minibossNames.any { it.equals(name, ignoreCase = true) }) {
                        context.source.feedback(Component.translatable("sraddons.carry.miniboss_already_exists",
                            Component.literal(name).withColor(0x55FFFF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    CarryState.minibossNames.add(name)
                    CarryState.saveData()
                    context.source.feedback(Component.translatable("sraddons.carry.miniboss_added",
                        Component.literal(name).withColor(0x55FFFF)).withColor(0x55FF55))
                    1
                }
        )

    fun removeMinibossNode() = ClientCommandManager.literal("remove-miniboss")
        .then(
            ClientCommandManager.argument("name", StringArgumentType.string())
                .suggests(suggestMinibossNames)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    val name = getQuotedName(context) ?: return@executes 1
                    if (CarryState.minibossNames.none { it.equals(name, ignoreCase = true) }) {
                        context.source.feedback(Component.translatable("sraddons.carry.miniboss_not_found",
                            Component.literal(name).withColor(0x55FFFF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    CarryState.minibossNames.removeAll { it.equals(name, ignoreCase = true) }
                    CarryState.saveData()
                    context.source.feedback(Component.translatable("sraddons.carry.miniboss_removed",
                        Component.literal(name).withColor(0x55FFFF)).withColor(0x55FF55))
                    1
                }
        )
}
