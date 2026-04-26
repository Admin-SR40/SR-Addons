package com.sraddons.command

import com.sraddons.config.SRConfig
import com.sraddons.gui.SRConfigGui
import com.sraddons.update.UpdateChecker
import com.sraddons.util.Constants
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import kotlinx.coroutines.CoroutineScope
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
                    // PartyCommands dispatcher rebuild will be handled if the feature is initialized
                    try {
                        com.sraddons.feature.partycommands.commands.Commands.rebuildDispatcher()
                    } catch (_: Exception) {}
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("Config reloaded!").withColor(0x55FF55))
                    )
                    1
                }

            // /sra config / /sra gui
            val configNode = ClientCommandManager.literal("config")
                .executes { context ->
                    SRConfigGui.open()
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("Opening config GUI...").withColor(0x55FF55))
                    )
                    1
                }

            val guiNode = ClientCommandManager.literal("gui")
                .executes { context ->
                    SRConfigGui.open()
                    context.source.sendFeedback(
                        Constants.makePrefix().copy()
                            .append(Component.literal("Opening config GUI...").withColor(0x55FF55))
                    )
                    1
                }

            // /sra version
            val versionNode = ClientCommandManager.literal("version")
                .executes { context ->
                    val prefix = Constants.makePrefix()
                    context.source.sendFeedback(
                        prefix.copy()
                            .append(Component.literal("SR-Addons v${Constants.MOD_VERSION}").withColor(0xFFFFFF))
                    )
                    context.source.sendFeedback(
                        prefix.copy()
                            .append(Component.literal("Made by Admin_SR40").withColor(0xAAAAAA))
                    )
                    1
                }

            // /sra update
            val updateNode = ClientCommandManager.literal("update")
                .executes { context ->
                    val prefix = Constants.makePrefix()
                    context.source.sendFeedback(
                        prefix.copy()
                            .append(Component.literal("Checking for updates...").withColor(0xFFFF55))
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        val result = UpdateChecker.check()
                        val mc = net.minecraft.client.Minecraft.getInstance()
                        mc.execute {
                            if (result.downloadUrl != null) {
                                val link = Component.literal(result.downloadUrl)
                                    .withColor(0x55FFFF)
                                    .withStyle(Style.EMPTY
                                        .withUnderlined(true))

                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.literal("Update available! Latest: v${result.latestVersion} (current: v${Constants.MOD_VERSION})").withColor(0x55FF55))
                                )
                                context.source.sendFeedback(
                                    prefix.copy().append(link)
                                )
                            } else if (result.latestVersion == "unknown") {
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.literal("Unable to check for updates (network error)").withColor(0xFF5555))
                                )
                            } else {
                                context.source.sendFeedback(
                                    prefix.copy()
                                        .append(Component.literal("You are on the latest version! (v${Constants.MOD_VERSION})").withColor(0x55FF55))
                                )
                            }
                        }
                    }
                    1
                }

            // /sra (no arguments - show help)
            val helpNode = root.executes { context ->
                val prefix = Constants.makePrefix()
                context.source.sendFeedback(
                    prefix.copy()
                        .append(Component.literal("SR-Addons v${Constants.MOD_VERSION}").withColor(0xFFFFFF))
                )
                context.source.sendFeedback(Component.literal("§7/sra reload §8- §fReload config"))
                context.source.sendFeedback(Component.literal("§7/sra config §8- §fOpen config GUI"))
                context.source.sendFeedback(Component.literal("§7/sra gui §8- §fOpen config GUI"))
                context.source.sendFeedback(Component.literal("§7/sra version §8- §fShow version info"))
                context.source.sendFeedback(Component.literal("§7/sra update §8- §fCheck for updates"))
                1
            }

            root
                .then(reloadNode)
                .then(configNode)
                .then(guiNode)
                .then(versionNode)
                .then(updateNode)
                .also { dispatcher.register(it) }
        }
    }
}
