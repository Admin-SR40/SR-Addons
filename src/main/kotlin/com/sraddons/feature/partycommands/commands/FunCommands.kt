package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.commands.SharedSuggestionProvider
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
                        if (SRConfig.settings.partyCommands.coinflip) {
                            val result = if (Random.nextBoolean()) "\u00a76heads" else "\u00a7ftails"
                            respond(formatResponse("Coinflip", result, ""))
                        } else { respondDisabled("fun cf") }
                        Command.SINGLE_SUCCESS
                    })

                builder.then(Command.literal("8ball")
                    .executes {
                        if (SRConfig.settings.partyCommands.eightball) {
                            respond(formatResponse("8-Ball", eightBallResponses.random(), "\u00a7d"))
                        } else { respondDisabled("fun 8ball") }
                        Command.SINGLE_SUCCESS
                    })

                builder.then(Command.literal("dice")
                    .executes {
                        if (SRConfig.settings.partyCommands.dice) {
                            val roll = (1..6).random()
                            val color = when (roll) { 6 -> "\u00a72"; 5 -> "\u00a7a"; 4 -> "\u00a7e"; 3 -> "\u00a76"; else -> "\u00a7c" }
                            respond(formatResponse("Dice Roll", roll.toString(), color))
                        } else { respondDisabled("fun dice") }
                        Command.SINGLE_SUCCESS
                    })

                builder.then(Command.literal("boop")
                    .then(Command.argument("player", StringArgumentType.word())
                        .executes { ctx ->
                            if (SRConfig.settings.partyCommands.boop) {
                                val target = StringArgumentType.getString(ctx, "player")
                                sendCommand("boop $target")
                                respond(formatResponse("Boop", "\u00a7aBooped $target", ""))
                            } else { respondDisabled("fun boop") }
                            Command.SINGLE_SUCCESS
                        })
                    .executes {
                        respond(formatResponse("Usage", "\u00a7c!fun boop <player>", ""))
                        Command.SINGLE_SUCCESS
                    })

                builder.then(Command.literal("random")
                    .then(Command.argument("min", StringArgumentType.word())
                        .then(Command.argument("max", StringArgumentType.word())
                            .executes { ctx ->
                                val minStr = StringArgumentType.getString(ctx, "min")
                                val maxStr = StringArgumentType.getString(ctx, "max")
                                val min = minStr.toIntOrNull()
                                val max = maxStr.toIntOrNull()
                                if (min == null || max == null) {
                                    respond(formatResponse("Error", "\u00a7cInvalid numbers! Usage: !fun random <min> <max>", ""))
                                } else {
                                    val actualMin = minOf(min, max)
                                    val actualMax = maxOf(min, max)
                                    val result = (actualMin..actualMax).random()
                                    respond("\u00a7bRandom Value: \u00a7e$result \u00a77[\u00a7f$actualMin\u00a77-\u00a7f$actualMax\u00a77]")
                                }
                                Command.SINGLE_SUCCESS
                            })
                        .executes { ctx ->
                            val maxStr = StringArgumentType.getString(ctx, "min")
                            val max = maxStr.toIntOrNull()
                            if (max == null || max < 1) {
                                respond(formatResponse("Error", "\u00a7cInvalid number! Usage: !fun random <max>", ""))
                            } else {
                                val result = (1..max).random()
                                respond("\u00a7bRandom Value: \u00a7e$result \u00a77[\u00a7f1\u00a77-\u00a7f$max\u00a77]")
                            }
                            Command.SINGLE_SUCCESS
                        })
                    .executes {
                        val result = (1..100).random()
                        respond("\u00a7bRandom Value: \u00a7e$result \u00a77[\u00a7f1\u00a77-\u00a7f100\u00a77]")
                        Command.SINGLE_SUCCESS
                    })

                builder.executes {
                    respond(formatResponse("Fun Commands", "\u00a7ecf, 8ball, dice, boop <player>, random [min] [max]", ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }
}
