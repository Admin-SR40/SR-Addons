package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider

object Commands {
    private val commandList = mutableListOf<Command>()
    val commands: List<Command> get() = commandList
    private val mc = Minecraft.getInstance()

    @Volatile
    @JvmField
    var DISPATCHER = CommandDispatcher<SharedSuggestionProvider>()

    fun add(command: Command) {
        synchronized(this) {
            commandList.removeAll { it.name == command.name }
            commandList.add(command)
        }
    }

    @JvmStatic
    @Throws(CommandSyntaxException::class)
    fun dispatch(message: String) {
        val source = mc.player?.connection?.suggestionsProvider ?: return
        DISPATCHER.execute(message, source)
    }

    fun rebuildDispatcher() {
        synchronized(this) {
            DISPATCHER = CommandDispatcher()
            commandList.forEach { it.registerTo(DISPATCHER) }
        }
    }
}
