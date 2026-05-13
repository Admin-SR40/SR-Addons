package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object UtilityCommands {
    private fun label(key: String) = Component.translatable("sraddons.pc.label.$key")

    fun register() {
        Commands.add(object : Command("forward", "Toggle party chat forwarding") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    SRConfig.settings.partyCommands.respondInPartyChat = !SRConfig.settings.partyCommands.respondInPartyChat
                    SRConfig.save()
                    Commands.rebuildDispatcher()
                    val statusKey = if (SRConfig.settings.partyCommands.respondInPartyChat) "sraddons.pc.forward.on" else "sraddons.pc.forward.off"
                    val statusColor = if (SRConfig.settings.partyCommands.respondInPartyChat) 0x55FF55 else 0xFF5555
                    respond(formatResponse(
                        Component.literal("Forward"),
                        Component.translatable(statusKey).withColor(statusColor)
                    ))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("reload", "Reload config") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    SRConfig.load()
                    Commands.rebuildDispatcher()
                    respond(formatResponse(
                        Component.literal("Config"),
                        Component.translatable("sraddons.pc.reload.done").withColor(0x55FF55)
                    ))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("cd", "Start countdown", "countdown") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("time", StringArgumentType.word())
                    .executes { ctx ->
                        if (SRConfig.isCommandEnabled("countdown")) {
                            val timeInput = StringArgumentType.getString(ctx, "time")
                            val seconds = CountdownManager.parseTime(timeInput)
                            if (seconds != null) {
                                CountdownManager.startCountdown(seconds, "Custom")
                            } else {
                                respond(formatResponse(label("error"), Component.translatable("sraddons.pc.error.invalid_time").withColor(0xFF5555)))
                            }
                        } else {
                            respondDisabled("cd")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (SRConfig.isCommandEnabled("countdown")) {
                        val currentCountdown = CountdownManager.getCurrentCountdown()
                        if (currentCountdown != null) {
                            val remaining = currentCountdown.remainingSeconds
                            val timeStr = CountdownManager.formatTime(remaining)
                            val label = if (currentCountdown.label == "Custom") "" else " (${currentCountdown.label})"
                            respond(formatResponse(
                                Component.literal("Countdown"),
                                Component.translatable("sraddons.pc.cd.remaining", Component.literal("$timeStr$label")).withColor(0xFFFF55)
                            ))
                        } else {
                            respond(formatResponse(
                                Component.literal("Countdown"),
                                Component.translatable("sraddons.pc.cd.no_active").withColor(0xAAAAAA)
                            ))
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
                    if (SRConfig.isCommandEnabled("countdown")) {
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
                        respond(formatResponse(
                            Component.literal("Note"),
                            Component.translatable("sraddons.pc.note.saved", Component.literal(message)).withColor(0x55FF55)
                        ))
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    val note = SRConfig.settings.partyCommands.note.noControlCodes
                    if (note.isNotEmpty()) {
                        if (PartyUtils.isInParty) {
                            sendPartyChat(note)
                            modMessage(formatResponse(
                                Component.literal("Note"),
                                Component.translatable("sraddons.pc.note.sent").withColor(0x55FF55)
                            ))
                        } else {
                            modMessage(formatResponse(
                                Component.literal("Note"),
                                Component.translatable("sraddons.pc.note.not_in_party").withColor(0xFF5555)
                            ))
                        }
                    } else {
                        respond(formatResponse(
                            Component.literal("Note"),
                            Component.translatable("sraddons.pc.note.not_set").withColor(0xFF5555)
                        ))
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

    }
}
