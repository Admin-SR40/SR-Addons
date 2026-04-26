package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.commands.SharedSuggestionProvider

abstract class Command(val name: String, val description: String, vararg val aliases: String) {
    companion object {
        const val SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS

        fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<SharedSuggestionProvider, T> {
            return RequiredArgumentBuilder.argument(name, type)
        }

        fun literal(name: String): LiteralArgumentBuilder<SharedSuggestionProvider> {
            return LiteralArgumentBuilder.literal(name)
        }
    }

    fun registerTo(dispatcher: CommandDispatcher<SharedSuggestionProvider>) {
        register(dispatcher, name)
        aliases.forEach { register(dispatcher, it) }
    }

    private fun register(dispatcher: CommandDispatcher<SharedSuggestionProvider>, name: String) {
        val builder = LiteralArgumentBuilder.literal<SharedSuggestionProvider>(name)
        build(builder)
        dispatcher.register(builder)
    }

    abstract fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>)
}
