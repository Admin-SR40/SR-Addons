package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.sendPartyChat
import com.sraddons.util.Constants
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object CarryCommand {

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val root = ClientCommandManager.literal("cm")

            root.executes { context ->
                showHelp(context.source)
                1
            }

            root.then(addTypeNode())
                .then(addNode())
                .then(setPriceNode())
                .then(setBulkPriceNode())
                .then(addAmountNode())
                .then(removeAmountNode())
                .then(calcPriceNode())
                .then(removeNode())
                .then(removeTypeNode())
                .then(listClientNode())
                .then(listTypeNode())
                .then(doneNode())
                .then(refundNode())
                .then(statusNode())
                .then(clearClientNode())
                .then(clearHistoryNode())
                .also { dispatcher.register(it) }
        }
    }

    private fun enabledCheck(source: FabricClientCommandSource): Boolean {
        if (!SRConfig.settings.carry.enabled) {
            source.sendFeedback(
                Constants.makePrefix().copy()
                    .append(Component.literal("Carry module is disabled in config.").withColor(0xFF5555))
            )
            return false
        }
        return true
    }

    private val suggestTypes = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryState.types.values.forEach { builder.suggest(it.name) }
        builder.buildFuture()
    }

    private val suggestClients = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryState.clients.values.forEach { builder.suggest(it.playerName) }
        builder.buildFuture()
    }

    private fun showHelp(source: FabricClientCommandSource) {
        source.sendFeedback(
            Constants.makePrefix().copy()
                .append(Component.literal("SR-Addons Carry Module").withColor(0xFFFFFF))
        )
        source.sendFeedback(Component.literal("§7/cm add-type §b<type> §8- §fAdd a carry type"))
        source.sendFeedback(Component.literal("§7/cm add §d<player> §b<type> §f<amount> §8- §fAdd a client"))
        source.sendFeedback(Component.literal("§7/cm set-price §b<type> §6<price> §8- §fSet price (e.g. 1.8M, 500K)"))
        source.sendFeedback(Component.literal("§7/cm set-bulk-price §b<type> §6<price> §f<threshold> §8- §fSet bulk price"))
        source.sendFeedback(Component.literal("§7/cm add-amount §d<player> §f<amount> §8- §fIncrease carry count"))
        source.sendFeedback(Component.literal("§7/cm remove-amount §d<player> §f<amount> §8- §fDecrease carry count"))
        source.sendFeedback(Component.literal("§7/cm calc-price §d<player> §8- §fCalculate total price"))
        source.sendFeedback(Component.literal("§7/cm remove §d<player> §8- §fRemove a client"))
        source.sendFeedback(Component.literal("§7/cm remove-type §b<type> §8- §fRemove a carry type"))
        source.sendFeedback(Component.literal("§7/cm list-client §8- §fShow all clients"))
        source.sendFeedback(Component.literal("§7/cm list-type §8- §fShow all carry types"))
        source.sendFeedback(Component.literal("§7/cm done §f<amount> §d<player> §8- §fRecord completed carries"))
        source.sendFeedback(Component.literal("§7/cm refund §d<player> §8- §fCalculate refund"))
        source.sendFeedback(Component.literal("§7/cm status §8- §fShow total earnings"))
        source.sendFeedback(Component.literal("§7/cm clear-client §8- §fRemove all clients"))
        source.sendFeedback(Component.literal("§7/cm clear-history §8- §fReset earnings history"))
    }

    // ---- /cm add-type <typeName> ----

    private fun addTypeNode() = ClientCommandManager.literal("add-type")
        .then(
            ClientCommandManager.argument("typeName", StringArgumentType.word())
                .executes { context ->
                    if (!enabledCheck(context.source)) return@executes 1
                    val typeName = StringArgumentType.getString(context, "typeName")
                    val key = typeName.lowercase()
                    if (CarryState.types.containsKey(key)) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§cCarry type §b$typeName §calready exists."))
                        )
                        return@executes 1
                    }
                    CarryState.types[key] = CarryType(name = typeName)
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("§aAdded carry type: §b$typeName"))
                    )
                    1
                }
        )

    // ---- /cm add <playerName> <typeName> <amount> ----

    private fun addNode() = ClientCommandManager.literal("add")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .then(
                    ClientCommandManager.argument("typeName", StringArgumentType.word())
                        .suggests(suggestTypes)
                        .then(
                            ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    if (!enabledCheck(context.source)) return@executes 1
                                    val playerName = StringArgumentType.getString(context, "playerName")
                                    val typeName = StringArgumentType.getString(context, "typeName")
                                    val amount = IntegerArgumentType.getInteger(context, "amount")
                                    val typeKey = typeName.lowercase()
                                    val clientKey = playerName.lowercase()

                                    if (!CarryState.types.containsKey(typeKey)) {
                                        context.source.sendFeedback(
                                            Constants.makePrefix().copy()
                                                .append(Component.literal("§cCarry type §b$typeName §cnot found."))
                                        )
                                        return@executes 1
                                    }
                                    if (CarryState.clients.containsKey(clientKey)) {
                                        context.source.sendFeedback(
                                            Constants.makePrefix().copy()
                                                .append(Component.literal("§cPlayer §d$playerName §cis already a client."))
                                        )
                                        return@executes 1
                                    }
                                    CarryState.clients[clientKey] = CarryClient(
                                        playerName = playerName,
                                        typeName = typeName,
                                        amount = amount
                                    )
                                    context.source.sendFeedback(
                                        Constants.makePrefix().copy()
                                            .append(Component.literal("§aAdded §d$playerName §aas client, requested §f$amount §b$typeName"))
                                    )
                                    1
                                }
                        )
                )
        )

    // ---- /cm set-price <typeName> <price> ----

    private fun setPriceNode() = ClientCommandManager.literal("set-price")
        .then(
            ClientCommandManager.argument("typeName", StringArgumentType.word())
                .suggests(suggestTypes)
                .then(
                    ClientCommandManager.argument("price", StringArgumentType.string())
                        .executes { context ->
                            if (!enabledCheck(context.source)) return@executes 1
                            val typeName = StringArgumentType.getString(context, "typeName")
                            val priceStr = StringArgumentType.getString(context, "price")
                            val key = typeName.lowercase()
                            val type = CarryState.types[key]
                            if (type == null) {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§cCarry type §b$typeName §cnot found."))
                                )
                                return@executes 1
                            }
                            val parsed = CarryPriceUtil.parsePrice(priceStr)
                            if (parsed == null) {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§cInvalid price format. Use raw numbers, K, or M (e.g. 1.8M, 500K)."))
                                )
                                return@executes 1
                            }
                            type.price = parsed
                            context.source.sendFeedback(
                                Constants.makePrefix().copy()
                                    .append(Component.literal("§aSet §b${type.name} §aprice to §6${CarryPriceUtil.formatPrice(parsed)} §acoins."))
                            )
                            1
                        }
                )
        )

    // ---- /cm set-bulk-price <typeName> <bulkPrice> <threshold> ----

    private fun setBulkPriceNode() = ClientCommandManager.literal("set-bulk-price")
        .then(
            ClientCommandManager.argument("typeName", StringArgumentType.word())
                .suggests(suggestTypes)
                .then(
                    ClientCommandManager.argument("bulkPrice", StringArgumentType.string())
                        .then(
                            ClientCommandManager.argument("threshold", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    if (!enabledCheck(context.source)) return@executes 1
                                    val typeName = StringArgumentType.getString(context, "typeName")
                                    val bulkPriceStr = StringArgumentType.getString(context, "bulkPrice")
                                    val threshold = IntegerArgumentType.getInteger(context, "threshold")
                                    val key = typeName.lowercase()
                                    val type = CarryState.types[key]
                                    if (type == null) {
                                        context.source.sendFeedback(
                                            Constants.makePrefix().copy()
                                                .append(Component.literal("§cCarry type §b$typeName §cnot found."))
                                        )
                                        return@executes 1
                                    }
                                    val parsed = CarryPriceUtil.parsePrice(bulkPriceStr)
                                    if (parsed == null) {
                                        context.source.sendFeedback(
                                            Constants.makePrefix().copy()
                                                .append(Component.literal("§cInvalid price format. Use raw numbers, K, or M (e.g. 1.8M, 500K)."))
                                        )
                                        return@executes 1
                                    }
                                    type.bulkPrice = parsed
                                    type.bulkThreshold = threshold
                                    context.source.sendFeedback(
                                        Constants.makePrefix().copy()
                                            .append(Component.literal("§aSet §b${type.name} §abulk price to §6${CarryPriceUtil.formatPrice(parsed)} §acoins §7(§f${threshold}+§7)."))
                                    )
                                    1
                                }
                        )
                )
        )

    // ---- /cm add-amount <playerName> <amount> ----

    private fun addAmountNode() = ClientCommandManager.literal("add-amount")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .then(
                    ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!enabledCheck(context.source)) return@executes 1
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = CarryState.clients[playerName.lowercase()]
                            if (client == null) {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§cPlayer §d$playerName §cis not a client."))
                                )
                                return@executes 1
                            }
                            client.amount += amount
                            context.source.sendFeedback(
                                Constants.makePrefix().copy()
                                    .append(Component.literal("§aAdded §f$amount §acarries to §d${client.playerName}"))
                            )
                            context.source.sendFeedback(
                                Constants.makePrefix().copy()
                                    .append(Component.literal("§7There are total §f${client.amount} §7carries now"))
                            )
                            1
                        }
                )
        )

    // ---- /cm remove-amount <playerName> <amount> ----

    private fun removeAmountNode() = ClientCommandManager.literal("remove-amount")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .then(
                    ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!enabledCheck(context.source)) return@executes 1
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = CarryState.clients[playerName.lowercase()]
                            if (client == null) {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§cPlayer §d$playerName §cis not a client."))
                                )
                                return@executes 1
                            }
                            val actualRemove = amount.coerceAtMost(client.amount)
                            client.amount -= actualRemove
                            if (client.completed > client.amount) {
                                client.completed = client.amount
                            }
                            context.source.sendFeedback(
                                Constants.makePrefix().copy()
                                    .append(Component.literal("§aRemoved §f$actualRemove §acarries from §d${client.playerName}"))
                            )
                            context.source.sendFeedback(
                                Constants.makePrefix().copy()
                                    .append(Component.literal("§7There are total §f${client.amount} §7carries now"))
                            )
                            1
                        }
                )
        )

    // ---- /cm calc-price <playerName> ----

    private fun calcPriceNode() = ClientCommandManager.literal("calc-price")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!enabledCheck(context.source)) return@executes 1
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val client = CarryState.clients[playerName.lowercase()]
                    if (client == null) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§cPlayer §d$playerName §cis not a client."))
                        )
                        return@executes 1
                    }
                    val type = CarryState.types[client.typeName.lowercase()]
                    if (type == null) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§cReferenced type §b${client.typeName} §cnot found."))
                        )
                        return@executes 1
                    }
                    val unitPrice = CarryPriceUtil.effectivePrice(type, client.amount)
                    val total = unitPrice * client.amount
                    val usingBulk = type.bulkPrice != null && client.amount >= type.bulkThreshold
                    val msg = if (usingBulk) {
                        "§fIt would be §f${client.amount} §7* §6${CarryPriceUtil.formatPrice(unitPrice)} §7(bulk, §f${type.bulkThreshold}+§7) §7= §6${CarryPriceUtil.formatPrice(total)} §fcoins in total"
                    } else {
                        "§fIt would be §f${client.amount} §7* §6${CarryPriceUtil.formatPrice(unitPrice)} §7= §6${CarryPriceUtil.formatPrice(total)} §fcoins in total"
                    }
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal(msg))
                    )
                    1
                }
        )

    // ---- /cm remove <playerName> ----

    private fun removeNode() = ClientCommandManager.literal("remove")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!enabledCheck(context.source)) return@executes 1
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val removed = CarryState.clients.remove(playerName.lowercase())
                    if (removed == null) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§cPlayer §d$playerName §cis not a client."))
                        )
                        return@executes 1
                    }
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("§aRemoved §d${removed.playerName} §afrom client list."))
                    )
                    1
                }
        )

    // ---- /cm remove-type <typeName> ----

    private fun removeTypeNode() = ClientCommandManager.literal("remove-type")
        .then(
            ClientCommandManager.argument("typeName", StringArgumentType.word())
                .suggests(suggestTypes)
                .executes { context ->
                    if (!enabledCheck(context.source)) return@executes 1
                    val typeName = StringArgumentType.getString(context, "typeName")
                    val key = typeName.lowercase()
                    val type = CarryState.types[key]
                    if (type == null) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§cCarry type §b$typeName §cnot found."))
                        )
                        return@executes 1
                    }
                    val referencingClients = CarryState.clients.values.filter {
                        it.typeName.equals(typeName, ignoreCase = true)
                    }
                    if (referencingClients.isNotEmpty()) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§cCannot remove type §b$typeName§c: it is in use by active clients."))
                        )
                        return@executes 1
                    }
                    CarryState.types.remove(key)
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("§aRemoved carry type: §b$typeName"))
                    )
                    1
                }
        )

    // ---- /cm list-client ----

    private fun listClientNode() = ClientCommandManager.literal("list-client")
        .executes { context ->
            if (!enabledCheck(context.source)) return@executes 1
            if (CarryState.clients.isEmpty()) {
                context.source.sendFeedback(
                    Constants.makePrefix().copy()
                        .append(Component.literal("§eYou have 0 clients."))
                )
            } else {
                context.source.sendFeedback(
                    Constants.makePrefix().copy()
                        .append(Component.literal("§fYou have §f${CarryState.clients.size} §fclient(s):"))
                )
                CarryState.clients.values.forEach { client ->
                    context.source.sendFeedback(
                        Component.literal(" §7- §d${client.playerName} §7| §b${client.typeName} §7| §f${client.amount} §7(done: §f${client.completed}§7)")
                    )
                }
            }
            1
        }

    // ---- /cm list-type ----

    private fun listTypeNode() = ClientCommandManager.literal("list-type")
        .executes { context ->
            if (!enabledCheck(context.source)) return@executes 1
            if (CarryState.types.isEmpty()) {
                context.source.sendFeedback(
                    Constants.makePrefix().copy()
                        .append(Component.literal("§eNo carry types defined."))
                )
            } else {
                context.source.sendFeedback(
                    Constants.makePrefix().copy()
                        .append(Component.literal("§fAvailable types:"))
                )
                CarryState.types.values.forEach { type ->
                    val priceStr = CarryPriceUtil.formatPrice(type.price)
                    val bulkStr = type.bulkPrice?.let { " §7/ §6${CarryPriceUtil.formatPrice(it)} §7(§f${type.bulkThreshold}+§7)" } ?: ""
                    context.source.sendFeedback(
                        Component.literal(" §7- §b${type.name} §7| §6${priceStr}$bulkStr")
                    )
                }
            }
            1
        }

    // ---- /cm done <amount> <playerName> ----

    private fun doneNode() = ClientCommandManager.literal("done")
        .then(
            ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                .then(
                    ClientCommandManager.argument("playerName", StringArgumentType.word())
                        .suggests(suggestClients)
                        .executes { context ->
                            if (!enabledCheck(context.source)) return@executes 1
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val client = CarryState.clients[playerName.lowercase()]
                            if (client == null) {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§cPlayer §d$playerName §cis not a client."))
                                )
                                return@executes 1
                            }
                            val type = CarryState.types[client.typeName.lowercase()]
                            if (type == null) {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§cReferenced type §b${client.typeName} §cnot found."))
                                )
                                return@executes 1
                            }
                            val remaining = client.amount - client.completed
                            if (remaining <= 0) {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§e${client.playerName} has no remaining carries to complete."))
                                )
                                return@executes 1
                            }
                            val actualDone = amount.coerceAtMost(remaining)
                            client.completed += actualDone

                            sendPartyChat("${client.completed}/${client.amount}")

                            if (client.completed >= client.amount) {
                                val unitPrice = CarryPriceUtil.effectivePrice(type, client.amount)
                                CarryState.status.totalOrders++
                                CarryState.status.totalCarries += client.amount
                                CarryState.status.totalEarned += unitPrice * client.amount
                                CarryState.saveHistory()
                                CarryState.clients.remove(playerName.lowercase())

                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§aIt looks like you've finished the carry!"))
                                )
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§aAutomatically removed §d${client.playerName} §afrom client list."))
                                )
                            } else {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.literal("§fRecorded §f$actualDone §fcarry(s) for §d${client.playerName}§f. Progress: §f${client.completed}§7/§f${client.amount}"))
                                )
                            }
                            1
                        }
                )
        )

    // ---- /cm refund <playerName> ----

    private fun refundNode() = ClientCommandManager.literal("refund")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!enabledCheck(context.source)) return@executes 1
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val client = CarryState.clients[playerName.lowercase()]
                    if (client == null) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§cPlayer §d$playerName §cis not a client."))
                        )
                        return@executes 1
                    }
                    val type = CarryState.types[client.typeName.lowercase()]
                    if (type == null) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§cReferenced type §b${client.typeName} §cnot found."))
                        )
                        return@executes 1
                    }
                    val remaining = client.amount - client.completed
                    val unitPrice = CarryPriceUtil.effectivePrice(type, client.amount)
                    val refundAmount = remaining * unitPrice

                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("§d${client.playerName} §frequested §f${client.amount} §fcarries, did §f${client.completed} §fso far"))
                    )

                    if (remaining > 0) {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§fYou need refund §f$remaining §7* §6${CarryPriceUtil.formatPrice(unitPrice)} §7= §6${CarryPriceUtil.formatPrice(refundAmount)} §fcoins"))
                        )
                    } else {
                        context.source.sendFeedback(
                            Constants.makePrefix().copy()
                                .append(Component.literal("§eNo carries remaining to refund."))
                        )
                    }

                    if (client.completed > 0) {
                        CarryState.status.totalOrders++
                        CarryState.status.totalCarries += client.completed
                        CarryState.status.totalEarned += unitPrice * client.completed
                        CarryState.saveHistory()
                    }

                    CarryState.clients.remove(playerName.lowercase())
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("§aIt looks like you've finished the carry!"))
                    )
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("§aAutomatically removed §d${client.playerName} §afrom client list."))
                    )
                    1
                }
        )

    // ---- /cm status ----

    private fun statusNode() = ClientCommandManager.literal("status")
        .executes { context ->
            if (!enabledCheck(context.source)) return@executes 1
            val s = CarryState.status
            if (s.totalOrders == 0 && s.totalCarries == 0 && s.totalEarned == 0L) {
                context.source.sendFeedback(
                    Constants.makePrefix().copy()
                        .append(Component.literal("§eNo completed orders yet."))
                )
            } else {
                context.source.sendFeedback(
                    Constants.makePrefix().copy()
                        .append(Component.literal("§fYou've finished §f${s.totalOrders} §forder(s), totalling §f${s.totalCarries} §fcarries."))
                )
                context.source.sendFeedback(
                    Constants.makePrefix().copy()
                        .append(Component.literal("§fYou've earned §6${CarryPriceUtil.formatPrice(s.totalEarned)} §fcoins so far."))
                )
            }
            1
        }

    // ---- /cm clear-client ----

    private fun clearClientNode() = ClientCommandManager.literal("clear-client")
        .executes { context ->
            if (!enabledCheck(context.source)) return@executes 1
            CarryState.clients.clear()
            context.source.sendFeedback(
                Constants.makePrefix().copy()
                    .append(Component.literal("§eCleared all clients from the list."))
            )
            1
        }

    // ---- /cm clear-history ----

    private fun clearHistoryNode() = ClientCommandManager.literal("clear-history")
        .executes { context ->
            if (!enabledCheck(context.source)) return@executes 1
            CarryState.status = CarryStatus()
            CarryState.saveHistory()
            context.source.sendFeedback(
                Constants.makePrefix().copy()
                    .append(Component.literal("§eEarnings history has been reset."))
            )
            1
        }
}
