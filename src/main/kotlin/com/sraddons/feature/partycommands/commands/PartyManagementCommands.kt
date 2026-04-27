package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object PartyManagementCommands {
    private val mc = Minecraft.getInstance()

    private fun label(key: String) = Component.translatable("sraddons.pc.label.$key")
    private fun error(key: String, vararg args: Any) = Component.translatable("sraddons.pc.error.$key", *args).withColor(0xFF5555)

    fun register() {
        Commands.add(object : Command("warp", "Warp party members", "w") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.warp) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("party warp")
                            respond(formatResponse(label("warp"), Component.translatable("sraddons.pc.warp.sent").withColor(0x55FF55)))
                        } else { respond(formatResponse(label("error"), error("not_leader"))) }
                    } else { respondDisabled("warp") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("allinvite", "Enable all invite", "allinv") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.allinvite) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("party settings allinvite")
                            respond(formatResponse(label("all_invite"), Component.translatable("sraddons.pc.allinvite.enabled").withColor(0x55FF55)))
                        } else { respond(formatResponse(label("error"), error("not_leader"))) }
                    } else { respondDisabled("allinvite") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("transfer", "Transfer party leader", "pt") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { val cleanMember = it.replace("\u25cf", "").trim(); !cleanMember.equals(myName, ignoreCase = true) }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (!PartyUtils.isInParty) {
                            respond(formatResponse(label("error"), error("not_in_party")))
                        } else if (PartyUtils.isLeader()) {
                            val input = StringArgumentType.getString(ctx, "player")
                            val target = PartyUtils.findMember(input)
                            sendCommand("p transfer $target")
                            respond(formatResponse(label("transfer"), Component.translatable("sraddons.pc.transfer.success", Component.literal(target)).withColor(0x55FF55)))
                        } else { respond(formatResponse(label("error"), error("not_leader"))) }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse(label("usage"), Component.literal("§c!transfer <player>")))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("promote", "Promote member") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { val cleanMember = it.replace("\u25cf", "").trim(); !cleanMember.equals(myName, ignoreCase = true) }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (SRConfig.settings.partyCommands.promote) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("party promote $target")
                                respond(formatResponse(label("promote"), Component.translatable("sraddons.pc.promote.success", Component.literal(target)).withColor(0x55FF55)))
                            } else { respond(formatResponse(label("error"), error("not_leader"))) }
                        } else { respondDisabled("promote") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (SRConfig.settings.partyCommands.promote) {
                        respond(formatResponse(label("usage"), Component.literal("§c!promote <player>")))
                    } else { respondDisabled("promote") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("demote", "Demote member") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { val cleanMember = it.replace("\u25cf", "").trim(); !cleanMember.equals(myName, ignoreCase = true) }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (SRConfig.settings.partyCommands.demote) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("party demote $target")
                                respond(formatResponse(label("demote"), Component.translatable("sraddons.pc.demote.success", Component.literal(target)).withColor(0x55FF55)))
                            } else { respond(formatResponse(label("error"), error("not_leader"))) }
                        } else { respondDisabled("demote") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (SRConfig.settings.partyCommands.demote) {
                        respond(formatResponse(label("usage"), Component.literal("§c!demote <player>")))
                    } else { respondDisabled("demote") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("disband", "Disband party") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.disband) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("p disband")
                            modMessage(formatResponse(label("disband"), Component.translatable("sraddons.pc.disband.done").withColor(0x55FF55)))
                        } else { respond(formatResponse(label("error"), error("not_leader"))) }
                    } else { respondDisabled("disband") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("leave", "Leave party") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.leave) {
                        sendCommand("p leave")
                        modMessage(formatResponse(label("leave"), Component.translatable("sraddons.pc.leave.done").withColor(0x55FF55)))
                    } else { respondDisabled("leave") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("kick", "Kick member", "k") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { val cleanMember = it.replace("\u25cf", "").trim(); !cleanMember.equals(myName, ignoreCase = true) }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(Command.argument("reason", StringArgumentType.greedyString())
                        .executes { ctx ->
                            if (SRConfig.settings.partyCommands.kick) {
                                if (PartyUtils.isLeader()) {
                                    val input = StringArgumentType.getString(ctx, "player")
                                    val target = PartyUtils.findMember(input)
                                    val reason = StringArgumentType.getString(ctx, "reason").noControlCodes
                                    sendPartyChat("Kicking $target : $reason")
                                    Thread {
                                        Thread.sleep(500)
                                        mc.execute { sendCommand("p kick $target") }
                                    }.start()
                                    respond(formatResponse(label("kick"), Component.translatable("sraddons.pc.kick.success", Component.literal(target)).withColor(0x55FF55)))
                                } else { respond(formatResponse(label("error"), error("not_leader"))) }
                            } else { respondDisabled("kick") }
                            Command.SINGLE_SUCCESS
                        })
                    .executes { ctx ->
                        if (SRConfig.settings.partyCommands.kick) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("p kick $target")
                                respond(formatResponse(label("kick"), Component.translatable("sraddons.pc.kick.success", Component.literal(target)).withColor(0x55FF55)))
                            } else { respond(formatResponse(label("error"), error("not_leader"))) }
                        } else { respondDisabled("kick") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse(label("usage"), Component.literal("§c!kick <player> [reason]")))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("kickoffline", "Kick offline members") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.kickoffline) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("p kickoffline")
                            respond(formatResponse(label("kickoffline"), Component.translatable("sraddons.pc.kickoffline.done").withColor(0x55FF55)))
                        } else { respond(formatResponse(label("error"), error("not_leader"))) }
                    } else { respondDisabled("kickoffline") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("kickall", "Kick all members except specified") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("exceptions", StringArgumentType.greedyString())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { val cleanMember = it.replace("\u25cf", "").trim(); !cleanMember.equals(myName, ignoreCase = true) }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (SRConfig.settings.partyCommands.kickall) {
                            if (PartyUtils.isLeader()) {
                                val exceptionsInput = StringArgumentType.getString(ctx, "exceptions")
                                val exceptions = exceptionsInput.split(" ").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                val myName = mc.player?.name?.string ?: ""
                                exceptions.add(myName)
                                val toKick = PartyUtils.members.filter { member ->
                                    exceptions.none { exception -> member.equals(exception, ignoreCase = true) }
                                }
                                if (toKick.isNotEmpty()) {
                                    sendPartyChat("Kicking all members...")
                                    toKick.forEachIndexed { index, target ->
                                        Thread {
                                            Thread.sleep(500L * (index + 1))
                                            mc.execute { sendCommand("p kick $target") }
                                        }.start()
                                    }
                                    respond(formatResponse(label("kickall"), Component.translatable("sraddons.pc.kickall.kicking", toKick.size.toString()).withColor(0x55FF55)))
                                } else { respond(formatResponse(label("kickall"), Component.translatable("sraddons.pc.kickall.none").withColor(0xFF5555))) }
                            } else { respond(formatResponse(label("error"), error("not_leader"))) }
                        } else { respondDisabled("kickall") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (SRConfig.settings.partyCommands.kickall) {
                        if (PartyUtils.isLeader()) {
                            val myName = mc.player?.name?.string ?: ""
                            val toKick = PartyUtils.members.filter { !it.equals(myName, ignoreCase = true) }
                            if (toKick.isNotEmpty()) {
                                sendPartyChat("Kicking all members...")
                                toKick.forEachIndexed { index, target ->
                                    Thread {
                                        Thread.sleep(500L * (index + 1))
                                        mc.execute { sendCommand("p kick $target") }
                                    }.start()
                                }
                                respond(formatResponse(label("kickall"), Component.translatable("sraddons.pc.kickall.kicking", toKick.size.toString()).withColor(0x55FF55)))
                            } else { respond(formatResponse(label("kickall"), Component.translatable("sraddons.pc.kickall.none").withColor(0xFF5555))) }
                        } else { respond(formatResponse(label("error"), error("not_leader"))) }
                    } else { respondDisabled("kickall") }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("invite", "Invite player to party", "inv") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .executes { ctx ->
                        if (SRConfig.settings.partyCommands.invite) {
                            val target = StringArgumentType.getString(ctx, "player")
                            sendCommand("p invite $target")
                            respond(formatResponse(label("invite"), Component.translatable("sraddons.pc.invite.success", Component.literal(target)).withColor(0x55FF55)))
                        } else { respondDisabled("invite") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse(label("usage"), Component.literal("§c!invite <player>")))
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }
}
