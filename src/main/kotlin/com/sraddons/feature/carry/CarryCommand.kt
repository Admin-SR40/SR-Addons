package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object CarryCommand {

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val root = ClientCommands.literal("cm")

            root.executes { context -> showHelp(context.source); 1 }

            root.then(CarryCommandSetup.addTypeNode())
                .then(CarryCommandSetup.addClientNode())
                .then(CarryCommandSetup.setPriceNode())
                .then(CarryCommandSetup.setBulkPriceNode())
                .then(CarryCommandManagement.setAmountNode())
                .then(CarryCommandManagement.addAmountNode())
                .then(CarryCommandManagement.removeAmountNode())
                .then(CarryCommandInfo.calcPriceNode())
                .then(CarryCommandManagement.removeClientNode())
                .then(CarryCommandManagement.removeTypeNode())
                .then(CarryCommandInfo.listClientNode())
                .then(CarryCommandInfo.listTypeNode())
                .then(CarryCommandActions.doneNode())
                .then(CarryCommandActions.refundNode())
                .then(CarryCommandInfo.statusNode())
                .then(CarryCommandActions.clearClientNode())
                .then(CarryCommandActions.clearHistoryNode())
                .then(CarryCommandActions.undoNode())
                .then(CarryCommandMiniboss.addMinibossNode())
                .then(CarryCommandMiniboss.removeMinibossNode())
                .also { dispatcher.register(it) }
        }
    }

    val suggestTypes = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryState.types.values.forEach { builder.suggest(it.name) }
        builder.buildFuture()
    }
    val suggestClients = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryState.clients.values.forEach { builder.suggest(it.playerName) }
        builder.buildFuture()
    }
    val suggestMinibossNames = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryState.minibossNames.forEach { builder.suggest("\"$it\"") }
        builder.buildFuture()
    }

    private fun helpLine(
        source: FabricClientCommandSource,
        cmd: String,
        descKey: String,
        arg: String? = null,
        argColor: Int = 0x55FFFF,
        extra: String? = null,
        extraColor: Int = 0xFFAA00
    ) {
        var line = Component.empty()
            .append(Component.literal(cmd).withColor(0xAAAAAA))
        if (arg != null) line = line.append(Component.literal(" $arg").withColor(argColor))
        if (extra != null) line = line.append(Component.literal(" $extra").withColor(extraColor))
        line = line.append(Component.literal(" - ").withColor(0x555555))
            .append(Component.translatable(descKey).withColor(0xFFFFFF))
        source.sendFeedback(line)
    }

    private fun showHelp(source: FabricClientCommandSource) {
        source.feedback(Component.translatable("sraddons.carry.help.title").withColor(0xFFFFFF))
        helpLine(source, "/cm add-type", "sraddons.carry.help.add_type_desc", arg = "<type>")
        helpLine(source, "/cm add-client", "sraddons.carry.help.add_desc", arg = "<player> <type> <amount>", argColor = 0xFF55FF)
        helpLine(source, "/cm set-price", "sraddons.carry.help.set_price_desc", arg = "<type>", extra = "<price>")
        helpLine(source, "/cm set-bulk-price", "sraddons.carry.help.set_bulk_price_desc", arg = "<type>", extra = "<price> <threshold>")
        helpLine(source, "/cm add-amount", "sraddons.carry.help.add_amount_desc", arg = "<player>", extra = "<amount>", extraColor = 0xFFFFFF)
        helpLine(source, "/cm set-amount", "sraddons.carry.help.set_amount_desc", arg = "<player>", extra = "<amount> [true|false]", extraColor = 0xFFFFFF)
        helpLine(source, "/cm remove-amount", "sraddons.carry.help.remove_amount_desc", arg = "<player>", extra = "<amount>", extraColor = 0xFFFFFF)
        helpLine(source, "/cm calc-price", "sraddons.carry.help.calc_price_desc", arg = "[player]", argColor = 0xFF55FF)
        helpLine(source, "/cm undo", "sraddons.carry.help.undo_desc")
        helpLine(source, "/cm remove-client", "sraddons.carry.help.remove_desc", arg = "<player>", argColor = 0xFF55FF)
        helpLine(source, "/cm remove-type", "sraddons.carry.help.remove_type_desc", arg = "<type>")
        helpLine(source, "/cm list-client", "sraddons.carry.help.list_client_desc")
        helpLine(source, "/cm list-type", "sraddons.carry.help.list_type_desc")
        helpLine(source, "/cm done", "sraddons.carry.help.done_desc", arg = "[player] [amount]", argColor = 0xFF55FF)
        helpLine(source, "/cm refund", "sraddons.carry.help.refund_desc", arg = "<player>", argColor = 0xFF55FF)
        helpLine(source, "/cm status", "sraddons.carry.help.status_desc")
        helpLine(source, "/cm clear-client", "sraddons.carry.help.clear_client_desc")
        helpLine(source, "/cm clear-history", "sraddons.carry.help.clear_history_desc")
        helpLine(source, "/cm add-miniboss", "sraddons.carry.help.add_miniboss_desc", arg = "\"<NAME>\"")
        helpLine(source, "/cm remove-miniboss", "sraddons.carry.help.remove_miniboss_desc", arg = "\"<NAME>\"")
    }
}
