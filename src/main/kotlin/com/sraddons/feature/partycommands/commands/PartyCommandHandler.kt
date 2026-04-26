package com.sraddons.feature.partycommands.commands

import com.sraddons.feature.partycommands.utils.CountdownManager
import com.sraddons.feature.partycommands.utils.PartyListHandler
import com.sraddons.feature.partycommands.utils.ServerUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object PartyCommandHandler {

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            ServerUtils.updateTps()
            PartyListHandler.onTick()
            CountdownManager.onTick()
        }
        registerCommands()
        Commands.rebuildDispatcher()
    }

    private fun registerCommands() {
        InfoCommands.register()
        FunCommands.register()
        PartyManagementCommands.register()
        QueueCommands.register()
        UtilityCommands.register()
    }
}
