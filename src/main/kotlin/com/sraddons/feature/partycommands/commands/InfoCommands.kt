package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
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
                        val color = getPingColor(ping)
                        respond(formatResponse("Current Ping", "${ping}ms", color))
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
                            respond(formatResponse("Current TPS", "\u00a77Updating TPS, please wait...", ""))
                        } else {
                            val color = getTpsColor(tps)
                            respond(formatResponse("Current TPS", tps.toFixed(1), color))
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
                        val color = getFpsColor(fps)
                        respond(formatResponse("Current FPS", fps.toString(), color))
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
                        respond(formatResponse("Current Time", time, "\u00a7f"))
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
                        respond(formatResponse("Coordinates", pos, "\u00a7f"))
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
                        respond(formatResponse("Current Coordinates", pos, "\u00a7f"))
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
                        respond(formatResponse("Holding", item, "\u00a7f"))
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

    private fun showHelp() {
        rawMessage("\u00a7b\u00a7l===== Available Commands =====")
        rawMessage("\u00a7e!help \u00a77- Show this message")
        rawMessage("\u00a7e!warp \u00a77- Warp members to this hub")
        rawMessage("\u00a7e!allinvite \u00a77- Enable all invite")
        rawMessage("\u00a7e!pt <player> \u00a77- Transfer party leader")
        rawMessage("\u00a7e!promote <player> \u00a77- Promote member")
        rawMessage("\u00a7e!demote <player> \u00a77- Demote member")
        rawMessage("\u00a7e!kick <player> \u00a77- Kick member from party")
        rawMessage("\u00a7e!kickoffline \u00a77- Kick offline members")
        rawMessage("\u00a7e!kickall [players...] \u00a77- Kick all members except specified")
        rawMessage("\u00a7e!disband \u00a77- Disband the party")
        rawMessage("\u00a7e!leave \u00a77- Leave the party")
        rawMessage("\u00a7e!ping \u00a77- Show latency")
        rawMessage("\u00a7e!tps \u00a77- Show TPS")
        rawMessage("\u00a7e!fps \u00a77- Show FPS")
        rawMessage("\u00a7e!time \u00a77- Show current time")
        rawMessage("\u00a7e!coords \u00a77- Show coordinates")
        rawMessage("\u00a7e!loc \u00a77- Show location")
        rawMessage("\u00a7e!hold \u00a77- Show held item")
        rawMessage("\u00a7e!status \u00a77- Show party status")
        rawMessage("\u00a7e!cd <time> \u00a77- Countdown (60, 5m, 1h)")
        rawMessage("\u00a7e!clear \u00a77- Clear countdown")
        rawMessage("\u00a7e!fun <cf/8ball/dice/boop/random> \u00a77- Fun commands")
        rawMessage("\u00a7e!t1-5 \u00a77- Kuudra")
        rawMessage("\u00a7e!f1-7 / m1-7 \u00a77- Dungeon")
        rawMessage("\u00a7e!note [message] \u00a77- Send/set note to party")
        rawMessage("\u00a7e!boop <player> \u00a77- Boop a player")
        rawMessage("\u00a7e!invite <player> \u00a77- Invite player to party")
        rawMessage("\u00a7e!forward \u00a77- Toggle party chat forwarding")
        rawMessage("\u00a7e!reload \u00a77- Reload config")
        rawMessage("\u00a7e!gui \u00a77- Open config GUI")
        rawMessage("\u00a7e!ver \u00a77- Show version info")
        rawMessage("\u00a7b\u00a7l============================")
    }
}
