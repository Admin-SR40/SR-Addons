package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object CarryCommand {

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val root = ClientCommandManager.literal("cm")

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

    private fun buildHelpLine(syntax: String, descKey: String): Component =
        Component.literal(syntax).append(Component.translatable(descKey))

    private fun showHelp(source: FabricClientCommandSource) {
        source.feedback(Component.translatable("sraddons.carry.help.title").withColor(0xFFFFFF))
        source.sendFeedback(buildHelpLine("§7/cm add-type §b<type> §8- §f", "sraddons.carry.help.add_type_desc"))
        source.sendFeedback(buildHelpLine("§7/cm add §d<player> §b<type> §f<amount> §8- §f", "sraddons.carry.help.add_desc"))
        source.sendFeedback(buildHelpLine("§7/cm set-price §b<type> §6<price> §8- §f", "sraddons.carry.help.set_price_desc"))
        source.sendFeedback(buildHelpLine("§7/cm set-bulk-price §b<type> §6<price> §f<threshold> §8- §f", "sraddons.carry.help.set_bulk_price_desc"))
        source.sendFeedback(buildHelpLine("§7/cm add-amount §d<player> §f<amount> §8- §f", "sraddons.carry.help.add_amount_desc"))
        source.sendFeedback(buildHelpLine("§7/cm set-amount §d<player> §f<amount> §7[§ftrue|false§7] §8- §f", "sraddons.carry.help.set_amount_desc"))
        source.sendFeedback(buildHelpLine("§7/cm remove-amount §d<player> §f<amount> §8- §f", "sraddons.carry.help.remove_amount_desc"))
        source.sendFeedback(buildHelpLine("§7/cm calc-price §d<player> §8- §f", "sraddons.carry.help.calc_price_desc"))
        source.sendFeedback(buildHelpLine("§7/cm undo §8- §f", "sraddons.carry.help.undo_desc"))
        source.sendFeedback(buildHelpLine("§7/cm remove §d<player> §8- §f", "sraddons.carry.help.remove_desc"))
        source.sendFeedback(buildHelpLine("§7/cm remove-type §b<type> §8- §f", "sraddons.carry.help.remove_type_desc"))
        source.sendFeedback(buildHelpLine("§7/cm list-client §8- §f", "sraddons.carry.help.list_client_desc"))
        source.sendFeedback(buildHelpLine("§7/cm list-type §8- §f", "sraddons.carry.help.list_type_desc"))
        source.sendFeedback(buildHelpLine("§7/cm done §7[§dplayer§7] §7[§famount§7] §8- §f", "sraddons.carry.help.done_desc"))
        source.sendFeedback(buildHelpLine("§7/cm refund §d<player> §8- §f", "sraddons.carry.help.refund_desc"))
        source.sendFeedback(buildHelpLine("§7/cm status §8- §f", "sraddons.carry.help.status_desc"))
        source.sendFeedback(buildHelpLine("§7/cm clear-client §8- §f", "sraddons.carry.help.clear_client_desc"))
        source.sendFeedback(buildHelpLine("§7/cm clear-history §8- §f", "sraddons.carry.help.clear_history_desc"))
        source.sendFeedback(buildHelpLine("§7/cm add-miniboss §b\"<NAME>\" §8- §f", "sraddons.carry.help.add_miniboss_desc"))
        source.sendFeedback(buildHelpLine("§7/cm remove-miniboss §b\"<NAME>\" §8- §f", "sraddons.carry.help.remove_miniboss_desc"))
    }
}
