package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.sraddons.feature.carry.CarryCommand.suggestClients
import com.sraddons.feature.carry.CarryCommand.suggestTypes
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

internal object CarryCommandSetup {

    fun addTypeNode() = ClientCommandManager.literal("add-type")
        .then(
            ClientCommandManager.argument("typeName", StringArgumentType.word())
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    CarryState.saveUndo()
                    val typeName = StringArgumentType.getString(context, "typeName")
                    val key = typeName.lowercase()
                    if (CarryState.types.containsKey(key)) {
                        context.source.feedback(Component.translatable("sraddons.carry.type_already_exists",
                            Component.literal(typeName).withColor(0x55FFFF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    CarryState.types[key] = CarryType(name = typeName)
                    CarryState.saveData()
                    context.source.feedback(Component.translatable("sraddons.carry.type_added",
                        Component.literal(typeName).withColor(0x55FFFF)).withColor(0x55FF55))
                    1
                }
        )

    fun addClientNode() = ClientCommandManager.literal("add-client")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .then(
                    ClientCommandManager.argument("typeName", StringArgumentType.word())
                        .suggests(suggestTypes)
                        .then(
                            ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    if (!context.source.requireEnabled()) return@executes 1
                                    CarryState.saveUndo()
                                    val playerName = StringArgumentType.getString(context, "playerName")
                                    val typeName = StringArgumentType.getString(context, "typeName")
                                    val amount = IntegerArgumentType.getInteger(context, "amount")
                                    val typeKey = typeName.lowercase()
                                    val clientKey = playerName.lowercase()

                                    if (!CarryState.types.containsKey(typeKey)) {
                                        context.source.feedback(Component.translatable("sraddons.carry.type_not_found",
                                            Component.literal(typeName).withColor(0x55FFFF)).withColor(0xFF5555))
                                        return@executes 1
                                    }
                                    if (CarryState.clients.containsKey(clientKey)) {
                                        context.source.feedback(Component.translatable("sraddons.carry.client_already_exists",
                                            Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                                        return@executes 1
                                    }
                                    CarryState.clients[clientKey] = CarryClient(
                                        playerName = playerName, typeName = typeName, amount = amount
                                    )
                                    CarryState.saveData()
                                    context.source.feedback(Component.translatable("sraddons.carry.client_added",
                                        Component.literal(playerName).withColor(0xFF55FF),
                                        Component.literal(amount.toString()).withColor(0xFFAA00),
                                        Component.literal(typeName).withColor(0x55FFFF).withColor(0x55FF55)))
                                    1
                                }
                        )
                )
        )

    fun setPriceNode() = ClientCommandManager.literal("set-price")
        .then(
            ClientCommandManager.argument("typeName", StringArgumentType.word())
                .suggests(suggestTypes)
                .then(
                    ClientCommandManager.argument("price", StringArgumentType.string())
                        .executes { context ->
                            if (!context.source.requireEnabled()) return@executes 1
                            CarryState.saveUndo()
                            val typeName = StringArgumentType.getString(context, "typeName")
                            val priceStr = StringArgumentType.getString(context, "price")
                            val type = context.source.lookupType(typeName) ?: return@executes 1
                            val parsed = CarryPriceUtil.parsePrice(priceStr)
                            if (parsed == null) {
                                context.source.feedback(Component.translatable("sraddons.carry.price_invalid").withColor(0xFF5555))
                                return@executes 1
                            }
                            type.price = parsed
                            CarryState.saveData()
                            context.source.feedback(Component.translatable("sraddons.carry.price_set",
                                Component.literal(type.name).withColor(0x55FFFF),
                                Component.literal(CarryPriceUtil.formatPrice(parsed)).withColor(0xFFAA00).withColor(0x55FF55)))
                            1
                        }
                )
        )

    fun setBulkPriceNode() = ClientCommandManager.literal("set-bulk-price")
        .then(
            ClientCommandManager.argument("typeName", StringArgumentType.word())
                .suggests(suggestTypes)
                .then(
                    ClientCommandManager.argument("bulkPrice", StringArgumentType.string())
                        .then(
                            ClientCommandManager.argument("threshold", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    if (!context.source.requireEnabled()) return@executes 1
                                    CarryState.saveUndo()
                                    val typeName = StringArgumentType.getString(context, "typeName")
                                    val bulkPriceStr = StringArgumentType.getString(context, "bulkPrice")
                                    val threshold = IntegerArgumentType.getInteger(context, "threshold")
                                    val type = context.source.lookupType(typeName) ?: return@executes 1
                                    val parsed = CarryPriceUtil.parsePrice(bulkPriceStr)
                                    if (parsed == null) {
                                        context.source.feedback(Component.translatable("sraddons.carry.price_invalid").withColor(0xFF5555))
                                        return@executes 1
                                    }
                                    type.bulkPrice = parsed
                                    type.bulkThreshold = threshold
                                    CarryState.saveData()
                                    context.source.feedback(Component.translatable("sraddons.carry.bulk_price_set",
                                        Component.literal(type.name).withColor(0x55FFFF),
                                        Component.literal(CarryPriceUtil.formatPrice(parsed)).withColor(0xFFAA00),
                                        Component.literal(threshold.toString()).withColor(0x55FFFF).withColor(0x55FF55)))
                                    1
                                }
                        )
                )
        )
}
