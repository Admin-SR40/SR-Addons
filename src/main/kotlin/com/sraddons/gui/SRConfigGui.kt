package com.sraddons.gui

import com.sraddons.config.SRConfig
import com.sraddons.config.toColor
import com.sraddons.util.Scheduler
import dev.isxander.yacl3.api.Binding
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.awt.Color

object SRConfigGui {

    private const val GUI_OPEN_DELAY_MS = 50L

    // --- Option builder helpers ---

    private fun guiKey(key: String) = "sraddons.gui.$key"
    private val RENDER_MODES = listOf("OUTLINE", "FILL", "BOTH")

    private fun boolOpt(key: String, default: Boolean, get: () -> Boolean, set: (Boolean) -> Unit) =
        Option.createBuilder<Boolean>()
            .name(Component.translatable(guiKey(key)))
            .description(OptionDescription.of(Component.translatable(guiKey("$key.desc"))))
            .binding(default, get, set)
            .controller(TickBoxControllerBuilder::create)
            .build()

    private fun strOpt(key: String, default: String, get: () -> String, set: (String) -> Unit) =
        Option.createBuilder<String>()
            .name(Component.translatable(guiKey(key)))
            .description(OptionDescription.of(Component.translatable(guiKey("$key.desc"))))
            .binding(default, get, set)
            .controller(StringControllerBuilder::create)
            .build()

    private fun intOpt(key: String, default: Int, min: Int, max: Int, step: Int, get: () -> Int, set: (Int) -> Unit) =
        Option.createBuilder<Int>()
            .name(Component.translatable(guiKey(key)))
            .description(OptionDescription.of(Component.translatable(guiKey("$key.desc"))))
            .binding(default, get, set)
            .controller { opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(step) }
            .build()

    private fun colorOpt(key: String, defaultR: Int, defaultG: Int, defaultB: Int, defaultA: Int,
                         get: () -> Color, set: (Int, Int, Int, Int) -> Unit) =
        Option.createBuilder<Color>()
            .name(Component.translatable(guiKey(key)))
            .description(OptionDescription.of(Component.translatable(guiKey("$key.desc"))))
            .binding(Color(defaultR, defaultG, defaultB, defaultA), get, { c -> set(c.red, c.green, c.blue, c.alpha) })
            .controller(ColorControllerBuilder::create)
            .build()

    private fun dropDownOpt(key: String, default: String, values: List<String>, get: () -> String, set: (String) -> Unit) =
        Option.createBuilder<String>()
            .name(Component.translatable(guiKey(key)))
            .description(OptionDescription.of(Component.translatable(guiKey("$key.desc"))))
            .binding(default, get, set)
            .controller { opt -> DropdownStringControllerBuilder.create(opt).allowAnyValue(false).values(values) }
            .build()

    // --- Screen creation ---

    fun createScreen(parent: Screen?): Screen {
        return YetAnotherConfigLib.createBuilder()
            .title(Component.translatable(guiKey("title")))
            .save { SRConfig.save() }
            .category(createGeneralCategory())
            .category(createPartyCommandsCategory())
            .category(createStarredMobCategory())
            .category(createCarryCategory())
            .category(createAlertsCategory())
            .build()
            .generateScreen(parent)
    }

    // ========== General Category ==========

    private fun createGeneralCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable(guiKey("general")))
            .tooltip(Component.translatable(guiKey("general.desc")))
            .group(createDisplayGroup())
            .group(createVisualTweaksGroup())
            .group(createTextGroup())
            .group(createQuickToolsGroup())
            .build()
    }

    private fun createDisplayGroup(): OptionGroup {
        val g = SRConfig.settings.general
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("general.group.display")))
            .description(OptionDescription.of(Component.translatable(guiKey("general.group.display.desc"))))
            .collapsed(false)
            .option(boolOpt("general.show_own_name", true, { g.showOwnNameInThirdPerson }, { g.showOwnNameInThirdPerson = it }))
            .option(boolOpt("general.remove_separator", true, { g.removeSeparator }, { g.removeSeparator = it }))
            .option(boolOpt("general.auto_check_updates", false, { g.autoCheckUpdates }, { g.autoCheckUpdates = it }))
            .build()
    }

    private fun createVisualTweaksGroup(): OptionGroup {
        val g = SRConfig.settings.general
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("general.visual_tweaks")))
            .description(OptionDescription.of(Component.translatable(guiKey("general.visual_tweaks.desc"))))
            .collapsed(true)
            .option(boolOpt("general.hide_armor_bar", false, { g.hideArmorBar }, { g.hideArmorBar = it }))
            .option(boolOpt("general.hide_hunger_bar", false, { g.hideHungerBar }, { g.hideHungerBar = it }))
            .option(boolOpt("general.hide_entity_fire", false, { g.hideEntityFire }, { g.hideEntityFire = it }))
            .option(boolOpt("general.fullbright", false, { g.fullbright }, { g.fullbright = it }))
            .option(boolOpt("general.better_fov", false, { g.betterFov }, { g.betterFov = it }))
            .build()
    }

    private fun createTextGroup(): OptionGroup {
        val g = SRConfig.settings.general
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("general.text")))
            .description(OptionDescription.of(Component.translatable(guiKey("general.text.desc"))))
            .collapsed(true)
            .option(boolOpt("general.replace_texts_enabled", false, { g.replaceTextsEnabled }, { g.replaceTextsEnabled = it }))
            .option(boolOpt("general.highlight_dev_name", true, { g.highlightDevName }, { g.highlightDevName = it }))
            .build()
    }

    private fun createQuickToolsGroup(): OptionGroup {
        val g = SRConfig.settings.general
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("general.quick_tools")))
            .description(OptionDescription.of(Component.translatable(guiKey("general.quick_tools.desc"))))
            .collapsed(true)
            .option(boolOpt("general.enable_calc", false, { g.enableStandaloneCalc }, { g.enableStandaloneCalc = it }))
            .option(boolOpt("general.pin_tooltip", true, { g.pinTooltip }, { g.pinTooltip = it }))
            .build()
    }

    // ========== PartyCommands Category ==========

    private data class CommandToggleGroup(
        val nameKey: String, val descKey: String, val commands: List<String>
    )

    private val PC_TOGGLE_GROUPS = listOf(
        CommandToggleGroup("sraddons.gui.pc.group.party_mgmt", "sraddons.gui.pc.group.party_mgmt.desc",
            listOf("warp", "allinvite", "kick", "kickoffline", "kickall", "promote", "demote", "transfer", "disband", "invite", "leave")),
        CommandToggleGroup("sraddons.gui.pc.group.queue.f", "sraddons.gui.pc.group.queue.f.desc",
            listOf("f1", "f2", "f3", "f4", "f5", "f6", "f7")),
        CommandToggleGroup("sraddons.gui.pc.group.queue.m", "sraddons.gui.pc.group.queue.m.desc",
            listOf("m1", "m2", "m3", "m4", "m5", "m6", "m7")),
        CommandToggleGroup("sraddons.gui.pc.group.queue.t", "sraddons.gui.pc.group.queue.t.desc",
            listOf("t1", "t2", "t3", "t4", "t5")),
        CommandToggleGroup("sraddons.gui.pc.group.info", "sraddons.gui.pc.group.info.desc",
            listOf("ping", "tps", "fps", "time", "location", "coords", "holding", "status", "countdown")),
        CommandToggleGroup("sraddons.gui.pc.group.fun", "sraddons.gui.pc.group.fun.desc",
            listOf("coinflip", "8ball", "dice", "boop"))
    )

    private fun createPartyCommandsCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable(guiKey("partycommands")))
            .tooltip(Component.translatable(guiKey("partycommands.desc")))
            .group(createPCBasicSettingsGroup())
            .group(createPCResponseGroup())
            .group(createPCDelaysGroup())
            .also { category -> PC_TOGGLE_GROUPS.forEach { group -> category.group(createPCToggleGroup(group)) } }
            .group(createPCNoteGroup())
            .build()
    }

    private fun createPCBasicSettingsGroup(): OptionGroup {
        val pc = SRConfig.settings.partyCommands
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("pc.basic_settings")))
            .description(OptionDescription.of(Component.translatable(guiKey("pc.basic_settings.desc"))))
            .collapsed(false)
            .option(boolOpt("pc.mod_enabled", true, { pc.enabled }, { pc.enabled = it }))
            .option(strOpt("pc.command_prefix", "!", { pc.prefix }, { pc.prefix = it }))
            .build()
    }

    private fun createPCResponseGroup(): OptionGroup {
        val pc = SRConfig.settings.partyCommands
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("pc.response_settings")))
            .description(OptionDescription.of(Component.translatable(guiKey("pc.response_settings.desc"))))
            .collapsed(false)
            .option(boolOpt("pc.respond_party", true, { pc.respondInPartyChat }, { pc.respondInPartyChat = it }))
            .option(boolOpt("pc.respond_local", true, { pc.showResponseLocally }, { pc.showResponseLocally = it }))
            .option(boolOpt("pc.auto_reply_mod", true, { pc.mod }, { pc.mod = it }))
            .build()
    }

    private fun createPCDelaysGroup(): OptionGroup {
        val pc = SRConfig.settings.partyCommands
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("pc.delays")))
            .description(OptionDescription.of(Component.translatable(guiKey("pc.delays.desc"))))
            .collapsed(true)
            .option(intOpt("pc.reply_mod_delay", 500, 100, 5000, 100, { pc.autoReplyModDelayMs }, { pc.autoReplyModDelayMs = it }))
            .option(intOpt("pc.reply_github_delay", 800, 100, 5000, 100, { pc.autoReplyGithubDelayMs }, { pc.autoReplyGithubDelayMs = it }))
            .option(intOpt("pc.update_cooldown", 60000, 5000, 300000, 5000, { pc.partyListUpdateCooldownMs }, { pc.partyListUpdateCooldownMs = it }))
            .option(intOpt("pc.update_init_delay", 500, 100, 5000, 100, { pc.partyListInitialDelayMs }, { pc.partyListInitialDelayMs = it }))
            .option(intOpt("pc.update_delay", 1500, 100, 10000, 100, { pc.partyListUpdateDelayMs }, { pc.partyListUpdateDelayMs = it }))
            .build()
    }

    private fun createPCToggleGroup(group: CommandToggleGroup): OptionGroup {
        val builder = OptionGroup.createBuilder()
            .name(Component.translatable(group.nameKey))
            .description(OptionDescription.of(Component.translatable(group.descKey)))
            .collapsed(true)
        group.commands.forEach { cmd ->
            builder.option(
                Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.pc.toggle.$cmd"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.toggle.$cmd.desc")))
                    .binding(true,
                        { SRConfig.isCommandEnabled(cmd) },
                        { enabled -> if (enabled) SRConfig.settings.partyCommands.disabledCommands.remove(cmd) else SRConfig.settings.partyCommands.disabledCommands.add(cmd) })
                    .controller(TickBoxControllerBuilder::create)
                    .build())
        }
        return builder.build()
    }

    private fun createPCNoteGroup(): OptionGroup {
        val pc = SRConfig.settings.partyCommands
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("pc.group.note_sound")))
            .description(OptionDescription.of(Component.translatable(guiKey("pc.group.note_sound.desc"))))
            .collapsed(true)
            .option(strOpt("pc.note_message", "", { pc.note }, { pc.note = it }))
            .option(boolOpt("pc.countdown_sound", true, { pc.countdownSound }, { pc.countdownSound = it }))
            .build()
    }

    // ========== StarredMob Category ==========

    private fun createStarredMobCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable(guiKey("starredmob")))
            .tooltip(Component.translatable(guiKey("starredmob.desc")))
            .group(createSMGeneralGroup())
            .group(createSMRenderGroup())
            .build()
    }

    private fun createSMGeneralGroup(): OptionGroup {
        val sm = SRConfig.settings.starredMob
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("starredmob.general")))
            .description(OptionDescription.of(Component.translatable(guiKey("starredmob.general.desc"))))
            .collapsed(false)
            .option(boolOpt("starredmob.enabled", true, { sm.enabled }, { sm.enabled = it }))
            .build()
    }

    private fun createSMRenderGroup(): OptionGroup {
        val sm = SRConfig.settings.starredMob
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("starredmob.render")))
            .description(OptionDescription.of(Component.translatable(guiKey("starredmob.render.desc"))))
            .collapsed(true)
            .option(colorOpt("starredmob.highlight_color", 255, 255, 0, 200,
                { sm.toColor() }, { r, g, b, a -> sm.colorRed = r; sm.colorGreen = g; sm.colorBlue = b; sm.colorAlpha = a }))
            .option(dropDownOpt("starredmob.render_mode", "BOTH", RENDER_MODES, { sm.renderMode }, { sm.renderMode = it }))
            .option(intOpt("starredmob.line_width", 3, 1, 10, 1, { sm.lineWidth }, { sm.lineWidth = it.coerceIn(1, 10) }))
            .option(intOpt("starredmob.max_distance", 64, 10, 128, 1, { sm.maxDistance }, { sm.maxDistance = it.coerceIn(10, 128) }))
            .build()
    }

    // ========== Carry Category ==========

    private fun createCarryCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable(guiKey("carry")))
            .tooltip(Component.translatable(guiKey("carry.desc")))
            .group(createCarryGeneralGroup())
            .group(createCarryHighlightProfilesGroup())
            .group(createBossNotificationGroup())
            .group(createCarryRenderGroup())
            .group(createMinibossDistanceGroup())
            .build()
    }

    private fun createCarryGeneralGroup(): OptionGroup {
        val c = SRConfig.settings.carry
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("carry.general")))
            .description(OptionDescription.of(Component.translatable(guiKey("carry.general.desc"))))
            .collapsed(false)
            .option(boolOpt("carry.enabled", true, { c.enabled }, { c.enabled = it }))
            .build()
    }

    private fun createCarryHighlightProfilesGroup(): OptionGroup {
        val c = SRConfig.settings.carry
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("carry.highlight_profiles")))
            .description(OptionDescription.of(Component.translatable(guiKey("carry.highlight_profiles.desc"))))
            .collapsed(true)
            .option(boolOpt("carry.client_highlight", true, { c.clientHighlight.enabled }, { c.clientHighlight.enabled = it }))
            .option(colorOpt("carry.client_color", 57, 255, 20, 200,
                { c.clientHighlight.toColor() }, { r, g, b, a -> c.clientHighlight.colorRed = r; c.clientHighlight.colorGreen = g; c.clientHighlight.colorBlue = b; c.clientHighlight.colorAlpha = a }))
            .option(boolOpt("carry.boss_highlight", true, { c.bossHighlight.enabled }, { c.bossHighlight.enabled = it }))
            .option(colorOpt("carry.boss_color", 255, 50, 50, 200,
                { c.bossHighlight.toColor() }, { r, g, b, a -> c.bossHighlight.colorRed = r; c.bossHighlight.colorGreen = g; c.bossHighlight.colorBlue = b; c.bossHighlight.colorAlpha = a }))
            .option(boolOpt("carry.miniboss_highlight", true, { c.minibossHighlight.enabled }, { c.minibossHighlight.enabled = it }))
            .option(colorOpt("carry.miniboss_color", 255, 165, 0, 200,
                { c.minibossHighlight.toColor() }, { r, g, b, a -> c.minibossHighlight.colorRed = r; c.minibossHighlight.colorGreen = g; c.minibossHighlight.colorBlue = b; c.minibossHighlight.colorAlpha = a }))
            .build()
    }

    private fun createBossNotificationGroup(): OptionGroup {
        val c = SRConfig.settings.carry
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("carry.boss_notification")))
            .description(OptionDescription.of(Component.translatable(guiKey("carry.boss_notification.desc"))))
            .collapsed(true)
            .option(boolOpt("carry.boss_notification_enabled", true, { c.bossSpawnNotification }, { c.bossSpawnNotification = it }))
            .option(strOpt("carry.boss_notification_text", "&cBOSS SPAWNED", { c.bossSpawnNotificationText }, { c.bossSpawnNotificationText = it }))
            .option(intOpt("carry.boss_uuid_prune", 1200, 100, 72000, 100, { c.bossUuidPruneInterval }, { c.bossUuidPruneInterval = it }))
            .build()
    }

    private fun createCarryRenderGroup(): OptionGroup {
        val c = SRConfig.settings.carry
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("carry.render")))
            .description(OptionDescription.of(Component.translatable(guiKey("carry.render.desc"))))
            .collapsed(true)
            .option(dropDownOpt("carry.render_mode", "BOTH", RENDER_MODES, { c.renderMode }, { c.renderMode = it }))
            .option(intOpt("carry.line_width", 3, 1, 10, 1, { c.lineWidth }, { c.lineWidth = it.coerceIn(1, 10) }))
            .option(intOpt("carry.max_distance", 64, 10, 128, 1, { c.maxDistance }, { c.maxDistance = it.coerceIn(10, 128) }))
            .build()
    }

    private fun createMinibossDistanceGroup(): OptionGroup {
        val c = SRConfig.settings.carry
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("carry.miniboss_distance")))
            .description(OptionDescription.of(Component.translatable(guiKey("carry.miniboss_distance.desc"))))
            .collapsed(true)
            .option(intOpt("carry.miniboss_max_distance", 16, 4, 32, 1, { c.minibossMaxDistance }, { c.minibossMaxDistance = it.coerceIn(4, 32) }))
            .build()
    }

    // ========== Alerts Category ==========

    private fun createAlertsCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable(guiKey("alerts")))
            .tooltip(Component.translatable(guiKey("alerts.desc")))
            .group(createRagnarockGroup())
            .group(createPingAlertGroup())
            .group(createTpsAlertGroup())
            .group(createChatAlertGroup())
            .group(createChatAlertList())
            .build()
    }

    private fun createChatAlertList() = ListOption.createBuilder<String>()
        .name(Component.translatable(guiKey("alerts.chat_alert.entries")))
        .description(OptionDescription.of(Component.translatable(guiKey("alerts.chat_alert.entries.desc"))))
        .binding(Binding.generic(
            mutableListOf(),
            { SRConfig.settings.chatAlert.entries.toMutableList() },
            { SRConfig.settings.chatAlert.entries.clear(); SRConfig.settings.chatAlert.entries.addAll(it) }
        ))
        .controller(StringControllerBuilder::create)
        .initial("keyword | subtitle | 5 | yes | yes")
        .build()

    private fun createRagnarockGroup(): OptionGroup {
        val r = SRConfig.settings.ragnarock
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("alerts.ragnarock")))
            .description(OptionDescription.of(Component.translatable(guiKey("alerts.ragnarock.desc"))))
            .collapsed(true)
            .option(boolOpt("alerts.ragnarock.enabled", true, { r.enabled }, { r.enabled = it }))
            .option(boolOpt("alerts.ragnarock.play_sound", true, { r.playSound }, { r.playSound = it }))
            .option(boolOpt("alerts.ragnarock.cast_notification", true, { r.castNotification }, { r.castNotification = it }))
            .option(strOpt("alerts.ragnarock.cast_message", "&aCasted Rag", { r.castMessage }, { r.castMessage = it }))
            .option(boolOpt("alerts.ragnarock.cancel_notification", true, { r.cancelNotification }, { r.cancelNotification = it }))
            .option(strOpt("alerts.ragnarock.cancel_message", "&cRagnarock Cancelled!", { r.cancelMessage }, { r.cancelMessage = it }))
            .option(boolOpt("alerts.ragnarock.show_strength", true, { r.showStrengthGained }, { r.showStrengthGained = it }))
            .option(boolOpt("alerts.ragnarock.announce_strength", false, { r.announceStrengthInParty }, { r.announceStrengthInParty = it }))
            .build()
    }

    private fun createPingAlertGroup(): OptionGroup {
        val p = SRConfig.settings.pingAlert
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("alerts.ping_alert")))
            .description(OptionDescription.of(Component.translatable(guiKey("alerts.ping_alert.desc"))))
            .collapsed(true)
            .option(boolOpt("alerts.ping_alert.enabled", false, { p.enabled }, { p.enabled = it }))
            .option(intOpt("alerts.ping_alert.threshold", 400, 50, 5000, 10, { p.threshold }, { p.threshold = it.coerceIn(50, 5000) }))
            .option(intOpt("alerts.ping_alert.delay", 3, 1, 30, 1, { p.delaySeconds }, { p.delaySeconds = it.coerceIn(1, 30) }))
            .option(strOpt("alerts.ping_alert.message", "&cHigh Ping", { p.message }, { p.message = it }))
            .option(boolOpt("alerts.ping_alert.play_sound", true, { p.playSound }, { p.playSound = it }))
            .build()
    }

    private fun createTpsAlertGroup(): OptionGroup {
        val t = SRConfig.settings.tpsAlert
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("alerts.tps_alert")))
            .description(OptionDescription.of(Component.translatable(guiKey("alerts.tps_alert.desc"))))
            .collapsed(true)
            .option(boolOpt("alerts.tps_alert.enabled", false, { t.enabled }, { t.enabled = it }))
            .option(
                Option.createBuilder<Double>()
                    .name(Component.translatable(guiKey("alerts.tps_alert.threshold")))
                    .description(OptionDescription.of(Component.translatable(guiKey("alerts.tps_alert.threshold.desc"))))
                    .binding(16.0, { t.threshold }, { t.threshold = it.coerceIn(1.0, 20.0) })
                    .controller { opt -> DoubleSliderControllerBuilder.create(opt).range(1.0, 20.0).step(0.5) }
                    .build())
            .option(intOpt("alerts.tps_alert.delay", 3, 1, 30, 1, { t.delaySeconds }, { t.delaySeconds = it.coerceIn(1, 30) }))
            .option(strOpt("alerts.tps_alert.message", "&cLow TPS", { t.message }, { t.message = it }))
            .option(boolOpt("alerts.tps_alert.play_sound", true, { t.playSound }, { t.playSound = it }))
            .build()
    }

    private fun createChatAlertGroup(): OptionGroup {
        val ca = SRConfig.settings.chatAlert
        return OptionGroup.createBuilder()
            .name(Component.translatable(guiKey("alerts.chat_alert")))
            .description(OptionDescription.of(Component.translatable(guiKey("alerts.chat_alert.desc"))))
            .collapsed(true)
            .option(boolOpt("alerts.chat_alert.enabled", true, { ca.enabled }, { ca.enabled = it }))
            .build()
    }

    // ========== Utility ==========

    fun open() {
        val mc = Minecraft.getInstance()
        Scheduler.schedule(GUI_OPEN_DELAY_MS) {
            mc.execute { mc.setScreen(createScreen(mc.screen)) }
        }
    }
}
