package com.sraddons.command

import com.sraddons.config.SRConfig
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

object SRACommand {

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val root = ClientCommandManager.literal("sra")

            // /sra reload
            val reloadNode = ClientCommandManager.literal("reload")
                .executes { context ->
                    SRConfig.load()
                    try {
                        com.sraddons.feature.partycommands.commands.Commands.rebuildDispatcher()
                    } catch (_: Exception) {}
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

            // /sra update
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
                            val prefix = Constants.makePrefix()
                            try {
                                val result = CalcUtil.evaluate(expr)
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.literal("§7$expr = §a${CalcUtil.format(result)}"))
                                )
                            } catch (e: Exception) {
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.translatable("sraddons.command.calc.error").withColor(0xFF5555))
                                )
                            }
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
                1
            }

            root
                .then(reloadNode)
                .then(configNode)
                .then(guiNode)
                .then(versionNode)
                .then(updateNode)
                .then(calcNode)
                .also { dispatcher.register(it) }
        }
    }

}
