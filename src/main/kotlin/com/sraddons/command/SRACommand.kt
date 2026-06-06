package com.sraddons.command

import com.sraddons.config.SRConfig
import com.sraddons.feature.helper.TextReplacer
import com.sraddons.gui.SRConfigGui
import com.sraddons.update.UpdateChecker
import com.sraddons.util.CalcUtil
import com.sraddons.util.Constants
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.CoroutineScope
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager

object SRACommand {

    private val LOGGER = LogManager.getLogger("SR-Addons-Command")

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val root = ClientCommandManager.literal("sra")

            // /sra reload
            val reloadNode = ClientCommandManager.literal("reload")
                .executes { context ->
                    SRConfig.load()
                    try {
                        com.sraddons.feature.partycommands.commands.Commands.rebuildDispatcher()
                    } catch (e: Exception) {
                        LOGGER.warn("Failed to rebuild dispatcher during reload", e)
                    }
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.translatable("sraddons.command.reloaded").withColor(0x55FF55))
                    )
                    1
                }

            // /sra config / /sra gui
            val configNode = ClientCommandManager.literal("config").executes(::openGuiCommand)
            val guiNode = ClientCommandManager.literal("gui").executes(::openGuiCommand)

            // /sra version
            val versionNode = ClientCommandManager.literal("version")
                .executes { context ->
                    val prefix = Constants.makePrefix()
                    context.source.sendFeedback(
                        prefix.copy()
                            .append(Component.translatable("sraddons.command.version", Constants.MOD_VERSION).withColor(0xFFFFFF))
                    )
                    context.source.sendFeedback(
                        prefix.copy()
                            .append(Component.translatable("sraddons.command.author").withColor(0xAAAAAA))
                    )
                    1
                }

            // /sra update -- disabled in DEBUG version
            val updateNode = ClientCommandManager.literal("update")
                .executes { context ->
                    val prefix = Constants.makePrefix()
                    context.source.sendFeedback(
                        prefix.copy()
                            .append(Component.translatable("sraddons.command.update.checking").withColor(0xFFFF55))
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        val result = UpdateChecker.check()
                        val mc = net.minecraft.client.Minecraft.getInstance()
                        mc.execute {
                            if (result.downloadUrl != null) {
                                val clickStyle = Style.EMPTY
                                    .withUnderlined(true)
                                    .withClickEvent(ClickEvent.OpenUrl(URI.create(result.downloadUrl)))

                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.translatable("sraddons.command.update.available", result.latestVersion, Constants.MOD_VERSION).withColor(0x55FF55))
                                )
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.translatable("sraddons.command.update.click").withColor(0xFFFFFF))
                                        .append(Component.translatable("sraddons.command.update.here").withColor(0x55FFFF).withStyle(clickStyle))
                                        .append(Component.translatable("sraddons.command.update.check_out").withColor(0xFFFFFF))
                                )
                            } else if (result.latestVersion == "unknown") {
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.translatable("sraddons.command.update.error").withColor(0xFF5555))
                                )
                            } else {
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.translatable("sraddons.command.update.latest", Constants.MOD_VERSION).withColor(0x55FF55))
                                )
                            }
                        }
                    }
                    1
                }

            // /sra calc <expression>
            val calcNode = ClientCommandManager.literal("calc")
                .then(
                    ClientCommandManager.argument("expression", StringArgumentType.greedyString())
                        .executes { context ->
                            val expr = StringArgumentType.getString(context, "expression")
                            executeCalc(context.source, expr)
                            1
                        }
                )

            // /sra (no arguments - show help)
            val helpNode = root.executes { context ->
                val prefix = Constants.makePrefix()
                context.source.sendFeedback(
                    prefix.copy()
                        .append(Component.translatable("sraddons.command.version", Constants.MOD_VERSION).withColor(0xFFFFFF))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra reload §8- §f")
                        .append(Component.translatable("sraddons.command.help.reload_desc"))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra config §8- §f")
                        .append(Component.translatable("sraddons.command.help.config_desc"))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra gui §8- §f")
                        .append(Component.translatable("sraddons.command.help.gui_desc"))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra version §8- §f")
                        .append(Component.translatable("sraddons.command.help.version_desc"))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra update §8- §f")
                        .append(Component.translatable("sraddons.command.help.update_desc"))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra calc <expression> §8- §f")
                        .append(Component.translatable("sraddons.command.help.calc_desc"))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra replaceTexts add \"word\" \"replacement\" §8- §f")
                        .append(Component.literal("Add text replacement"))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra replaceTexts remove \"word\" §8- §f")
                        .append(Component.literal("Remove text replacement"))
                )
                context.source.sendFeedback(
                    Component.literal("§7/sra replaceTexts list §8- §f")
                        .append(Component.literal("List custom replacements"))
                )
                1
            }

            // /sra replaceTexts
            val rtNode = ClientCommandManager.literal("replaceTexts")
                .executes { context ->
                    val prefix = Constants.makePrefix()
                    context.source.sendFeedback(
                        prefix.copy()
                            .append(Component.literal("Replace Texts Commands:").withColor(0x55FFFF))
                    )
                    context.source.sendFeedback(
                        Component.literal("§7/sra replaceTexts add \"word\" \"replacement\" §8- §fAdd a replacement")
                    )
                    context.source.sendFeedback(
                        Component.literal("§7/sra replaceTexts remove \"word\" §8- §fRemove a replacement")
                    )
                    context.source.sendFeedback(
                        Component.literal("§7/sra replaceTexts list §8- §fList custom replacements")
                    )
                    context.source.sendFeedback(
                        Component.literal("")
                    )
                    context.source.sendFeedback(
                        Component.literal("§7Replacement formats:").withColor(0x55FFFF)
                    )
                    context.source.sendFeedback(
                        Component.literal("  §7& codes: §f&cRed &lBold &nUnderline §8+ §7§r")
                    )
                    context.source.sendFeedback(
                        Component.literal("  §7Gradient: §f:g:<name>:<text> §7§o(available: cyanToLightBlue, goldToYellow, aquaToGreen, redToOrange, purpleToPink)")
                    )
                    1
                }
                .then(
                    ClientCommandManager.literal("add")
                        .then(
                            ClientCommandManager.argument("word", StringArgumentType.string())
                                .then(
                                    ClientCommandManager.argument("replacement", StringArgumentType.string())
                                        .executes { context ->
                                            val word = StringArgumentType.getString(context, "word")
                                            val replacement = StringArgumentType.getString(context, "replacement")
                                            val prefix = Constants.makePrefix()
                                            TextReplacer.add(word, replacement)
                                            context.source.sendFeedback(
                                                prefix.copy()
                                                    .append(Component.literal("Added replacement: \"$word\" → \"$replacement\"").withColor(0x55FF55))
                                            )
                                            1
                                        }
                                )
                        )
                )
                .then(
                    ClientCommandManager.literal("remove")
                        .then(
                            ClientCommandManager.argument("word", StringArgumentType.string())
                                .suggests { _, builder ->
                                    TextReplacer.getCustoms().keys.forEach { builder.suggest("\"$it\"") }
                                    builder.buildFuture()
                                }
                                .executes { context ->
                                    val word = StringArgumentType.getString(context, "word")
                                    val prefix = Constants.makePrefix()
                                    if (TextReplacer.remove(word)) {
                                        context.source.sendFeedback(
                                            prefix.copy()
                                                .append(Component.literal("Removed replacement: \"$word\"").withColor(0x55FF55))
                                        )
                                    } else {
                                        context.source.sendFeedback(
                                            prefix.copy()
                                                .append(Component.literal("Replacement not found: \"$word\"").withColor(0xFF5555))
                                        )
                                    }
                                    1
                                }
                        )
                )
                .then(
                    ClientCommandManager.literal("list")
                        .executes { context ->
                            val prefix = Constants.makePrefix()
                            val customs = TextReplacer.getCustoms()
                            if (customs.isEmpty()) {
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.literal("No custom replacements configured.").withColor(0xAAAAAA))
                                )
                            } else {
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.literal("Custom Replacements (${customs.size}):").withColor(0x55FFFF))
                                )
                                customs.entries.forEachIndexed { i, (k, v) ->
                                    context.source.sendFeedback(
                                        Component.literal("§7${i + 1}. §f$k §8→ §f$v")
                                    )
                                }
                            }
                            1
                        }
                )

            root
                .then(reloadNode)
                .then(configNode)
                .then(guiNode)
                .then(versionNode)
                .then(updateNode)
                .then(calcNode)
                .then(rtNode)
                .then(buildAlertNode())
                .also { dispatcher.register(it) }

            // Standalone /calc command (with TAB completion; Mixin ensures priority over other mods)
            val calcRoot = ClientCommandManager.literal("calc")
                .then(
                    ClientCommandManager.argument("expression", StringArgumentType.greedyString())
                        .executes { context ->
                            if (!SRConfig.settings.general.enableStandaloneCalc) {
                                context.source.sendFeedback(
                                    Constants.makePrefix().copy()
                                        .append(Component.translatable("sraddons.command.calc.standalone_disabled").withColor(0xFF5555))
                                )
                                return@executes 1
                            }
                            val expr = StringArgumentType.getString(context, "expression")
                            executeCalc(context.source, expr)
                            1
                        }
                )
            dispatcher.register(calcRoot)
        }
    }

    private fun executeCalc(
        source: net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource,
        expr: String
    ) {
        val prefix = Constants.makePrefix()
        try {
            val result = CalcUtil.evaluate(expr)
            source.sendFeedback(
                prefix.copy()
                    .append(Component.literal("§7$expr = §a${CalcUtil.format(result)}"))
            )
        } catch (e: Exception) {
            source.sendFeedback(
                prefix.copy()
                    .append(Component.translatable("sraddons.command.calc.error").withColor(0xFF5555))
            )
        }
    }

    private fun buildAlertNode() = ClientCommandManager.literal("alert")
        .executes { context ->
            val prefix = Constants.makePrefix()
            context.source.sendFeedback(prefix.copy().append(Component.literal("Chat Alert commands:").withColor(0x55FFFF)))
            context.source.sendFeedback(Component.literal("§7/sra alert add \"keyword\" \"subtitle\" [cooldown] §8- §fAdd alert (cooldown in seconds, default 5)"))
            context.source.sendFeedback(Component.literal("§7/sra alert remove \"keyword\" §8- §fRemove alert"))
            context.source.sendFeedback(Component.literal("§7/sra alert list §8- §fList all alerts"))
            context.source.sendFeedback(Component.literal("§7/sra alert clear §8- §fRemove all alerts"))
            1
        }
        .then(
            ClientCommandManager.literal("add")
                .then(
                    ClientCommandManager.argument("keyword", StringArgumentType.string())
                        .then(
                            ClientCommandManager.argument("subtitle", StringArgumentType.string())
                                .executes { context ->
                                    val keyword = StringArgumentType.getString(context, "keyword")
                                    val subtitle = StringArgumentType.getString(context, "subtitle")
                                    val prefix = Constants.makePrefix()
                                    SRConfig.settings.chatAlert.entries.add("$keyword | $subtitle | 5 | yes | yes")
                                    SRConfig.save()
                                    context.source.sendFeedback(
                                        prefix.copy()
                                            .append(Component.literal("Added alert: \"$keyword\" → \"$subtitle\" (cooldown: 5s)").withColor(0x55FF55))
                                    )
                                    1
                                }
                                .then(
                                    ClientCommandManager.argument("cooldown", IntegerArgumentType.integer(0))
                                        .executes { context ->
                                            val keyword = StringArgumentType.getString(context, "keyword")
                                            val subtitle = StringArgumentType.getString(context, "subtitle")
                                            val cooldown = IntegerArgumentType.getInteger(context, "cooldown")
                                            val prefix = Constants.makePrefix()
                                            SRConfig.settings.chatAlert.entries.add("$keyword | $subtitle | $cooldown | yes | yes")
                                            SRConfig.save()
                                            context.source.sendFeedback(
                                                prefix.copy()
                                                    .append(Component.literal("Added alert: \"$keyword\" → \"$subtitle\" (cooldown: ${cooldown}s)").withColor(0x55FF55))
                                            )
                                            1
                                        }
                                )
                        )
                )
        )
        .then(
            ClientCommandManager.literal("remove")
                .then(
                    ClientCommandManager.argument("keyword", StringArgumentType.string())
                        .suggests { _, builder ->
                            SRConfig.settings.chatAlert.entries.forEach { entry ->
                                val kw = entry.split(" | ").getOrElse(0) { entry }
                                builder.suggest("\"$kw\"")
                            }
                            builder.buildFuture()
                        }
                        .executes { context ->
                            val keyword = StringArgumentType.getString(context, "keyword")
                            val prefix = Constants.makePrefix()
                            val removed = SRConfig.settings.chatAlert.entries.removeAll {
                                it.split(" | ").getOrElse(0) { "" }.equals(keyword, ignoreCase = true)
                            }
                            if (removed) {
                                SRConfig.save()
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.literal("Removed alert: \"$keyword\"").withColor(0x55FF55))
                                )
                            } else {
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.literal("Alert not found: \"$keyword\"").withColor(0xFF5555))
                                )
                            }
                            1
                        }
                )
        )
        .then(
            ClientCommandManager.literal("list")
                .executes { context ->
                    val prefix = Constants.makePrefix()
                    val entries = SRConfig.settings.chatAlert.entries
                    if (entries.isEmpty()) {
                        context.source.sendFeedback(prefix.copy().append(Component.literal("No alerts configured.").withColor(0xAAAAAA)))
                    } else {
                        context.source.sendFeedback(prefix.copy().append(Component.literal("Chat Alerts (${entries.size}):").withColor(0x55FFFF)))
                        entries.forEachIndexed { i, raw ->
                            val parts = raw.split(" | ", limit = 5)
                            val kw = parts.getOrElse(0) { "" }
                            val sub = parts.getOrElse(1) { "" }
                            val cd = parts.getOrElse(2) { "5" }
                            val ip = parts.getOrElse(3) { "yes" }
                            val is_ = parts.getOrElse(4) { "yes" }
                            val flags = when {
                                ip == "no" && is_ == "no" -> "exact"
                                ip == "no" -> "prefix"
                                is_ == "no" -> "suffix"
                                else -> "contains"
                            }
                            context.source.sendFeedback(
                                Component.literal("§7${i + 1}. §f\"$kw\" §8→ §f\"$sub\" §7(${cd}s, $flags)")
                            )
                        }
                    }
                    1
                }
        )
        .then(
            ClientCommandManager.literal("clear")
                .executes { context ->
                    val prefix = Constants.makePrefix()
                    SRConfig.settings.chatAlert.entries.clear()
                    SRConfig.save()
                    context.source.sendFeedback(prefix.copy().append(Component.literal("All alerts cleared.").withColor(0x55FF55)))
                    1
                }
        )

    private fun openGuiCommand(context: com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>): Int {
        SRConfigGui.open()
        context.source.sendFeedback(
            Constants.makePrefix().copy()
                .append(Component.translatable("sraddons.command.gui.opening").withColor(0x55FF55))
        )
        return 1
    }

}
