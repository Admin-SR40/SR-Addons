package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.StringArgumentType
import com.sraddons.feature.carry.CarryCommand.suggestClients
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

internal object CarryCommandInfo {

    fun calcPriceNode() = ClientCommands.literal("calc-price")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            calcPriceWithDefault(context.source)
            1
        }
        .then(
            ClientCommands.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val client = context.source.lookupClient(playerName) ?: return@executes 1
                    calcPriceForClient(context.source, client)
                    1
                }
        )

    fun listClientNode() = ClientCommands.literal("list-client")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            if (CarryState.clients.isEmpty()) {
                context.source.feedback(Component.translatable("sraddons.carry.client_zero").withColor(0xFFFF55))
            } else {
                context.source.feedback(Component.translatable("sraddons.carry.client_count",
                    Component.literal(CarryState.clients.size.toString()).withColor(0xFFFFFF)))
                CarryState.clients.values.forEach { client ->
                    context.source.sendFeedback(
                        Component.translatable("sraddons.carry.client_list_entry",
                            Component.literal(client.playerName).withColor(0xFF55FF),
                            Component.literal(client.typeName).withColor(0x55FFFF),
                            Component.literal(client.amount.toString()).withColor(0xFFAA00),
                            Component.literal(client.completed.toString()).withColor(0x55FF55)
                        ).withColor(0xAAAAAA))
                }
            }
            1
        }

    fun listTypeNode() = ClientCommands.literal("list-type")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            if (CarryState.types.isEmpty()) {
                context.source.feedback(Component.translatable("sraddons.carry.types_empty").withColor(0xFFFF55))
            } else {
                context.source.feedback(Component.translatable("sraddons.carry.types_available").withColor(0xFFFFFF))
                CarryState.types.values.forEach { type ->
                    val priceStr = Component.literal(CarryPriceUtil.formatPrice(type.price)).withColor(0xFFAA00)
                    val bulkStr = type.bulkPrice?.let {
                        Component.literal(" / ${CarryPriceUtil.formatPrice(it)} (${type.bulkThreshold}+)").withColor(0xAAAAAA)
                    } ?: Component.literal("")
                    context.source.sendFeedback(
                        Component.translatable("sraddons.carry.type_list_entry",
                            Component.literal(type.name).withColor(0x55FFFF), priceStr, bulkStr))
                }
            }
            1
        }

    fun statusNode() = ClientCommands.literal("status")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            val s = CarryState.status
            if (s.totalOrders == 0 && s.totalCarries == 0 && s.totalEarned == 0L) {
                context.source.feedback(Component.translatable("sraddons.carry.no_orders").withColor(0xFFFF55))
            } else {
                context.source.feedback(Component.translatable("sraddons.carry.orders_summary",
                    Component.literal(s.totalOrders.toString()).withColor(0xFFAA00),
                    Component.literal(s.totalCarries.toString()).withColor(0x55FFFF)).withColor(0xFFFFFF))
                context.source.feedback(Component.translatable("sraddons.carry.earnings_summary",
                    Component.literal(CarryPriceUtil.formatPrice(s.totalEarned)).withColor(0xFFAA00)).withColor(0xFFFFFF))
            }
            1
        }

    private fun calcPriceWithDefault(source: FabricClientCommandSource) {
        val clientCount = CarryState.clients.size
        if (clientCount == 0) {
            source.feedback(Component.translatable("sraddons.carry.client_zero").withColor(0xFF5555))
            return
        }
        if (clientCount > 1) {
            source.feedback(Component.translatable("sraddons.carry.specify_player",
                Component.literal(clientCount.toString()).withColor(0xFFFFFF)).withColor(0xFF5555))
            return
        }
        calcPriceForClient(source, CarryState.clients.values.first())
    }

    private fun calcPriceForClient(source: FabricClientCommandSource, client: CarryClient) {
        val type = source.lookupType(client.typeName) ?: return
        val unitPrice = CarryPriceUtil.effectivePrice(type, client)
        val total = unitPrice * client.amount
        val usingBulk = client.useBulk && type.bulkPrice != null && client.amount >= type.bulkThreshold
        if (usingBulk) {
            source.feedback(Component.translatable("sraddons.carry.calc_price.bulk",
                Component.literal(client.amount.toString()).withColor(0x55FFFF),
                Component.literal(CarryPriceUtil.formatPrice(unitPrice)).withColor(0xFFAA00),
                Component.literal(type.bulkThreshold.toString()).withColor(0xFFFFFF),
                Component.literal(CarryPriceUtil.formatPrice(total)).withColor(0xFFFFFF)))
        } else {
            source.feedback(Component.translatable("sraddons.carry.calc_price.standard",
                Component.literal(client.amount.toString()).withColor(0x55FFFF),
                Component.literal(CarryPriceUtil.formatPrice(unitPrice)).withColor(0xFFAA00),
                Component.literal(CarryPriceUtil.formatPrice(total)).withColor(0xFFFFFF)))
        }
    }
}
