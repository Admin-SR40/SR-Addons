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
import com.sraddons.feature.helper.ChatKeywordAlert
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
    private val logger = org.apache.logging.log4j.LogManager.getLogger("SR-Addons")

    override fun onInitializeClient() {
        try { SRConfig.load() } catch (e: Exception) { logger.error("Failed to load config", e) }
        try { CarryState.loadHistory() } catch (e: Exception) { logger.error("Failed to load carry history", e) }
        try { CarryState.loadData() } catch (e: Exception) { logger.error("Failed to load carry data", e) }
        try { SRACommand.register() } catch (e: Exception) { logger.error("Failed to register /sra commands", e) }
        try { CarryCommand.register() } catch (e: Exception) { logger.error("Failed to register /cm commands", e) }
        try { CarryHighlightRenderer.init() } catch (e: Exception) { logger.error("Failed to init carry renderer", e) }
        try { StarredMobRenderer.init() } catch (e: Exception) { logger.error("Failed to init starred mob renderer", e) }
        try { PartyCommandHandler.init() } catch (e: Exception) { logger.error("Failed to init party commands", e) }
        try { ChatListener.init() } catch (e: Exception) { logger.error("Failed to init chat listener", e) }
        try { AutoPartyListUpdater.init() } catch (e: Exception) { logger.error("Failed to init party list updater", e) }
        try { CommandKeyBinding.init() } catch (e: Exception) { logger.error("Failed to init key bindings", e) }
        try { RagnarockNotifier.init() } catch (e: Exception) { logger.error("Failed to init ragnarock notifier", e) }
        try { PingTpsAlertNotifier.init() } catch (e: Exception) { logger.error("Failed to init alert notifier", e) }
        try { TextReplacer.init() } catch (e: Exception) { logger.error("Failed to init text replacer", e) }
        try { ChatKeywordAlert.init() } catch (e: Exception) { logger.error("Failed to init chat alert", e) }

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
