package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object QueueCommands {
    private val floorInstances = FloorData.INSTANCES

    private fun label(key: String) = Component.translatable("sraddons.pc.label.$key")

    private fun isEnabled(cmd: String): Boolean = SRConfig.isCommandEnabled(cmd)

    fun register() {
        floorInstances.keys.forEach { cmd ->
            Commands.add(object : Command(cmd, "Queue $cmd") {
                override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                    builder.then(Command.argument("seconds", StringArgumentType.word())
                        .executes { ctx ->
                            if (isEnabled(cmd)) {
                                val seconds = StringArgumentType.getString(ctx, "seconds").toIntOrNull()
                                if (seconds != null && seconds > 0) {
                                    CountdownManager.startCountdown(minOf(seconds, 300), cmd.uppercase())
                                } else { respond(formatResponse(label("error"), Component.translatable("sraddons.pc.queue.invalid_time").withColor(0xFF5555))) }
                            } else { respondDisabled(cmd) }
                            Command.SINGLE_SUCCESS
                        })
                    builder.executes {
                        if (isEnabled(cmd)) {
                            val instance = floorInstances[cmd]!!
                            respond(formatResponse(Component.translatable("sraddons.pc.queue.label"), Component.literal("§e${cmd.uppercase()}")))
                            sendCommand("joininstance $instance")
                        } else { respondDisabled(cmd) }
                        Command.SINGLE_SUCCESS
                    }
                }
            })
        }
    }
}
