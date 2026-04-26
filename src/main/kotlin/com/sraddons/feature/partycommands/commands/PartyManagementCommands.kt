package com.sraddons.feature.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider

object PartyManagementCommands {
    private val mc = Minecraft.getInstance()

    fun register() {
        Commands.add(object : Command("warp", "Warp party members", "w") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (SRConfig.settings.partyCommands.warp) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("party warp")
                            respond(formatResponse("Warp", "\u00a7aSent warp request", ""))
                        } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
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
                            respond(formatResponse("All Invite", "\u00a7aEnabled", ""))
                        } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
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
                            respond(formatResponse("Error", "\u00a7cYou are not in a party!", ""))
                        } else if (PartyUtils.isLeader()) {
                            val input = StringArgumentType.getString(ctx, "player")
                            val target = PartyUtils.findMember(input)
                            sendCommand("p transfer $target")
                            respond(formatResponse("Transfer", "\u00a7aTransferred to $target", ""))
                        } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse("Usage", "\u00a7c!transfer <player>", ""))
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
                                respond(formatResponse("Promote", "\u00a7aPromoted $target", ""))
                            } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
                        } else { respondDisabled("promote") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (SRConfig.settings.partyCommands.promote) {
                        respond(formatResponse("Usage", "\u00a7c!promote <player>", ""))
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
                                respond(formatResponse("Demote", "\u00a7aDemoted $target", ""))
                            } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
                        } else { respondDisabled("demote") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (SRConfig.settings.partyCommands.demote) {
                        respond(formatResponse("Usage", "\u00a7c!demote <player>", ""))
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
                            modMessage(formatResponse("Disband", "\u00a7aParty disbanded", ""))
                        } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
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
                        modMessage(formatResponse("Leave", "\u00a7aLeft party", ""))
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
                                    respond(formatResponse("Kick", "\u00a7aKicked $target", ""))
                                } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
                            } else { respondDisabled("kick") }
                            Command.SINGLE_SUCCESS
                        })
                    .executes { ctx ->
                        if (SRConfig.settings.partyCommands.kick) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("p kick $target")
                                respond(formatResponse("Kick", "\u00a7aKicked $target", ""))
                            } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
                        } else { respondDisabled("kick") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse("Usage", "\u00a7c!kick <player> [reason]", ""))
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
                            respond(formatResponse("Kickoffline", "\u00a7aKicked offline members", ""))
                        } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
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
                                    respond(formatResponse("Kickall", "\u00a7aKicking ${toKick.size} member(s)", ""))
                                } else { respond(formatResponse("Kickall", "\u00a7cNo members to kick", "")) }
                            } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
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
                                respond(formatResponse("Kickall", "\u00a7aKicking ${toKick.size} member(s)", ""))
                            } else { respond(formatResponse("Kickall", "\u00a7cNo members to kick", "")) }
                        } else { respond(formatResponse("Error", "\u00a7cYou are not the leader!", "")) }
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
                            respond(formatResponse("Invite", "\u00a7aInvited $target", ""))
                        } else { respondDisabled("invite") }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse("Usage", "\u00a7c!invite <player>", ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }
}
