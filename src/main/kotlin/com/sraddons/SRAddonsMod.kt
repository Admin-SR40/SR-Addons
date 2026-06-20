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
import com.sraddons.feature.hud.HudElementHider
import com.sraddons.feature.helper.ChatKeywordAlert
import com.sraddons.feature.helper.PingTpsAlertNotifier
import com.sraddons.feature.helper.RagnarockNotifier
import com.sraddons.feature.helper.TextReplacer
import com.sraddons.feature.starredmob.renderer.StarredMobRenderer
import com.sraddons.update.UpdateChecker
import com.sraddons.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
    private val logger = org.apache.logging.log4j.LogManager.getLogger("SR-Addons")

    private fun safeInit(name: String, block: () -> Unit) {
        try { block() } catch (e: Exception) { logger.error("Failed to init $name", e) }
    }

    override fun onInitializeClient() {
        safeInit("config") { SRConfig.load() }
        safeInit("carry history") { CarryState.loadHistory() }
        safeInit("carry data") { CarryState.loadData() }
        safeInit("/sra commands") { SRACommand.register() }
        safeInit("/cm commands") { CarryCommand.register() }
        safeInit("carry renderer") { CarryHighlightRenderer.init() }
        safeInit("starred mob renderer") { StarredMobRenderer.init() }
        safeInit("party commands") { PartyCommandHandler.init() }
        safeInit("chat listener") { ChatListener.init() }
        safeInit("party list updater") { AutoPartyListUpdater.init() }
        safeInit("key bindings") { CommandKeyBinding.init() }
        safeInit("ragnarock notifier") { RagnarockNotifier.init() }
        safeInit("alert notifier") { PingTpsAlertNotifier.init() }
        safeInit("text replacer") { TextReplacer.init() }
        safeInit("chat alert") { ChatKeywordAlert.init() }
        safeInit("hud element hider") { HudElementHider.init() }

        if (SRConfig.settings.general.autoCheckUpdates) {
            startAutoUpdateCheck()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            showUpdateNotificationIfNeeded()
        }
    }

    private fun startAutoUpdateCheck() {
        Thread.startVirtualThread {
            runBlocking(Dispatchers.IO) {
                updateResult = UpdateChecker.check()
            }
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
            mc.gui.chat.addClientSystemMessage(
                prefix.copy()
                    .append(Component.translatable("sraddons.command.update.available", result.latestVersion, Constants.MOD_VERSION).withColor(0x55FF55))
            )
            mc.gui.chat.addClientSystemMessage(
                prefix.copy()
                    .append(Component.translatable("sraddons.command.update.click").withColor(0xFFFFFF))
                    .append(Component.translatable("sraddons.command.update.here").withColor(0x55FFFF).withStyle(clickStyle))
                    .append(Component.translatable("sraddons.command.update.check_out").withColor(0xFFFFFF))
            )
        }
    }
}
