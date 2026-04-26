package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider

object Commands {
    val COMMANDS = mutableListOf<Command>()
    @JvmField
    var DISPATCHER = CommandDispatcher<SharedSuggestionProvider>()
    private val mc = Minecraft.getInstance()

    fun init() {}

    fun add(command: Command) {
        COMMANDS.removeAll { it.name == command.name }
        COMMANDS.add(command)
    }

    @JvmStatic
    @Throws(CommandSyntaxException::class)
    fun dispatch(message: String) {
        val source = mc.player?.connection?.suggestionsProvider ?: return
        DISPATCHER.execute(message, source)
    }

    fun rebuildDispatcher() {
        DISPATCHER = CommandDispatcher()
        COMMANDS.forEach { it.registerTo(DISPATCHER) }
    }
}
