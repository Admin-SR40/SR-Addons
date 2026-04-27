package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object InfoCommands {
    private val mc = Minecraft.getInstance()

    fun register() {
        Commands.add(object : Command("help", "Show help message", "h") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes { showHelp(); Command.SINGLE_SUCCESS }
            }
        })

        Commands.add(object : Command("ping", "Show latency") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.ping) {
                        val ping = ServerUtils.currentPing
                        respond(formatResponse(
                            Component.translatable("sraddons.pc.ping.response"),
                            Component.literal("${ping}ms").withColor(getPingColor(ping))
                        ))
                    } else { respondDisabled("ping") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("tps", "Show server TPS") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.tps) {
                        val tps = ServerUtils.averageTps
                        if (tps < 0) {
                            respond(formatResponse(
                                Component.translatable("sraddons.pc.tps.response"),
                                Component.translatable("sraddons.pc.tps.updating").withColor(0xAAAAAA)
                            ))
                        } else {
                            respond(formatResponse(
                                Component.translatable("sraddons.pc.tps.response"),
                                Component.literal(tps.toFixed(1)).withColor(getTpsColor(tps))
                            ))
                        }
                    } else { respondDisabled("tps") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("fps", "Show current FPS") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.fps) {
                        val fps = ServerUtils.currentFps
                        respond(formatResponse(
                            Component.translatable("sraddons.pc.fps.response"),
                            Component.literal(fps.toString()).withColor(getFpsColor(fps))
                        ))
                    } else { respondDisabled("fps") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("time", "Show current time") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.time) {
                        val time = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
                        respond(formatResponse(
                            Component.translatable("sraddons.pc.time.response"),
                            Component.literal(time).withColor(0xFFFFFF)
                        ))
                    } else { respondDisabled("time") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("location", "Show current coordinates", "loc") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.location) {
                        val pos = getPositionString()
                        respond(formatResponse(
                            Component.translatable("sraddons.pc.coords.response"),
                            Component.literal(pos).withColor(0xFFFFFF)
                        ))
                    } else { respondDisabled("location") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("coords", "Show current coordinates", "co") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.coords) {
                        val pos = getPositionString()
                        respond(formatResponse(
                            Component.translatable("sraddons.pc.loc.response"),
                            Component.literal(pos).withColor(0xFFFFFF)
                        ))
                    } else { respondDisabled("coords") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("holding", "Show held item", "hold") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.holding) {
                        val item = mc.player?.mainHandItem?.displayName?.string ?: "Air"
                        respond(formatResponse(
                            Component.translatable("sraddons.pc.holding.response"),
                            Component.literal(item).withColor(0xFFFFFF)
                        ))
                    } else { respondDisabled("holding") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("status", "Show party status") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.status) {
                        PartyListHandler.startWaiting()
                        sendCommand("p list")
                    } else { respondDisabled("status") }
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }

    private fun buildHelpLine(command: String, descKey: String): Component {
        return Component.literal("§e$command §7- ")
            .append(Component.translatable(descKey).withColor(0xFFFFFF))
    }

    private fun showHelp() {
        rawMessage(Component.literal("§b§l===== ")
            .append(Component.translatable("sraddons.pc.help.title"))
            .append(Component.literal(" =====")))
        rawMessage(buildHelpLine("!help", "sraddons.pc.help.show_message_desc"))
        rawMessage(buildHelpLine("!warp", "sraddons.pc.help.warp_desc"))
        rawMessage(buildHelpLine("!allinvite", "sraddons.pc.help.allinvite_desc"))
        rawMessage(buildHelpLine("!pt <player>", "sraddons.pc.help.transfer_desc"))
        rawMessage(buildHelpLine("!promote <player>", "sraddons.pc.help.promote_desc"))
        rawMessage(buildHelpLine("!demote <player>", "sraddons.pc.help.demote_desc"))
        rawMessage(buildHelpLine("!kick <player>", "sraddons.pc.help.kick_desc"))
        rawMessage(buildHelpLine("!kickoffline", "sraddons.pc.help.kickoffline_desc"))
        rawMessage(buildHelpLine("!kickall [players...]", "sraddons.pc.help.kickall_desc"))
        rawMessage(buildHelpLine("!disband", "sraddons.pc.help.disband_desc"))
        rawMessage(buildHelpLine("!leave", "sraddons.pc.help.leave_desc"))
        rawMessage(buildHelpLine("!ping", "sraddons.pc.help.ping_desc"))
        rawMessage(buildHelpLine("!tps", "sraddons.pc.help.tps_desc"))
        rawMessage(buildHelpLine("!fps", "sraddons.pc.help.fps_desc"))
        rawMessage(buildHelpLine("!time", "sraddons.pc.help.time_desc"))
        rawMessage(buildHelpLine("!coords", "sraddons.pc.help.coords_desc"))
        rawMessage(buildHelpLine("!loc", "sraddons.pc.help.loc_desc"))
        rawMessage(buildHelpLine("!hold", "sraddons.pc.help.hold_desc"))
        rawMessage(buildHelpLine("!status", "sraddons.pc.help.status_desc"))
        rawMessage(buildHelpLine("!cd <time>", "sraddons.pc.help.cd_desc"))
        rawMessage(buildHelpLine("!clear", "sraddons.pc.help.clear_desc"))
        rawMessage(buildHelpLine("!fun <cf/8ball/dice/boop/random>", "sraddons.pc.help.fun_desc"))
        rawMessage(buildHelpLine("!t1-5", "sraddons.pc.help.queue_desc"))
        rawMessage(buildHelpLine("!f1-7 / m1-7", "sraddons.pc.help.queue_desc"))
        rawMessage(buildHelpLine("!note [message]", "sraddons.pc.help.note_desc"))
        rawMessage(buildHelpLine("!boop <player>", "sraddons.pc.help.boop_desc"))
        rawMessage(buildHelpLine("!invite <player>", "sraddons.pc.help.invite_desc"))
        rawMessage(buildHelpLine("!forward", "sraddons.pc.help.forward_desc"))
        rawMessage(buildHelpLine("!reload", "sraddons.pc.help.reload_desc"))
        rawMessage(buildHelpLine("!gui", "sraddons.pc.help.gui_desc"))
        rawMessage(buildHelpLine("!ver", "sraddons.pc.help.ver_desc"))
        rawMessage(Component.literal("§b§l============================"))
    }
}
