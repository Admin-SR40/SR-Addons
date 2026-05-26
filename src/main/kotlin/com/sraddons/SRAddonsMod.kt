package com.sraddons

import com.sraddons.command.SRACommand
import com.sraddons.config.SRConfig
import com.sraddons.feature.carry.CarryCommand
import com.sraddons.feature.carry.CarryHighlightRenderer
import com.sraddons.feature.carry.CarryState
import com.sraddons.feature.partycommands.commands.PartyCommandHandler
import com.sraddons.feature.partycommands.utils.AutoPartyListUpdater
import com.sraddons.feature.partycommands.utils.ChatListener
import com.sraddons.feature.partycommands.utils.CommandKeyBinding
import com.sraddons.feature.helper.PingTpsAlertNotifier
import com.sraddons.feature.helper.RagnarockNotifier
import com.sraddons.feature.helper.TextReplacer
import com.sraddons.feature.starredmob.renderer.StarredMobRenderer
import com.sraddons.update.UpdateChecker
import com.sraddons.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.net.URI

class SRAddonsMod : ClientModInitializer {

    @Volatile
    private var updateResult: UpdateChecker.UpdateResult? = null
    private var notificationShown = false

    override fun onInitializeClient() {
        SRConfig.load()
        CarryState.loadHistory()
        CarryState.loadData()
        SRACommand.register()
        CarryCommand.register()
        CarryHighlightRenderer.init()
        StarredMobRenderer.init()
        PartyCommandHandler.init()
        ChatListener.init()
        AutoPartyListUpdater.init()
        CommandKeyBinding.init()
        RagnarockNotifier.init()
        PingTpsAlertNotifier.init()
        TextReplacer.init()

        if (SRConfig.settings.general.autoCheckUpdates) {
            startAutoUpdateCheck()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            showUpdateNotificationIfNeeded()
        }
    }

    private fun startAutoUpdateCheck() {
        CoroutineScope(Dispatchers.IO).launch {
            updateResult = UpdateChecker.check()
        }
    }

    private fun showUpdateNotificationIfNeeded() {
        if (notificationShown) return
        notificationShown = true
        val result = updateResult ?: return
        if (result.downloadUrl == null) return

        val mc = net.minecraft.client.Minecraft.getInstance()
        val prefix = Constants.makePrefix()
        val clickStyle = Style.EMPTY
            .withUnderlined(true)
            .withClickEvent(ClickEvent.OpenUrl(URI.create(result.downloadUrl)))

        mc.execute {
            mc.gui?.chat?.addMessage(
                prefix.copy()
                    .append(Component.translatable("sraddons.command.update.available", result.latestVersion, Constants.MOD_VERSION).withColor(0x55FF55))
            )
            mc.gui?.chat?.addMessage(
                prefix.copy()
                    .append(Component.translatable("sraddons.command.update.click").withColor(0xFFFFFF))
                    .append(Component.translatable("sraddons.command.update.here").withColor(0x55FFFF).withStyle(clickStyle))
                    .append(Component.translatable("sraddons.command.update.check_out").withColor(0xFFFFFF))
            )
        }
    }
}
