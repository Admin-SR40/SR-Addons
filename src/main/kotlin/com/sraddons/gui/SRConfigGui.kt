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
            .title(Component.literal("SR-Addons"))
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
            .name(Component.literal("EntityFire"))
            .tooltip(Component.literal("Hide entity fire effect"))
            .group(
                OptionGroup.createBuilder()
                    .name(Component.literal("General"))
                    .description(OptionDescription.of(Component.literal("EntityFire settings")))
                    .collapsed(false)
                    .option(
                        dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                            .name(Component.literal("Hidden Fire"))
                            .description(OptionDescription.of(Component.literal("Hide fire effect on burning entities")))
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
            .name(Component.literal("PartyCommands"))
            .tooltip(Component.literal("Party commands configuration"))
            .group(createPCBasicSettingsGroup())
            .group(createPCResponseGroup())
            .group(createPCToggleGroup("Party Management", "Warp, invite, kick, promote, demote, transfer, disband, leave", listOf(
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
            .group(createPCToggleGroup("Queue Commands", "Dungeon and Kuudra queue", listOf(
                "!f1-f7 / !m1-m7 / !t1-t5" to Binding({ SRConfig.settings.partyCommands.queueInstance }, { SRConfig.settings.partyCommands.queueInstance = it })
            )))
            .group(createPCToggleGroup("Info Commands", "Ping, TPS, FPS, time, location, coords, holding, status, countdown", listOf(
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
            .group(createPCToggleGroup("Fun Commands", "cf, 8ball, dice, boop, random", listOf(
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
            .name(Component.literal("Basic Settings"))
            .description(OptionDescription.of(Component.literal("General mod settings")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Mod Enabled"))
                    .description(OptionDescription.of(Component.literal("Master toggle for party commands")))
                    .binding(true, { SRConfig.settings.partyCommands.enabled }, { SRConfig.settings.partyCommands.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.literal("Command Prefix"))
                    .description(OptionDescription.of(Component.literal("Prefix for party commands (default: !)")))
                    .binding("!", { SRConfig.settings.partyCommands.prefix }, { SRConfig.settings.partyCommands.prefix = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createPCResponseGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.literal("Response Settings"))
            .description(OptionDescription.of(Component.literal("Where to show command responses")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Respond in Party Chat"))
                    .description(OptionDescription.of(Component.literal("Send command responses to party chat")))
                    .binding(true, { SRConfig.settings.partyCommands.respondInPartyChat }, { SRConfig.settings.partyCommands.respondInPartyChat = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Show Response Locally"))
                    .description(OptionDescription.of(Component.literal("Show command responses in your own chat HUD")))
                    .binding(true, { SRConfig.settings.partyCommands.showResponseLocally }, { SRConfig.settings.partyCommands.showResponseLocally = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Remove Separator Lines"))
                    .description(OptionDescription.of(Component.literal("Hide decorative separator lines (---) from Hypixel chat")))
                    .binding(true, { SRConfig.settings.partyCommands.removeSeparator }, { SRConfig.settings.partyCommands.removeSeparator = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Auto Reply to !mod"))
                    .description(OptionDescription.of(Component.literal("Automatically reply when party members send !mod")))
                    .binding(true, { SRConfig.settings.partyCommands.mod }, { SRConfig.settings.partyCommands.mod = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createPCToggleGroup(name: String, desc: String, toggles: List<Pair<String, Binding<Boolean>>>): OptionGroup {
        val groupBuilder = OptionGroup.createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc)))
            .collapsed(true)

        toggles.forEach { (commandName, binding) ->
            groupBuilder.option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal(commandName))
                    .description(OptionDescription.of(Component.literal("Enable $commandName command")))
                    .binding(true, binding.getter, binding.setter)
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
        }

        return groupBuilder.build()
    }

    private fun createPCNoteGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.literal("Note & Sound"))
            .description(OptionDescription.of(Component.literal("Note message and countdown sound settings")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.literal("Note Message"))
                    .description(OptionDescription.of(Component.literal("Message to send when using !note (use !note <msg> to set in-game)")))
                    .binding("", { SRConfig.settings.partyCommands.note }, { SRConfig.settings.partyCommands.note = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Countdown Sound"))
                    .description(OptionDescription.of(Component.literal("Play sound on countdown reminders")))
                    .binding(true, { SRConfig.settings.partyCommands.countdownSound }, { SRConfig.settings.partyCommands.countdownSound = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    // ========== StarredMob Category ==========

    private fun createStarredMobCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.literal("StarredMob"))
            .tooltip(Component.literal("Starred mob highlighter configuration"))
            .group(createSMGeneralGroup())
            .group(createSMRenderGroup())
            .build()
    }

    private fun createSMGeneralGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.literal("General"))
            .description(OptionDescription.of(Component.literal("General settings")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Enabled"))
                    .description(OptionDescription.of(Component.literal("Master toggle for highlighting starred mobs")))
                    .binding(true, { SRConfig.settings.starredMob.enabled }, { SRConfig.settings.starredMob.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("See Through Walls"))
                    .description(OptionDescription.of(Component.literal("Render highlights through blocks")))
                    .binding(false, { SRConfig.settings.starredMob.seeThroughWalls }, { SRConfig.settings.starredMob.seeThroughWalls = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }

    private fun createSMRenderGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.literal("Render Settings"))
            .description(OptionDescription.of(Component.literal("Appearance of the highlight")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Color>()
                    .name(Component.literal("Highlight Color"))
                    .description(OptionDescription.of(Component.literal("Color used to highlight starred mobs")))
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
                    .name(Component.literal("Render Mode"))
                    .description(OptionDescription.of(Component.literal("OUTLINE = wireframe only, FILL = solid fill only, BOTH = outline + fill")))
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
                    .name(Component.literal("Line Width"))
                    .description(OptionDescription.of(Component.literal("Thickness of the outline lines")))
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
                    .name(Component.literal("Max Distance"))
                    .description(OptionDescription.of(Component.literal("Maximum distance (in blocks) to highlight starred mobs")))
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
            .name(Component.literal("Carry"))
            .tooltip(Component.literal("Carry module configuration"))
            .group(
                OptionGroup.createBuilder()
                    .name(Component.literal("General"))
                    .description(OptionDescription.of(Component.literal("Carry module settings")))
                    .collapsed(false)
                    .option(
                        dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                            .name(Component.literal("Enabled"))
                            .description(OptionDescription.of(Component.literal("Master toggle for /cm commands")))
                            .binding(true, { SRConfig.settings.carry.enabled }, { SRConfig.settings.carry.enabled = it })
                            .controller(TickBoxControllerBuilder::create)
                            .build()
                    )
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
