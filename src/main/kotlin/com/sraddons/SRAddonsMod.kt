package com.sraddons

import com.sraddons.command.SRACommand
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.commands.Commands
import com.sraddons.feature.partycommands.commands.PartyCommandHandler
import com.sraddons.feature.partycommands.utils.AutoPartyListUpdater
import com.sraddons.feature.partycommands.utils.ChatListener
import com.sraddons.feature.partycommands.utils.CommandKeyBinding
import com.sraddons.feature.starredmob.renderer.StarredMobRenderer
import net.fabricmc.api.ClientModInitializer

class SRAddonsMod : ClientModInitializer {

    override fun onInitializeClient() {
        SRConfig.load()
        SRACommand.register()
        StarredMobRenderer.init()
        Commands.init()
        PartyCommandHandler.init()
        ChatListener.init()
        AutoPartyListUpdater.init()
        CommandKeyBinding.init()
    }
}
