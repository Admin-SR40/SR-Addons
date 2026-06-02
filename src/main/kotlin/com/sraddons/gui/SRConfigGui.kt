package com.sraddons.gui

import com.sraddons.config.SRConfig
import com.sraddons.config.toColor
import com.sraddons.util.Scheduler
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.ColorControllerBuilder
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.awt.Color

object SRConfigGui {

    private const val GUI_OPEN_DELAY_MS = 50L

    fun createScreen(parent: Screen?): Screen {
        return YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("sraddons.gui.title"))
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
            .name(Component.translatable("sraddons.gui.general"))
            .tooltip(Component.translatable("sraddons.gui.general.desc"))
            .group(createDisplayGroup())
            .group(createVisualTweaksGroup())
            .group(createTextGroup())
            .group(createQuickToolsGroup())
            .build()
    }

    private fun createDisplayGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.general.group.display"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.general.group.display.desc")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.show_own_name"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.show_own_name.desc")))
                    .binding(true, { SRConfig.settings.general.showOwnNameInThirdPerson }, { SRConfig.settings.general.showOwnNameInThirdPerson = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.remove_separator"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.remove_separator.desc")))
                    .binding(true, { SRConfig.settings.general.removeSeparator }, { SRConfig.settings.general.removeSeparator = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.auto_check_updates"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.auto_check_updates.desc")))
                    .binding(false, { SRConfig.settings.general.autoCheckUpdates }, { SRConfig.settings.general.autoCheckUpdates = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createVisualTweaksGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.general.visual_tweaks"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.general.visual_tweaks.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.hide_entity_fire"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.hide_entity_fire.desc")))
                    .binding(false, { SRConfig.settings.general.hideEntityFire }, { SRConfig.settings.general.hideEntityFire = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.fullbright"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.fullbright.desc")))
                    .binding(false, { SRConfig.settings.general.fullbright }, { SRConfig.settings.general.fullbright = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.better_fov"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.better_fov.desc")))
                    .binding(false, { SRConfig.settings.general.betterFov }, { SRConfig.settings.general.betterFov = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createTextGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.general.text"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.general.text.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.replace_texts_enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.replace_texts_enabled.desc")))
                    .binding(false, { SRConfig.settings.general.replaceTextsEnabled }, { SRConfig.settings.general.replaceTextsEnabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.highlight_dev_name"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.highlight_dev_name.desc")))
                    .binding(true, { SRConfig.settings.general.highlightDevName }, { SRConfig.settings.general.highlightDevName = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createQuickToolsGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.general.quick_tools"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.general.quick_tools.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.general.enable_calc"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.general.enable_calc.desc")))
                    .binding(false, { SRConfig.settings.general.enableStandaloneCalc }, { SRConfig.settings.general.enableStandaloneCalc = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
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
            .name(Component.translatable("sraddons.gui.partycommands"))
            .tooltip(Component.translatable("sraddons.gui.partycommands.desc"))
            .group(createPCBasicSettingsGroup())
            .group(createPCResponseGroup())
            .also { category ->
                PC_TOGGLE_GROUPS.forEach { group ->
                    category.group(createPCToggleGroup(group))
                }
            }
            .group(createPCNoteGroup())
            .build()
    }

    private fun createPCBasicSettingsGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.pc.basic_settings"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.basic_settings.desc")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.pc.mod_enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.mod_enabled.desc")))
                    .binding(true, { SRConfig.settings.partyCommands.enabled }, { SRConfig.settings.partyCommands.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.pc.command_prefix"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.command_prefix.desc")))
                    .binding("!", { SRConfig.settings.partyCommands.prefix }, { SRConfig.settings.partyCommands.prefix = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createPCResponseGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.pc.response_settings"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.response_settings.desc")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.pc.respond_party"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.respond_party.desc")))
                    .binding(true, { SRConfig.settings.partyCommands.respondInPartyChat }, { SRConfig.settings.partyCommands.respondInPartyChat = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.pc.respond_local"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.respond_local.desc")))
                    .binding(true, { SRConfig.settings.partyCommands.showResponseLocally }, { SRConfig.settings.partyCommands.showResponseLocally = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.pc.auto_reply_mod"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.auto_reply_mod.desc")))
                    .binding(true, { SRConfig.settings.partyCommands.mod }, { SRConfig.settings.partyCommands.mod = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createPCToggleGroup(group: CommandToggleGroup): OptionGroup {
        val groupBuilder = OptionGroup.createBuilder()
            .name(Component.translatable(group.nameKey))
            .description(OptionDescription.of(Component.translatable(group.descKey)))
            .collapsed(true)

        group.commands.forEach { cmd ->
            groupBuilder.option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.pc.toggle.$cmd"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.toggle.$cmd.desc")))
                    .binding(true,
                        { SRConfig.isCommandEnabled(cmd) },
                        { enabled -> if (enabled) SRConfig.settings.partyCommands.disabledCommands.remove(cmd) else SRConfig.settings.partyCommands.disabledCommands.add(cmd) }
                    )
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
        }

        return groupBuilder.build()
    }

    private fun createPCNoteGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.pc.group.note_sound"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.group.note_sound.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.pc.note_message"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.note_message.desc")))
                    .binding("", { SRConfig.settings.partyCommands.note }, { SRConfig.settings.partyCommands.note = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.pc.countdown_sound"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.countdown_sound.desc")))
                    .binding(true, { SRConfig.settings.partyCommands.countdownSound }, { SRConfig.settings.partyCommands.countdownSound = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    // ========== StarredMob Category ==========

    private fun createStarredMobCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable("sraddons.gui.starredmob"))
            .tooltip(Component.translatable("sraddons.gui.starredmob.desc"))
            .group(createSMGeneralGroup())
            .group(createSMRenderGroup())
            .build()
    }

    private fun createSMGeneralGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.starredmob.general"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.general.desc")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.starredmob.enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.enabled.desc")))
                    .binding(true, { SRConfig.settings.starredMob.enabled }, { SRConfig.settings.starredMob.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createSMRenderGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.starredmob.render"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.render.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Color>()
                    .name(Component.translatable("sraddons.gui.starredmob.highlight_color"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.highlight_color.desc")))
                    .binding(
                        Color(255, 255, 0, 200),
                        { SRConfig.settings.starredMob.toColor() },
                        {
                            SRConfig.settings.starredMob.colorRed = it.red
                            SRConfig.settings.starredMob.colorGreen = it.green
                            SRConfig.settings.starredMob.colorBlue = it.blue
                            SRConfig.settings.starredMob.colorAlpha = it.alpha
                        }
                    )
                    .controller(ColorControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.starredmob.render_mode"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.render_mode.desc")))
                    .binding("BOTH", { SRConfig.settings.starredMob.renderMode }, { SRConfig.settings.starredMob.renderMode = it })
                    .controller { option ->
                        DropdownStringControllerBuilder.create(option).allowAnyValue(false).values(listOf("OUTLINE", "FILL", "BOTH"))
                    }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.starredmob.line_width"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.line_width.desc")))
                    .binding(3, { SRConfig.settings.starredMob.lineWidth }, { SRConfig.settings.starredMob.lineWidth = it.coerceIn(1, 10) })
                    .controller { option -> IntegerSliderControllerBuilder.create(option).range(1, 10).step(1) }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.starredmob.max_distance"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.max_distance.desc")))
                    .binding(64, { SRConfig.settings.starredMob.maxDistance }, { SRConfig.settings.starredMob.maxDistance = it.coerceIn(10, 128) })
                    .controller { option -> IntegerSliderControllerBuilder.create(option).range(10, 128).step(1) }
                    .build()
            )
            .build()
    }

    // ========== Carry Category ==========

    private fun createCarryCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable("sraddons.gui.carry"))
            .tooltip(Component.translatable("sraddons.gui.carry.desc"))
            .group(createCarryGeneralGroup())
            .group(createCarryHighlightProfilesGroup())
            .group(createBossNotificationGroup())
            .group(createCarryRenderGroup())
            .group(createMinibossDistanceGroup())
            .build()
    }

    private fun createCarryGeneralGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.carry.general"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.general.desc")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.carry.enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.enabled.desc")))
                    .binding(true, { SRConfig.settings.carry.enabled }, { SRConfig.settings.carry.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createCarryHighlightProfilesGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.carry.highlight_profiles"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.highlight_profiles.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.carry.client_highlight"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.client_highlight.desc")))
                    .binding(true, { SRConfig.settings.carry.clientHighlight.enabled }, { SRConfig.settings.carry.clientHighlight.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Color>()
                    .name(Component.translatable("sraddons.gui.carry.client_color"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.client_color.desc")))
                    .binding(
                        Color(57, 255, 20, 200),
                        { SRConfig.settings.carry.clientHighlight.toColor() },
                        { c ->
                            SRConfig.settings.carry.clientHighlight.colorRed = c.red
                            SRConfig.settings.carry.clientHighlight.colorGreen = c.green
                            SRConfig.settings.carry.clientHighlight.colorBlue = c.blue
                            SRConfig.settings.carry.clientHighlight.colorAlpha = c.alpha
                        }
                    )
                    .controller(ColorControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.carry.boss_highlight"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.boss_highlight.desc")))
                    .binding(true, { SRConfig.settings.carry.bossHighlight.enabled }, { SRConfig.settings.carry.bossHighlight.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Color>()
                    .name(Component.translatable("sraddons.gui.carry.boss_color"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.boss_color.desc")))
                    .binding(
                        Color(255, 50, 50, 200),
                        { SRConfig.settings.carry.bossHighlight.toColor() },
                        { c ->
                            SRConfig.settings.carry.bossHighlight.colorRed = c.red
                            SRConfig.settings.carry.bossHighlight.colorGreen = c.green
                            SRConfig.settings.carry.bossHighlight.colorBlue = c.blue
                            SRConfig.settings.carry.bossHighlight.colorAlpha = c.alpha
                        }
                    )
                    .controller(ColorControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.carry.miniboss_highlight"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.miniboss_highlight.desc")))
                    .binding(true, { SRConfig.settings.carry.minibossHighlight.enabled }, { SRConfig.settings.carry.minibossHighlight.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Color>()
                    .name(Component.translatable("sraddons.gui.carry.miniboss_color"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.miniboss_color.desc")))
                    .binding(
                        Color(255, 165, 0, 200),
                        { SRConfig.settings.carry.minibossHighlight.toColor() },
                        { c ->
                            SRConfig.settings.carry.minibossHighlight.colorRed = c.red
                            SRConfig.settings.carry.minibossHighlight.colorGreen = c.green
                            SRConfig.settings.carry.minibossHighlight.colorBlue = c.blue
                            SRConfig.settings.carry.minibossHighlight.colorAlpha = c.alpha
                        }
                    )
                    .controller(ColorControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createBossNotificationGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.carry.boss_notification"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.boss_notification.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.carry.boss_notification_enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.boss_notification_enabled.desc")))
                    .binding(true, { SRConfig.settings.carry.bossSpawnNotification }, { SRConfig.settings.carry.bossSpawnNotification = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.carry.boss_notification_text"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.boss_notification_text.desc")))
                    .binding("&cBOSS SPAWNED", { SRConfig.settings.carry.bossSpawnNotificationText }, { SRConfig.settings.carry.bossSpawnNotificationText = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createCarryRenderGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.carry.render"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.render.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.carry.render_mode"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.render_mode.desc")))
                    .binding("BOTH", { SRConfig.settings.carry.renderMode }, { SRConfig.settings.carry.renderMode = it })
                    .controller { option ->
                        DropdownStringControllerBuilder.create(option).allowAnyValue(false).values(listOf("OUTLINE", "FILL", "BOTH"))
                    }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.carry.line_width"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.line_width.desc")))
                    .binding(3, { SRConfig.settings.carry.lineWidth }, { SRConfig.settings.carry.lineWidth = it.coerceIn(1, 10) })
                    .controller { option -> IntegerSliderControllerBuilder.create(option).range(1, 10).step(1) }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.carry.max_distance"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.max_distance.desc")))
                    .binding(64, { SRConfig.settings.carry.maxDistance }, { SRConfig.settings.carry.maxDistance = it.coerceIn(10, 128) })
                    .controller { option -> IntegerSliderControllerBuilder.create(option).range(10, 128).step(1) }
                    .build()
            )
            .build()
    }

    private fun createMinibossDistanceGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.carry.miniboss_distance"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.miniboss_distance.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.carry.miniboss_max_distance"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.miniboss_max_distance.desc")))
                    .binding(16, { SRConfig.settings.carry.minibossMaxDistance }, { SRConfig.settings.carry.minibossMaxDistance = it.coerceIn(4, 32) })
                    .controller { option -> IntegerSliderControllerBuilder.create(option).range(4, 32).step(1) }
                    .build()
            )
            .build()
    }

    // ========== Alerts Category ==========

    private fun createAlertsCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable("sraddons.gui.alerts"))
            .tooltip(Component.translatable("sraddons.gui.alerts.desc"))
            .group(createRagnarockGroup())
            .group(createPingAlertGroup())
            .group(createTpsAlertGroup())
            .build()
    }

    private fun createRagnarockGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.alerts.ragnarock"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.ragnarock.enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.enabled.desc")))
                    .binding(true, { SRConfig.settings.ragnarock.enabled }, { SRConfig.settings.ragnarock.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.ragnarock.play_sound"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.play_sound.desc")))
                    .binding(true, { SRConfig.settings.ragnarock.playSound }, { SRConfig.settings.ragnarock.playSound = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.ragnarock.cast_notification"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.cast_notification.desc")))
                    .binding(true, { SRConfig.settings.ragnarock.castNotification }, { SRConfig.settings.ragnarock.castNotification = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.alerts.ragnarock.cast_message"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.cast_message.desc")))
                    .binding("&aCasted Rag", { SRConfig.settings.ragnarock.castMessage }, { SRConfig.settings.ragnarock.castMessage = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.ragnarock.cancel_notification"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.cancel_notification.desc")))
                    .binding(true, { SRConfig.settings.ragnarock.cancelNotification }, { SRConfig.settings.ragnarock.cancelNotification = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.alerts.ragnarock.cancel_message"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.cancel_message.desc")))
                    .binding("&cRagnarock Cancelled!", { SRConfig.settings.ragnarock.cancelMessage }, { SRConfig.settings.ragnarock.cancelMessage = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.ragnarock.show_strength"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.show_strength.desc")))
                    .binding(true, { SRConfig.settings.ragnarock.showStrengthGained }, { SRConfig.settings.ragnarock.showStrengthGained = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.ragnarock.announce_strength"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ragnarock.announce_strength.desc")))
                    .binding(false, { SRConfig.settings.ragnarock.announceStrengthInParty }, { SRConfig.settings.ragnarock.announceStrengthInParty = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createPingAlertGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.alerts.ping_alert"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ping_alert.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.ping_alert.enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ping_alert.enabled.desc")))
                    .binding(false, { SRConfig.settings.pingAlert.enabled }, { SRConfig.settings.pingAlert.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.alerts.ping_alert.threshold"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ping_alert.threshold.desc")))
                    .binding(400, { SRConfig.settings.pingAlert.threshold }, { SRConfig.settings.pingAlert.threshold = it.coerceIn(50, 5000) })
                    .controller { option -> IntegerSliderControllerBuilder.create(option).range(50, 5000).step(10) }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.alerts.ping_alert.delay"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ping_alert.delay.desc")))
                    .binding(3, { SRConfig.settings.pingAlert.delaySeconds }, { SRConfig.settings.pingAlert.delaySeconds = it.coerceIn(1, 30) })
                    .controller { option -> IntegerSliderControllerBuilder.create(option).range(1, 30).step(1) }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.alerts.ping_alert.message"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ping_alert.message.desc")))
                    .binding("&cHigh Ping", { SRConfig.settings.pingAlert.message }, { SRConfig.settings.pingAlert.message = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.ping_alert.play_sound"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.ping_alert.play_sound.desc")))
                    .binding(true, { SRConfig.settings.pingAlert.playSound }, { SRConfig.settings.pingAlert.playSound = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createTpsAlertGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.alerts.tps_alert"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.tps_alert.desc")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.tps_alert.enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.tps_alert.enabled.desc")))
                    .binding(false, { SRConfig.settings.tpsAlert.enabled }, { SRConfig.settings.tpsAlert.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Double>()
                    .name(Component.translatable("sraddons.gui.alerts.tps_alert.threshold"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.tps_alert.threshold.desc")))
                    .binding(16.0, { SRConfig.settings.tpsAlert.threshold }, { SRConfig.settings.tpsAlert.threshold = it.coerceIn(1.0, 20.0) })
                    .controller { option -> DoubleSliderControllerBuilder.create(option).range(1.0, 20.0).step(0.5) }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.alerts.tps_alert.delay"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.tps_alert.delay.desc")))
                    .binding(3, { SRConfig.settings.tpsAlert.delaySeconds }, { SRConfig.settings.tpsAlert.delaySeconds = it.coerceIn(1, 30) })
                    .controller { option -> IntegerSliderControllerBuilder.create(option).range(1, 30).step(1) }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.alerts.tps_alert.message"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.tps_alert.message.desc")))
                    .binding("&cLow TPS", { SRConfig.settings.tpsAlert.message }, { SRConfig.settings.tpsAlert.message = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.alerts.tps_alert.play_sound"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.alerts.tps_alert.play_sound.desc")))
                    .binding(true, { SRConfig.settings.tpsAlert.playSound }, { SRConfig.settings.tpsAlert.playSound = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    // ========== Utility ==========

    fun open() {
        val mc = Minecraft.getInstance()
        Scheduler.schedule(GUI_OPEN_DELAY_MS) {
            mc.execute {
                mc.setScreen(createScreen(mc.screen))
            }
        }
    }
}
