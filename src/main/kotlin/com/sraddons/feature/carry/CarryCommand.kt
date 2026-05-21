package com.sraddons.feature.carry

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.sendPartyChat
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
                .then(addClientNode())
                .then(setPriceNode())
                .then(setBulkPriceNode())
                .then(setAmountNode())
                .then(addAmountNode())
                .then(removeAmountNode())
                .then(calcPriceNode())
                .then(removeClientNode())
                .then(removeTypeNode())
                .then(listClientNode())
                .then(listTypeNode())
                .then(doneNode())
                .then(refundNode())
                .then(statusNode())
                .then(clearClientNode())
                .then(clearHistoryNode())
                .then(undoNode())
                .then(addMinibossNode())
                .then(removeMinibossNode())
                .also { dispatcher.register(it) }
        }
    }

    private val suggestTypes = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryState.types.values.forEach { builder.suggest(it.name) }
        builder.buildFuture()
    }

    private val suggestClients = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryState.clients.values.forEach { builder.suggest(it.playerName) }
        builder.buildFuture()
    }

    private val suggestMinibossNames = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryState.minibossNames.forEach { builder.suggest("\"$it\"") }
        builder.buildFuture()
    }

    private fun buildHelpLine(syntax: String, descKey: String): Component {
        return Component.literal(syntax)
            .append(Component.translatable(descKey))
    }

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

    // ---- /cm add-type <typeName> ----

    private fun addTypeNode() = ClientCommandManager.literal("add-type")
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

    // ---- /cm add-client <playerName> <typeName> <amount> ----

    private fun addClientNode() = ClientCommandManager.literal("add-client")
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
                                        playerName = playerName,
                                        typeName = typeName,
                                        amount = amount
                                    )
                                    CarryState.saveData()
                                    context.source.feedback(Component.translatable("sraddons.carry.client_added",
                                                Component.literal(playerName).withColor(0xFF55FF),
                                                Component.literal(amount.toString()).withColor(0xFFAA00),
                                                Component.literal(typeName).withColor(0x55FFFF).withColor(0x55FF55))
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
                            if (!context.source.requireEnabled()) return@executes 1
                            CarryState.saveUndo()
                            val typeName = StringArgumentType.getString(context, "typeName")
                            val priceStr = StringArgumentType.getString(context, "price")
                            val key = typeName.lowercase()
                            val type = CarryState.types[key]
                            if (type == null) {
                                context.source.feedback(Component.translatable("sraddons.carry.type_not_found",
                                            Component.literal(typeName).withColor(0x55FFFF)).withColor(0xFF5555))
                                return@executes 1
                            }
                            val parsed = CarryPriceUtil.parsePrice(priceStr)
                            if (parsed == null) {
                                context.source.feedback(Component.translatable("sraddons.carry.price_invalid").withColor(0xFF5555))
                                return@executes 1
                            }
                            type.price = parsed
                            CarryState.saveData()
                            context.source.feedback(Component.translatable("sraddons.carry.price_set",
                                        Component.literal(type.name).withColor(0x55FFFF),
                                        Component.literal(CarryPriceUtil.formatPrice(parsed)).withColor(0xFFAA00).withColor(0x55FF55))
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
                                    if (!context.source.requireEnabled()) return@executes 1
                                    CarryState.saveUndo()
                                    val typeName = StringArgumentType.getString(context, "typeName")
                                    val bulkPriceStr = StringArgumentType.getString(context, "bulkPrice")
                                    val threshold = IntegerArgumentType.getInteger(context, "threshold")
                                    val key = typeName.lowercase()
                                    val type = CarryState.types[key]
                                    if (type == null) {
                                        context.source.feedback(Component.translatable("sraddons.carry.type_not_found",
                                                    Component.literal(typeName).withColor(0x55FFFF)).withColor(0xFF5555))
                                        return@executes 1
                                    }
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
                                                Component.literal(threshold.toString()).withColor(0x55FFFF).withColor(0x55FF55))
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
                            if (!context.source.requireEnabled()) return@executes 1
                            CarryState.saveUndo()
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = CarryState.clients[playerName.lowercase()]
                            if (client == null) {
                                context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                                            Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                                return@executes 1
                            }
                            client.amount += amount
                            CarryState.saveData()
                            context.source.feedback(Component.translatable("sraddons.carry.amount_added",
                                        Component.literal(amount.toString()).withColor(0xFFAA00),
                                        Component.literal(client.playerName).withColor(0xFF55FF).withColor(0x55FF55))
                            )
                            context.source.feedback(Component.translatable("sraddons.carry.amount_total",
                                        Component.literal(client.amount.toString()).withColor(0xFFFFFF)).withColor(0xAAAAAA))
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
                            if (!context.source.requireEnabled()) return@executes 1
                            CarryState.saveUndo()
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = CarryState.clients[playerName.lowercase()]
                            if (client == null) {
                                context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                                            Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                                return@executes 1
                            }
                            val actualRemove = amount.coerceAtMost(client.amount)
                            client.amount -= actualRemove
                            if (client.completed > client.amount) {
                                client.completed = client.amount
                            }
                            CarryState.saveData()
                            context.source.feedback(Component.translatable("sraddons.carry.amount_removed",
                                        Component.literal(actualRemove.toString()).withColor(0xFFAA00),
                                        Component.literal(client.playerName).withColor(0xFF55FF).withColor(0x55FF55))
                            )
                            context.source.feedback(Component.translatable("sraddons.carry.amount_total",
                                        Component.literal(client.amount.toString()).withColor(0xFFFFFF)).withColor(0xAAAAAA))
                            1
                        }
                )
        )

    // ---- /cm set-amount <playerName> <amount> [useBulk] ----

    private fun setAmountNode() = ClientCommandManager.literal("set-amount")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .then(
                    ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!context.source.requireEnabled()) return@executes 1
                            CarryState.saveUndo()
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = CarryState.clients[playerName.lowercase()]
                            if (client == null) {
                                context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                                            Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                                return@executes 1
                            }
                            client.amount = amount
                            client.useBulk = false
                            if (client.completed > client.amount) {
                                client.completed = client.amount
                            }
                            CarryState.saveData()
                            context.source.feedback(Component.translatable("sraddons.carry.amount_set",
                                        Component.literal(client.playerName).withColor(0xFF55FF),
                                        Component.literal(amount.toString()).withColor(0xFFAA00),
                                        Component.translatable("sraddons.carry.bool.false").withColor(0xAAAAAA).withColor(0x55FF55))
                            )
                            1
                        }
                        .then(
                            ClientCommandManager.argument("useBulk", BoolArgumentType.bool())
                                .executes { context ->
                                    if (!context.source.requireEnabled()) return@executes 1
                                    CarryState.saveUndo()
                                    val playerName = StringArgumentType.getString(context, "playerName")
                                    val amount = IntegerArgumentType.getInteger(context, "amount")
                                    val useBulk = BoolArgumentType.getBool(context, "useBulk")
                                    val client = CarryState.clients[playerName.lowercase()]
                                    if (client == null) {
                                        context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                                                    Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                                        return@executes 1
                                    }
                                    client.amount = amount
                                    client.useBulk = useBulk
                                    if (client.completed > client.amount) {
                                        client.completed = client.amount
                                    }
                                    CarryState.saveData()
                                    val bulkKey = if (useBulk) "sraddons.carry.bool.true" else "sraddons.carry.bool.false"
                                    context.source.feedback(Component.translatable("sraddons.carry.amount_set",
                                                Component.literal(client.playerName).withColor(0xFF55FF),
                                                Component.literal(amount.toString()).withColor(0xFFAA00),
                                                Component.translatable(bulkKey).withColor(0xAAAAAA).withColor(0x55FF55))
                                    )
                                    1
                                }
                        )
                )
        )

    // ---- /cm calc-price [playerName] ----

    private fun calcPriceNode() = ClientCommandManager.literal("calc-price")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            calcPriceWithDefault(context.source)
            1
        }
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val client = CarryState.clients[playerName.lowercase()]
                    if (client == null) {
                        context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                                    Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    calcPriceForClient(context.source, client)
                    1
                }
        )

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
        val type = CarryState.types[client.typeName.lowercase()]
        if (type == null) {
            source.feedback(Component.translatable("sraddons.carry.type_not_found",
                        Component.literal(client.typeName).withColor(0x55FFFF)).withColor(0xFF5555))
            return
        }
        val unitPrice = CarryPriceUtil.effectivePrice(type, client.amount)
        val total = unitPrice * client.amount
        val usingBulk = type.bulkPrice != null && client.amount >= type.bulkThreshold
        if (usingBulk) {
            source.feedback(Component.translatable("sraddons.carry.calc_price.bulk",
                        Component.literal(client.amount.toString()).withColor(0x55FFFF),
                        Component.literal(CarryPriceUtil.formatPrice(unitPrice)).withColor(0xFFAA00),
                        Component.literal(type.bulkThreshold.toString()).withColor(0xFFFFFF),
                        Component.literal(CarryPriceUtil.formatPrice(total)).withColor(0xFFAA00).withColor(0xFFFFFF))
            )
        } else {
            source.feedback(Component.translatable("sraddons.carry.calc_price.standard",
                        Component.literal(client.amount.toString()).withColor(0x55FFFF),
                        Component.literal(CarryPriceUtil.formatPrice(unitPrice)).withColor(0xFFAA00),
                        Component.literal(CarryPriceUtil.formatPrice(total)).withColor(0xFFAA00).withColor(0xFFFFFF))
            )
        }
    }

    // ---- /cm remove-client <playerName> ----

    private fun removeClientNode() = ClientCommandManager.literal("remove-client")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
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

    // ---- /cm remove-type <typeName> ----

    private fun removeTypeNode() = ClientCommandManager.literal("remove-type")
        .then(
            ClientCommandManager.argument("typeName", StringArgumentType.word())
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

    // ---- /cm list-client ----

    private fun listClientNode() = ClientCommandManager.literal("list-client")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            if (CarryState.clients.isEmpty()) {
                context.source.feedback(Component.translatable("sraddons.carry.client_zero").withColor(0xFFFF55))
            } else {
                context.source.feedback(Component.translatable("sraddons.carry.client_count",
                            Component.literal(CarryState.clients.size.toString()).withColor(0xFFFFFF).withColor(0xFFFFFF))
                )
                CarryState.clients.values.forEach { client ->
                    context.source.sendFeedback(
                        Component.translatable("sraddons.carry.client_list_entry",
                            Component.literal(client.playerName).withColor(0xFF55FF),
                            Component.literal(client.typeName).withColor(0x55FFFF),
                            Component.literal(client.amount.toString()).withColor(0xFFAA00),
                            Component.literal(client.completed.toString()).withColor(0x55FF55)
                        ).withColor(0xAAAAAA)
                    )
                }
            }
            1
        }

    // ---- /cm list-type ----

    private fun listTypeNode() = ClientCommandManager.literal("list-type")
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
                            Component.literal(type.name).withColor(0x55FFFF),
                            priceStr,
                            bulkStr
                        )
                    )
                }
            }
            1
        }

    // ---- /cm done [playerName] [amount] ----

    private fun doneNode() = ClientCommandManager.literal("done")
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
                    val client = CarryState.clients[playerName.lowercase()]
                    if (client == null) {
                        context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                                    Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    executeDone(context.source, 1, client)
                    1
                }
                .then(
                    ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!context.source.requireEnabled()) return@executes 1
                            val playerName = StringArgumentType.getString(context, "playerName")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val client = CarryState.clients[playerName.lowercase()]
                            if (client == null) {
                                context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                                            Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                                return@executes 1
                            }
                            executeDone(context.source, amount, client)
                            1
                        }
                )
        )

    private fun doneWithDefaultAmount(source: FabricClientCommandSource, amount: Int) {
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
        val client = CarryState.clients.values.first()
        executeDone(source, amount, client)
    }

    private fun executeDone(source: FabricClientCommandSource, amount: Int, client: CarryClient) {
        CarryState.saveUndo()
        val type = CarryState.types[client.typeName.lowercase()]
        if (type == null) {
            source.feedback(Component.translatable("sraddons.carry.type_not_found",
                        Component.literal(client.typeName).withColor(0x55FFFF)).withColor(0xFF5555))
            return
        }
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
            val unitPrice = CarryPriceUtil.effectivePrice(type, client.amount)
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

    // ---- /cm refund <playerName> ----

    private fun refundNode() = ClientCommandManager.literal("refund")
        .then(
            ClientCommandManager.argument("playerName", StringArgumentType.word())
                .suggests(suggestClients)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1
                    CarryState.saveUndo()
                    val playerName = StringArgumentType.getString(context, "playerName")
                    val client = CarryState.clients[playerName.lowercase()]
                    if (client == null) {
                        context.source.feedback(Component.translatable("sraddons.carry.client_not_found",
                                    Component.literal(playerName).withColor(0xFF55FF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    val type = CarryState.types[client.typeName.lowercase()]
                    if (type == null) {
                        context.source.feedback(Component.translatable("sraddons.carry.type_not_found",
                                    Component.literal(client.typeName).withColor(0x55FFFF)).withColor(0xFF5555))
                        return@executes 1
                    }
                    val remaining = client.amount - client.completed
                    val unitPrice = CarryPriceUtil.effectivePrice(type, client.amount)
                    val refundAmount = remaining * unitPrice

                    context.source.feedback(Component.translatable("sraddons.carry.refund_info",
                                Component.literal(client.playerName).withColor(0xFF55FF),
                                Component.literal(client.amount.toString()).withColor(0xFFAA00),
                                Component.literal(client.completed.toString()).withColor(0x55FF55).withColor(0xFFFFFF))
                    )

                    if (remaining > 0) {
                        context.source.feedback(Component.translatable("sraddons.carry.refund_amount",
                                    Component.literal(remaining.toString()).withColor(0x55FFFF),
                                    Component.literal(CarryPriceUtil.formatPrice(unitPrice)).withColor(0xFFAA00),
                                    Component.literal(CarryPriceUtil.formatPrice(refundAmount)).withColor(0xFFAA00).withColor(0xFFFFFF))
                        )
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

    // ---- /cm status ----

    private fun statusNode() = ClientCommandManager.literal("status")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            val s = CarryState.status
            if (s.totalOrders == 0 && s.totalCarries == 0 && s.totalEarned == 0L) {
                context.source.feedback(Component.translatable("sraddons.carry.no_orders").withColor(0xFFFF55))
            } else {
                context.source.feedback(Component.translatable("sraddons.carry.orders_summary",
                            Component.literal(s.totalOrders.toString()).withColor(0xFFAA00),
                            Component.literal(s.totalCarries.toString()).withColor(0x55FFFF)
                        ).withColor(0xFFFFFF))
                context.source.feedback(Component.translatable("sraddons.carry.earnings_summary",
                            Component.literal(CarryPriceUtil.formatPrice(s.totalEarned)).withColor(0xFFAA00)
                        ).withColor(0xFFFFFF))
            }
            1
        }

    // ---- /cm clear-client ----

    private fun clearClientNode() = ClientCommandManager.literal("clear-client")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            CarryState.saveUndo()
            CarryState.clients.clear()
            CarryState.saveData()
            context.source.feedback(Component.translatable("sraddons.carry.all_cleared").withColor(0xFFFF55))
            1
        }

    // ---- /cm clear-history ----

    private fun clearHistoryNode() = ClientCommandManager.literal("clear-history")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            CarryState.saveUndo()
            CarryState.status = CarryStatus()
            CarryState.saveHistory()
            context.source.feedback(Component.translatable("sraddons.carry.history_reset").withColor(0xFFFF55))
            1
        }

    // ---- /cm undo ----

    private fun undoNode() = ClientCommandManager.literal("undo")
        .executes { context ->
            if (!context.source.requireEnabled()) return@executes 1
            if (CarryState.undo()) {
                context.source.feedback(Component.translatable("sraddons.carry.undo.success").withColor(0x55FF55))
            } else {
                context.source.feedback(Component.translatable("sraddons.carry.undo.nothing").withColor(0xFFFF55))
            }
            1
        }

    // ---- /cm add-miniboss "<NAME>" ----

    private fun addMinibossNode() = ClientCommandManager.literal("add-miniboss")
        .then(
            ClientCommandManager.argument("name", StringArgumentType.string())
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1

                    val argNode = context.nodes.find { it.node.name == "name" } ?: return@executes 1
                    val rawArg = context.input.substring(argNode.range.start, argNode.range.end)
                    if (!rawArg.startsWith("\"") || !rawArg.endsWith("\"")) {
                        context.source.feedback(Component.translatable("sraddons.carry.quotes_required").withColor(0xFF5555))
                        return@executes 1
                    }

                    val name = StringArgumentType.getString(context, "name")
                    
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

    // ---- /cm remove-miniboss "<NAME>" ----

    private fun removeMinibossNode() = ClientCommandManager.literal("remove-miniboss")
        .then(
            ClientCommandManager.argument("name", StringArgumentType.string())
                .suggests(suggestMinibossNames)
                .executes { context ->
                    if (!context.source.requireEnabled()) return@executes 1

                    val argNode = context.nodes.find { it.node.name == "name" } ?: return@executes 1
                    val rawArg = context.input.substring(argNode.range.start, argNode.range.end)
                    if (!rawArg.startsWith("\"") || !rawArg.endsWith("\"")) {
                        context.source.feedback(Component.translatable("sraddons.carry.quotes_required").withColor(0xFF5555))
                        return@executes 1
                    }

                    val name = StringArgumentType.getString(context, "name")
                    
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
