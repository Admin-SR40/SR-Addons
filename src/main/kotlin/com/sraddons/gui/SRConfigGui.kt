package com.sraddons.gui

import com.sraddons.config.SRConfig
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.ColorControllerBuilder
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.awt.Color

object SRConfigGui {

    fun createScreen(parent: Screen?): Screen {
        return YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("sraddons.gui.title"))
            .save { SRConfig.save() }
            .category(createEntityFireCategory())
            .category(createPartyCommandsCategory())
            .category(createStarredMobCategory())
            .category(createCarryCategory())
            .build()
            .generateScreen(parent)
    }

    // ========== EntityFire Category ==========

    private fun createEntityFireCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable("sraddons.gui.entityfire"))
            .tooltip(Component.translatable("sraddons.gui.entityfire.desc"))
            .group(
                OptionGroup.createBuilder()
                    .name(Component.translatable("sraddons.gui.pc.group.general"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.group.general.desc")))
                    .collapsed(false)
                    .option(
                        dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                            .name(Component.translatable("sraddons.gui.entityfire.hidden_fire"))
                            .description(OptionDescription.of(Component.translatable("sraddons.gui.entityfire.hidden_fire.desc")))
                            .binding(false, { SRConfig.settings.entityFire.hiddenFire }, { SRConfig.settings.entityFire.hiddenFire = it })
                            .controller(TickBoxControllerBuilder::create)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    // ========== PartyCommands Category ==========

    private fun createPartyCommandsCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.translatable("sraddons.gui.partycommands"))
            .tooltip(Component.translatable("sraddons.gui.partycommands.desc"))
            .group(createPCBasicSettingsGroup())
            .group(createPCResponseGroup())
            .group(createPCToggleGroup("sraddons.gui.pc.group.party_mgmt", "sraddons.gui.pc.group.party_mgmt.desc", listOf(
                "!warp" to Binding({ SRConfig.settings.partyCommands.warp }, { SRConfig.settings.partyCommands.warp = it }),
                "!allinvite" to Binding({ SRConfig.settings.partyCommands.allinvite }, { SRConfig.settings.partyCommands.allinvite = it }),
                "!kick" to Binding({ SRConfig.settings.partyCommands.kick }, { SRConfig.settings.partyCommands.kick = it }),
                "!kickoffline" to Binding({ SRConfig.settings.partyCommands.kickoffline }, { SRConfig.settings.partyCommands.kickoffline = it }),
                "!kickall" to Binding({ SRConfig.settings.partyCommands.kickall }, { SRConfig.settings.partyCommands.kickall = it }),
                "!promote" to Binding({ SRConfig.settings.partyCommands.promote }, { SRConfig.settings.partyCommands.promote = it }),
                "!demote" to Binding({ SRConfig.settings.partyCommands.demote }, { SRConfig.settings.partyCommands.demote = it }),
                "!transfer" to Binding({ SRConfig.settings.partyCommands.transfer }, { SRConfig.settings.partyCommands.transfer = it }),
                "!disband" to Binding({ SRConfig.settings.partyCommands.disband }, { SRConfig.settings.partyCommands.disband = it }),
                "!invite" to Binding({ SRConfig.settings.partyCommands.invite }, { SRConfig.settings.partyCommands.invite = it }),
                "!leave" to Binding({ SRConfig.settings.partyCommands.leave }, { SRConfig.settings.partyCommands.leave = it })
            )))
            .group(createPCToggleGroup("sraddons.gui.pc.group.queue", "sraddons.gui.pc.group.queue.desc", listOf(
                "!f1-f7 / !m1-m7 / !t1-t5" to Binding({ SRConfig.settings.partyCommands.queueInstance }, { SRConfig.settings.partyCommands.queueInstance = it })
            )))
            .group(createPCToggleGroup("sraddons.gui.pc.group.info", "sraddons.gui.pc.group.info.desc", listOf(
                "!ping" to Binding({ SRConfig.settings.partyCommands.ping }, { SRConfig.settings.partyCommands.ping = it }),
                "!tps" to Binding({ SRConfig.settings.partyCommands.tps }, { SRConfig.settings.partyCommands.tps = it }),
                "!fps" to Binding({ SRConfig.settings.partyCommands.fps }, { SRConfig.settings.partyCommands.fps = it }),
                "!time" to Binding({ SRConfig.settings.partyCommands.time }, { SRConfig.settings.partyCommands.time = it }),
                "!location" to Binding({ SRConfig.settings.partyCommands.location }, { SRConfig.settings.partyCommands.location = it }),
                "!coords" to Binding({ SRConfig.settings.partyCommands.coords }, { SRConfig.settings.partyCommands.coords = it }),
                "!holding" to Binding({ SRConfig.settings.partyCommands.holding }, { SRConfig.settings.partyCommands.holding = it }),
                "!status" to Binding({ SRConfig.settings.partyCommands.status }, { SRConfig.settings.partyCommands.status = it }),
                "!cd (Countdown)" to Binding({ SRConfig.settings.partyCommands.countdown }, { SRConfig.settings.partyCommands.countdown = it })
            )))
            .group(createPCToggleGroup("sraddons.gui.pc.group.fun", "sraddons.gui.pc.group.fun.desc", listOf(
                "!fun cf" to Binding({ SRConfig.settings.partyCommands.coinflip }, { SRConfig.settings.partyCommands.coinflip = it }),
                "!fun 8ball" to Binding({ SRConfig.settings.partyCommands.eightball }, { SRConfig.settings.partyCommands.eightball = it }),
                "!fun dice" to Binding({ SRConfig.settings.partyCommands.dice }, { SRConfig.settings.partyCommands.dice = it }),
                "!fun boop" to Binding({ SRConfig.settings.partyCommands.boop }, { SRConfig.settings.partyCommands.boop = it })
            )))
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
                    .name(Component.translatable("sraddons.gui.pc.remove_separator"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.remove_separator.desc")))
                    .binding(true, { SRConfig.settings.partyCommands.removeSeparator }, { SRConfig.settings.partyCommands.removeSeparator = it })
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

    private fun createPCToggleGroup(nameKey: String, descKey: String, toggles: List<Pair<String, Binding<Boolean>>>): OptionGroup {
        val groupBuilder = OptionGroup.createBuilder()
            .name(Component.translatable(nameKey))
            .description(OptionDescription.of(Component.translatable(descKey)))
            .collapsed(true)

        toggles.forEach { (commandName, binding) ->
            groupBuilder.option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal(commandName))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.pc.mod_enabled.desc")))
                    .binding(true, binding.getter, binding.setter)
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
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.starredmob.see_through"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.see_through.desc")))
                    .binding(false, { SRConfig.settings.starredMob.seeThroughWalls }, { SRConfig.settings.starredMob.seeThroughWalls = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createSMRenderGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.translatable("sraddons.gui.starredmob.render"))
            .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.render.desc")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Color>()
                    .name(Component.translatable("sraddons.gui.starredmob.highlight_color"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.highlight_color.desc")))
                    .binding(
                        Color(255, 255, 0, 200),
                        {
                            Color(
                                SRConfig.settings.starredMob.colorRed.coerceIn(0, 255),
                                SRConfig.settings.starredMob.colorGreen.coerceIn(0, 255),
                                SRConfig.settings.starredMob.colorBlue.coerceIn(0, 255),
                                SRConfig.settings.starredMob.colorAlpha.coerceIn(0, 255)
                            )
                        },
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
                    .binding(
                        "BOTH",
                        { SRConfig.settings.starredMob.renderMode },
                        { SRConfig.settings.starredMob.renderMode = it }
                    )
                    .controller { option ->
                        DropdownStringControllerBuilder.create(option)
                            .allowAnyValue(false)
                            .values(listOf("OUTLINE", "FILL", "BOTH"))
                    }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.starredmob.line_width"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.line_width.desc")))
                    .binding(
                        3,
                        { SRConfig.settings.starredMob.lineWidth },
                        { SRConfig.settings.starredMob.lineWidth = it.coerceIn(1, 10) }
                    )
                    .controller { option ->
                        IntegerSliderControllerBuilder.create(option)
                            .range(1, 10)
                            .step(1)
                    }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.starredmob.max_distance"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.starredmob.max_distance.desc")))
                    .binding(
                        64,
                        { SRConfig.settings.starredMob.maxDistance },
                        { SRConfig.settings.starredMob.maxDistance = it.coerceIn(10, 128) }
                    )
                    .controller { option ->
                        IntegerSliderControllerBuilder.create(option)
                            .range(10, 128)
                            .step(1)
                    }
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
            .group(createCarryHighlightGroup(
                "sraddons.gui.carry.client_highlight",
                "sraddons.gui.carry.client_highlight.desc",
                { SRConfig.settings.carry.clientHighlight.enabled },
                { SRConfig.settings.carry.clientHighlight.enabled = it },
                {
                    Color(
                        SRConfig.settings.carry.clientHighlight.colorRed.coerceIn(0, 255),
                        SRConfig.settings.carry.clientHighlight.colorGreen.coerceIn(0, 255),
                        SRConfig.settings.carry.clientHighlight.colorBlue.coerceIn(0, 255),
                        SRConfig.settings.carry.clientHighlight.colorAlpha.coerceIn(0, 255)
                    )
                },
                { c ->
                    SRConfig.settings.carry.clientHighlight.colorRed = c.red
                    SRConfig.settings.carry.clientHighlight.colorGreen = c.green
                    SRConfig.settings.carry.clientHighlight.colorBlue = c.blue
                    SRConfig.settings.carry.clientHighlight.colorAlpha = c.alpha
                }
            ))
            .group(createCarryHighlightGroup(
                "sraddons.gui.carry.boss_highlight",
                "sraddons.gui.carry.boss_highlight.desc",
                { SRConfig.settings.carry.bossHighlight.enabled },
                { SRConfig.settings.carry.bossHighlight.enabled = it },
                {
                    Color(
                        SRConfig.settings.carry.bossHighlight.colorRed.coerceIn(0, 255),
                        SRConfig.settings.carry.bossHighlight.colorGreen.coerceIn(0, 255),
                        SRConfig.settings.carry.bossHighlight.colorBlue.coerceIn(0, 255),
                        SRConfig.settings.carry.bossHighlight.colorAlpha.coerceIn(0, 255)
                    )
                },
                { c ->
                    SRConfig.settings.carry.bossHighlight.colorRed = c.red
                    SRConfig.settings.carry.bossHighlight.colorGreen = c.green
                    SRConfig.settings.carry.bossHighlight.colorBlue = c.blue
                    SRConfig.settings.carry.bossHighlight.colorAlpha = c.alpha
                }
            ))
            .group(createCarryRenderGroup())
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

    private fun createCarryHighlightGroup(
        nameKey: String,
        descKey: String,
        enabledGetter: () -> Boolean,
        enabledSetter: (Boolean) -> Unit,
        colorGetter: () -> Color,
        colorSetter: (Color) -> Unit
    ): OptionGroup {
        val descStr = Component.translatable(descKey).string
        return OptionGroup.createBuilder()
            .name(Component.translatable(nameKey))
            .description(OptionDescription.of(Component.translatable(descKey)))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.carry.highlight_enabled"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.highlight_enabled.desc", descStr)))
                    .binding(true, enabledGetter, enabledSetter)
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Color>()
                    .name(Component.translatable("sraddons.gui.carry.highlight_color"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.highlight_color.desc")))
                    .binding(Color(255, 255, 0, 200), colorGetter, colorSetter)
                    .controller(ColorControllerBuilder::create)
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
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.translatable("sraddons.gui.carry.see_through"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.see_through.desc")))
                    .binding(false, { SRConfig.settings.carry.seeThroughWalls }, { SRConfig.settings.carry.seeThroughWalls = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.translatable("sraddons.gui.carry.render_mode"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.render_mode.desc")))
                    .binding(
                        "BOTH",
                        { SRConfig.settings.carry.renderMode },
                        { SRConfig.settings.carry.renderMode = it }
                    )
                    .controller { option ->
                        DropdownStringControllerBuilder.create(option)
                            .allowAnyValue(false)
                            .values(listOf("OUTLINE", "FILL", "BOTH"))
                    }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.carry.line_width"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.line_width.desc")))
                    .binding(
                        3,
                        { SRConfig.settings.carry.lineWidth },
                        { SRConfig.settings.carry.lineWidth = it.coerceIn(1, 10) }
                    )
                    .controller { option ->
                        IntegerSliderControllerBuilder.create(option)
                            .range(1, 10)
                            .step(1)
                    }
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Int>()
                    .name(Component.translatable("sraddons.gui.carry.max_distance"))
                    .description(OptionDescription.of(Component.translatable("sraddons.gui.carry.max_distance.desc")))
                    .binding(
                        64,
                        { SRConfig.settings.carry.maxDistance },
                        { SRConfig.settings.carry.maxDistance = it.coerceIn(10, 128) }
                    )
                    .controller { option ->
                        IntegerSliderControllerBuilder.create(option)
                            .range(10, 128)
                            .step(1)
                    }
                    .build()
            )
            .build()
    }

    // ========== Utility ==========

    data class Binding<T>(val getter: () -> T, val setter: (T) -> Unit)

    fun open() {
        val mc = Minecraft.getInstance()
        Thread {
            Thread.sleep(50)
            mc.execute {
                mc.setScreen(createScreen(mc.screen))
            }
        }.start()
    }
}
