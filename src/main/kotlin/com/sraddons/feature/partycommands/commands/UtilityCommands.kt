package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.gui.SRConfigGui
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.commands.SharedSuggestionProvider

object UtilityCommands {
    fun register() {
        Commands.add(object : Command("forward", "Toggle party chat forwarding") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    SRConfig.settings.partyCommands.respondInPartyChat = !SRConfig.settings.partyCommands.respondInPartyChat
                    SRConfig.save()
                    Commands.rebuildDispatcher()
                    val status = if (SRConfig.settings.partyCommands.respondInPartyChat) "\u00a7aON" else "\u00a7cOFF"
                    respond(formatResponse("Forward", status, ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("reload", "Reload config") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    SRConfig.load()
                    Commands.rebuildDispatcher()
                    respond(formatResponse("Config", "\u00a7aReloaded", ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("cd", "Start countdown", "countdown") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("time", StringArgumentType.word())
                    .executes { ctx ->
                        if (SRConfig.settings.partyCommands.countdown) {
                            val timeInput = StringArgumentType.getString(ctx, "time")
                            val seconds = CountdownManager.parseTime(timeInput)
                            if (seconds != null) {
                                CountdownManager.startCountdown(seconds, "Custom")
                            } else {
                                respond(formatResponse("Error", "\u00a7cInvalid time! Use: 60, 5m, 1h, 5m30s (max 12h)", ""))
                            }
                        } else {
                            respondDisabled("cd")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (SRConfig.settings.partyCommands.countdown) {
                        val currentCountdown = CountdownManager.getCurrentCountdown()
                        if (currentCountdown != null) {
                            val remaining = currentCountdown.remainingSeconds
                            val timeStr = CountdownManager.formatTime(remaining)
                            val label = if (currentCountdown.label == "Custom") "" else " (${currentCountdown.label})"
                            respond(formatResponse("Countdown", "\u00a7e$timeStr \u00a77remaining$label", ""))
                        } else {
                            respond(formatResponse("Countdown", "\u00a77No active countdown", ""))
                        }
                    } else {
                        respondDisabled("cd")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("clear", "Clear countdown") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.countdown) {
                        CountdownManager.clearCountdown()
                    } else {
                        respondDisabled("clear")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("note", "Send saved note to party") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("message", StringArgumentType.greedyString())
                    .executes { ctx ->
                        val message = StringArgumentType.getString(ctx, "message")
                        SRConfig.settings.partyCommands.note = message
                        SRConfig.save()
                        respond(formatResponse("Note", "\u00a7aSaved: \u00a7f$message", ""))
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    val note = SRConfig.settings.partyCommands.note.noControlCodes
                    if (note.isNotEmpty()) {
                        if (PartyUtils.isInParty) {
                            sendPartyChat(note)
                            modMessage(formatResponse("Note", "\u00a7aSent to party", ""))
                        } else {
                            modMessage(formatResponse("Note", "\u00a7cYou are not in a party!", ""))
                        }
                    } else {
                        respond(formatResponse("Note", "\u00a7cNo note saved. Use !note <message> to set one.", ""))
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("gui", "Open config GUI") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    SRConfigGui.open()
                    modMessage(formatResponse("GUI", "\u00a7aOpening config GUI...", ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("ver", "Show version info", "version") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    rawMessage("\u00a7b\u00a7l===== SR-Addons =====")
                    rawMessage("\u00a7eVersion: \u00a7a1.0.0")
                    rawMessage("\u00a7eFeatures: \u00a7aEntityFire, PartyCommands, StarredMob")
                    rawMessage("\u00a7eAuthor: \u00a7aAdmin_SR40")
                    rawMessage("\u00a7eGitHub: \u00a7aAdmin-SR40/SR-Addons")
                    rawMessage("\u00a7b\u00a7l=======================")
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }
}
