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

    fun register() {
        floorInstances.keys.forEach { cmd ->
            Commands.add(object : Command(cmd, "Queue $cmd") {
                override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                    builder.then(Command.argument("seconds", StringArgumentType.word())
                        .executes { ctx ->
                            if (SRConfig.settings.partyCommands.queueInstance) {
                                val seconds = StringArgumentType.getString(ctx, "seconds").toIntOrNull()
                                if (seconds != null && seconds > 0) {
                                    CountdownManager.startCountdown(minOf(seconds, 300), cmd.uppercase())
                                } else { respond(formatResponse(label("error"), Component.translatable("sraddons.pc.queue.invalid_time").withColor(0xFF5555))) }
                            } else { respondDisabled(cmd) }
                            Command.SINGLE_SUCCESS
                        })
                    builder.executes {
                        if (SRConfig.settings.partyCommands.queueInstance) {
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
