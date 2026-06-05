package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import kotlin.random.Random

object FunCommands {
    private val eightBallResponses = arrayOf(
        "It is certain", "It is decidedly so", "Without a doubt",
        "Yes definitely", "You may rely on it", "As I see it, yes",
        "Most likely", "Outlook good", "Yes", "Signs point to yes",
        "Reply hazy try again", "Ask again later", "Better not tell you now",
        "Cannot predict now", "Concentrate and ask again", "Don't count on it",
        "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful"
    )

    fun register() {
        Commands.add(object : Command("fun", "Fun commands") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.literal("cf")
                    .executes {
                        if (SRConfig.isCommandEnabled("coinflip")) {
                            val result = if (Random.nextBoolean()) "heads" else "tails"
                            val color = if (Random.nextBoolean()) 0xFFAA00 else 0xFFFFFF
                            respond(formatResponse(
                                Component.translatable("sraddons.pc.fun.coinflip.label"),
                                Component.literal(result).withColor(color)
                            ))
                        } else { respondDisabled("fun cf") }
                        Command.SINGLE_SUCCESS
                    })
                builder.then(Command.literal("8ball")
                    .executes {
                        if (SRConfig.isCommandEnabled("eightball")) {
                            respond(formatResponse(
                                Component.translatable("sraddons.pc.fun.eightball.label"),
                                Component.literal(eightBallResponses.random()).withColor(0xFF55FF)
                            ))
                        } else { respondDisabled("fun 8ball") }
                        Command.SINGLE_SUCCESS
                    })
                builder.then(Command.literal("dice")
                    .executes {
                        if (SRConfig.isCommandEnabled("dice")) {
                            val roll = (1..6).random()
                            val color = when (roll) { 6 -> 0x00AA00; 5 -> 0x55FF55; 4 -> 0xFFFF55; 3 -> 0xFFAA00; else -> 0xFF5555 }
                            respond(formatResponse(
                                Component.translatable("sraddons.pc.fun.dice.label"),
                                Component.literal(roll.toString()).withColor(color)
                            ))
                        } else { respondDisabled("fun dice") }
                        Command.SINGLE_SUCCESS
                    })
                builder.then(Command.literal("boop")
                    .then(Command.argument("player", StringArgumentType.word())
                        .executes { ctx ->
                            if (SRConfig.isCommandEnabled("boop")) {
                                val target = StringArgumentType.getString(ctx, "player")
                                sendCommand("boop $target")
                                respond(formatResponse(
                                    Component.literal("Boop"),
                                    Component.translatable("sraddons.pc.fun.boop.success", Component.literal(target)).withColor(0x55FF55)
                                ))
                            } else { respondDisabled("fun boop") }
                            Command.SINGLE_SUCCESS
                        })
                    .executes {
                        respond(formatResponse(label("usage"), Component.literal("§c!fun boop <player>")))
                        Command.SINGLE_SUCCESS
                    })
                builder.then(Command.literal("random")
                    .then(Command.argument("min", StringArgumentType.word())
                        .then(Command.argument("max", StringArgumentType.word())
                            .executes { ctx ->
                                if (SRConfig.isCommandEnabled("random")) {
                                    val minStr = StringArgumentType.getString(ctx, "min")
                                    val maxStr = StringArgumentType.getString(ctx, "max")
                                    val min = minStr.toIntOrNull()
                                    val max = maxStr.toIntOrNull()
                                    if (min == null || max == null) {
                                        respond(formatResponse(label("error"), Component.translatable("sraddons.pc.fun.random.error_range").withColor(0xFF5555)))
                                    } else {
                                        val actualMin = minOf(min, max)
                                        val actualMax = maxOf(min, max)
                                        val result = (actualMin..actualMax).random()
                                        respond(buildRandomResult(result, actualMin, actualMax))
                                    }
                                } else { respondDisabled("fun random") }
                                Command.SINGLE_SUCCESS
                            })
                        .executes { ctx ->
                            if (SRConfig.isCommandEnabled("random")) {
                                val maxStr = StringArgumentType.getString(ctx, "min")
                                val max = maxStr.toIntOrNull()
                                if (max == null || max < 1) {
                                    respond(formatResponse(label("error"), Component.translatable("sraddons.pc.fun.random.error_single").withColor(0xFF5555)))
                                } else {
                                    val result = (1..max).random()
                                    respond(buildRandomResult(result, 1, max))
                                }
                            } else { respondDisabled("fun random") }
                            Command.SINGLE_SUCCESS
                        })
                    .executes {
                        if (SRConfig.isCommandEnabled("random")) {
                            val result = (1..100).random()
                            respond(buildRandomResult(result, 1, 100))
                        } else { respondDisabled("fun random") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(Component.translatable("sraddons.pc.fun.help").withColor(0xFFFF55))
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }

    private fun label(key: String) = Component.translatable("sraddons.pc.label.$key")
    private fun buildRandomResult(result: Int, min: Int, max: Int): Component {
        return Component.translatable("sraddons.pc.fun.random.result",
            Component.literal(result.toString()).withColor(0xFFFF55),
            Component.literal(min.toString()).withColor(0xFFFFFF),
            Component.literal(max.toString()).withColor(0xFFFFFF)
        ).withColor(0x55FFFF)
    }
}
