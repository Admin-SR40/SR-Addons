package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.sraddons.feature.carry.CarryCommand.suggestClients
import com.sraddons.feature.carry.CarryCommand.suggestTypes
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.minecraft.network.chat.Component

internal object CarryCommandManagement {

    fun addAmountNode() = ClientCommands.literal("add-amount")
        .then(
            ClientCommands.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .then(
                    ClientCommands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!context.source.requireEnabled()) return@executes 1
                            CarryState.saveUndo()
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = context.source.lookupClient(playerName) ?: return@executes 1
                            client.amount += amount
                            CarryState.saveData()
                            context.source.feedback(Component.translatable("sraddons.carry.amount_added",
                                Component.literal(amount.toString()).withColor(0xFFAA00),
                                Component.literal(client.playerName).withColor(0xFF55FF).withColor(0x55FF55)))
                            context.source.feedback(Component.translatable("sraddons.carry.amount_total",
                                Component.literal(client.amount.toString()).withColor(0xFFFFFF)).withColor(0xAAAAAA))
                            1
                        }
                )
        )

    fun removeAmountNode() = ClientCommands.literal("remove-amount")
        .then(
            ClientCommands.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .then(
                    ClientCommands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!context.source.requireEnabled()) return@executes 1
                            CarryState.saveUndo()
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = context.source.lookupClient(playerName) ?: return@executes 1
                            val actualRemove = amount.coerceAtMost(client.amount)
                            client.amount -= actualRemove
                            client.completed = client.completed.coerceAtMost(client.amount)
                            CarryState.saveData()
                            context.source.feedback(Component.translatable("sraddons.carry.amount_removed",
                                Component.literal(actualRemove.toString()).withColor(0xFFAA00),
                                Component.literal(client.playerName).withColor(0xFF55FF).withColor(0x55FF55)))
                            context.source.feedback(Component.translatable("sraddons.carry.amount_total",
                                Component.literal(client.amount.toString()).withColor(0xFFFFFF)).withColor(0xAAAAAA))
                            1
                        }
                )
        )

    fun setAmountNode() = ClientCommands.literal("set-amount")
        .then(
            ClientCommands.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .then(
                    ClientCommands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!context.source.requireEnabled()) return@executes 1
                            CarryState.saveUndo()
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = context.source.lookupClient(playerName) ?: return@executes 1
                            client.amount = amount
                            client.useBulk = false
                            if (client.completed > client.amount) client.completed = client.amount
                            CarryState.saveData()
                            context.source.feedback(Component.translatable("sraddons.carry.amount_set",
                                Component.literal(client.playerName).withColor(0xFF55FF),
                                Component.literal(amount.toString()).withColor(0xFFAA00),
                                Component.translatable("sraddons.carry.bool.false").withColor(0xAAAAAA).withColor(0x55FF55)))
                            1
                        }
                        .then(
                            ClientCommands.argument("useBulk", BoolArgumentType.bool())
                                .executes { context ->
                                    if (!context.source.requireEnabled()) return@executes 1
                                    CarryState.saveUndo()
                                    val playerName = StringArgumentType.getString(context, "playerName")
                                    val amount = IntegerArgumentType.getInteger(context, "amount")
                                    val useBulk = BoolArgumentType.getBool(context, "useBulk")
                                    val client = context.source.lookupClient(playerName) ?: return@executes 1
                                    client.amount = amount
                                    client.useBulk = useBulk
                                    if (client.completed > client.amount) client.completed = client.amount
                                    CarryState.saveData()
                                    val bulkKey = if (useBulk) "sraddons.carry.bool.true" else "sraddons.carry.bool.false"
                                    context.source.feedback(Component.translatable("sraddons.carry.amount_set",
                                        Component.literal(client.playerName).withColor(0xFF55FF),
                                        Component.literal(amount.toString()).withColor(0xFFAA00),
                                        Component.translatable(bulkKey).withColor(0xAAAAAA).withColor(0x55FF55)))
                                    1
                                }
                        )
                )
        )

    fun removeClientNode() = ClientCommands.literal("remove-client")
        .then(
            ClientCommands.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    CarryState.saveUndo()
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val removed = CarryState.clients.remove(playerName.lowercase())
                    CarryState.saveData()
                    if (removed == null) {
                        context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                            Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    context.source.feedback(Component.translatable("sraddons.carry.client_removed",
                        Component.literal(removed.playerName).withColor(0xFF55FF)).withColor(0x55FF55))
                    1
                }
        )

    fun removeTypeNode() = ClientCommands.literal("remove-type")
        .then(
            ClientCommands.argument("typeName", StringArgumentType.word())
                .suggests(suggestTypes)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    CarryState.saveUndo()
                    val typeName = StringArgumentType.getString(context, "typeName")
                    val key = typeName.lowercase()
                    val type = CarryState.types[key]
                    if (type == null) {
                        context.source.feedback(Component.translatable("sraddons.carry.type_not_found",
                            Component.literal(typeName).withColor(0x55FFFF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    val referencingClients = CarryState.clients.values.filter {
                        it.typeName.equals(typeName, ignoreCase = true)
                    }
                    if (referencingClients.isNotEmpty()) {
                        context.source.feedback(Component.translatable("sraddons.carry.type_in_use",
                            Component.literal(typeName).withColor(0x55FFFF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    CarryState.types.remove(key)
                    CarryState.saveData()
                    context.source.feedback(Component.translatable("sraddons.carry.type_removed",
                        Component.literal(typeName).withColor(0x55FFFF)).withColor(0x55FF55))
                    1
                }
        )
}
