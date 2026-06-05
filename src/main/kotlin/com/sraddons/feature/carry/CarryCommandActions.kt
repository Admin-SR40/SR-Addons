package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.sraddons.feature.carry.CarryCommand.suggestClients
import com.sraddons.feature.partycommands.utils.sendPartyChat
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

internal object CarryCommandActions {

    fun doneNode() = ClientCommandManager.literal("done")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            doneWithDefaultAmount(context.source, 1)
            1
        }
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val client = context.source.lookupClient(playerName) ?: return@executes 1
                    executeDone(context.source, 1, client)
                    1
                }
                .then(
                    ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!context.source.requireEnabled()) return@executes 1
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = context.source.lookupClient(playerName) ?: return@executes 1
                            executeDone(context.source, amount, client)
                            1
                        }
                )
        )

    fun refundNode() = ClientCommandManager.literal("refund")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    CarryState.saveUndo()
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val client = context.source.lookupClient(playerName) ?: return@executes 1
                    val type = context.source.lookupType(client.typeName) ?: return@executes 1
                    val remaining = client.amount - client.completed
                    val unitPrice = CarryPriceUtil.effectivePrice(type, client)
                    val refundAmount = remaining * unitPrice

                    context.source.feedback(Component.translatable("sraddons.carry.refund_info",
                        Component.literal(client.playerName).withColor(0xFF55FF),
                        Component.literal(client.amount.toString()).withColor(0xFFAA00),
                        Component.literal(client.completed.toString()).withColor(0x55FF55).withColor(0xFFFFFF)))

                    if (remaining > 0) {
                        context.source.feedback(Component.translatable("sraddons.carry.refund_amount",
                            Component.literal(remaining.toString()).withColor(0x55FFFF),
                            Component.literal(CarryPriceUtil.formatPrice(unitPrice)).withColor(0xFFAA00),
                            Component.literal(CarryPriceUtil.formatPrice(refundAmount)).withColor(0xFFAA00).withColor(0xFFFFFF)))
                    } else {
                        context.source.feedback(Component.translatable("sraddons.carry.refund_none").withColor(0xFFFF55))
                    }

                    if (client.completed > 0) {
                        CarryState.status.totalOrders++
                        CarryState.status.totalCarries += client.completed
                        CarryState.status.totalEarned += unitPrice * client.completed
                        CarryState.saveHistory()
                    }

                    CarryState.clients.remove(playerName.lowercase())
                    CarryState.saveData()
                    context.source.feedback(Component.translatable("sraddons.carry.finished").withColor(0x55FF55))
                    context.source.feedback(Component.translatable("sraddons.carry.auto_removed",
                        Component.literal(client.playerName).withColor(0xFF55FF)).withColor(0x55FF55))
                    1
                }
        )

    fun undoNode() = ClientCommandManager.literal("undo")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            if (CarryState.undo()) {
                context.source.feedback(Component.translatable("sraddons.carry.undo.success").withColor(0x55FF55))
            } else {
                context.source.feedback(Component.translatable("sraddons.carry.undo.nothing").withColor(0xFFFF55))
            }
            1
        }

    fun clearClientNode() = ClientCommandManager.literal("clear-client")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            CarryState.saveUndo()
            CarryState.clients.clear()
            CarryState.saveData()
            context.source.feedback(Component.translatable("sraddons.carry.all_cleared").withColor(0xFFFF55))
            1
        }

    fun clearHistoryNode() = ClientCommandManager.literal("clear-history")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            CarryState.saveUndo()
            CarryState.status = CarryStatus()
            CarryState.saveHistory()
            context.source.feedback(Component.translatable("sraddons.carry.history_reset").withColor(0xFFFF55))
            1
        }

    private fun doneWithDefaultAmount(source: FabricClientCommandSource, amount: Int) {
        when (CarryState.clients.size) {
            0 -> source.feedback(Component.translatable("sraddons.carry.client_zero").withColor(0xFF5555))
            1 -> executeDone(source, amount, CarryState.clients.values.first())
            else -> source.feedback(Component.translatable("sraddons.carry.specify_player",
                Component.literal(CarryState.clients.size.toString()).withColor(0xFFFFFF)).withColor(0xFF5555))
        }
    }

    private fun executeDone(source: FabricClientCommandSource, amount: Int, client: CarryClient) {
        CarryState.saveUndo()
        val type = source.lookupType(client.typeName) ?: return
        val remaining = client.amount - client.completed
        if (remaining <= 0) {
            source.feedback(Component.translatable("sraddons.carry.no_remaining",
                Component.literal(client.playerName).withColor(0xFF55FF)).withColor(0xFFFF55))
            return
        }
        val actualDone = amount.coerceAtMost(remaining)
        client.completed += actualDone

        sendPartyChat("${client.completed}/${client.amount}")

        if (client.completed >= client.amount) {
            val unitPrice = CarryPriceUtil.effectivePrice(type, client)
            CarryState.status.totalOrders++
            CarryState.status.totalCarries += client.amount
            CarryState.status.totalEarned += unitPrice * client.amount
            CarryState.saveHistory()
            CarryState.clients.remove(client.playerName.lowercase())
            CarryState.saveData()

            source.feedback(Component.translatable("sraddons.carry.finished").withColor(0x55FF55))
            source.feedback(Component.translatable("sraddons.carry.auto_removed",
                Component.literal(client.playerName).withColor(0xFF55FF)).withColor(0x55FF55))
        } else {
            source.feedback(Component.translatable("sraddons.carry.progress",
                Component.literal(actualDone.toString()).withColor(0x55FF55),
                Component.literal(client.playerName).withColor(0xFF55FF)))
            source.feedback(Component.translatable("sraddons.carry.progress_detail",
                Component.literal(client.completed.toString()).withColor(0x55FF55),
                Component.literal(client.amount.toString()).withColor(0xFFAA00)))
        }
    }
}
