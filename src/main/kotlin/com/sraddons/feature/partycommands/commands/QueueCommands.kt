package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object QueueCommands {
    private val floorInstances = mapOf(
        "f1" to "catacombs_floor_one", "f2" to "catacombs_floor_two", "f3" to "catacombs_floor_three",
        "f4" to "catacombs_floor_four", "f5" to "catacombs_floor_five", "f6" to "catacombs_floor_six",
        "f7" to "catacombs_floor_seven", "m1" to "master_catacombs_floor_one",
        "m2" to "master_catacombs_floor_two", "m3" to "master_catacombs_floor_three",
        "m4" to "master_catacombs_floor_four", "m5" to "master_catacombs_floor_five",
        "m6" to "master_catacombs_floor_six", "m7" to "master_catacombs_floor_seven",
        "t1" to "kuudra_normal", "t2" to "kuudra_hot", "t3" to "kuudra_burning",
        "t4" to "kuudra_fiery", "t5" to "kuudra_infernal"
    )

    private fun label(key: String) = Component.translatable("sraddons.pc.label.$key")

    private fun isEnabled(cmd: String): Boolean = when (cmd) {
        "f1" -> SRConfig.settings.partyCommands.queueF1
        "f2" -> SRConfig.settings.partyCommands.queueF2
        "f3" -> SRConfig.settings.partyCommands.queueF3
        "f4" -> SRConfig.settings.partyCommands.queueF4
        "f5" -> SRConfig.settings.partyCommands.queueF5
        "f6" -> SRConfig.settings.partyCommands.queueF6
        "f7" -> SRConfig.settings.partyCommands.queueF7
        "m1" -> SRConfig.settings.partyCommands.queueM1
        "m2" -> SRConfig.settings.partyCommands.queueM2
        "m3" -> SRConfig.settings.partyCommands.queueM3
        "m4" -> SRConfig.settings.partyCommands.queueM4
        "m5" -> SRConfig.settings.partyCommands.queueM5
        "m6" -> SRConfig.settings.partyCommands.queueM6
        "m7" -> SRConfig.settings.partyCommands.queueM7
        "t1" -> SRConfig.settings.partyCommands.queueT1
        "t2" -> SRConfig.settings.partyCommands.queueT2
        "t3" -> SRConfig.settings.partyCommands.queueT3
        "t4" -> SRConfig.settings.partyCommands.queueT4
        "t5" -> SRConfig.settings.partyCommands.queueT5
        else -> true
    }

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
