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
            val configNode = ClientCommandManager.literal("config")
                .executes { context ->
                    SRConfigGui.open()
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.translatable("sraddons.command.gui.opening").withColor(0x55FF55))
                    )
                    1
                }

            val guiNode = ClientCommandManager.literal("gui")
                .executes { context ->
                    SRConfigGui.open()
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.translatable("sraddons.command.gui.opening").withColor(0x55FF55))
                    )
                    1
                }

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
                                    TextReplacer.customs.keys.forEach { builder.suggest("\"$it\"") }
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
                            val customs = TextReplacer.customs
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

}
