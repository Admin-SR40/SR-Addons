package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import com.sraddons.util.Scheduler
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component

object ChatListener {

    private const val MOD_REPLY_DELAY_MS = 500L
    private const val GITHUB_REPLY_DELAY_MS = 800L

    private val joinedSelf = Regex("^You have joined ((?:\\[[^]]*?])? ?)?(\\w{1,16})'s? party!$")
    private val joinedOther = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the party\\.$")
    private val joinedLobby = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the lobby!$")
    private val leftParty = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has left the party\\.$")
    private val kickedParty = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has been removed from the party\\.$")
    private val kickedOffline = Regex("^Kicked ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because they were offline\\.$")
    private val kickedDisconnected = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) was removed from your party because they disconnected\\.$")
    private val transferLeave = Regex("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because ((?:\\[[^]]*?])? ?)?(\\w{1,16}) left$")
    private val transferBy = Regex("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$")
    private val partyInvite = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) invited ((?:\\[[^]]*?])? ?)?(\\w{1,16}) to the party! They have 60 seconds to accept.$")
    private val leaderDisconnected = Regex("^The party leader, ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disconnected, they have 5 minutes to rejoin before the party is disbanded\\.$")
    private val leaderRejoined = Regex("^The party leader ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has rejoined\\.$")
    private val memberDisconnected = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disconnected, they have 5 minutes to rejoin before they are removed from the party\\.$")
    private val memberRejoined = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has rejoined\\.$")
    private val membersList = Regex("^Party (Leader|Moderators|Members): (.+)$")
    private val dungeonJoin = Regex("^Party Finder > (\\w{1,16}) joined the dungeon group! ")
    private val kuudraJoin = Regex("^Party Finder > ((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the group!")
    private val partyFinderQueued = Regex("^Party Finder > Your party has been queued in the party finder!$")

    private val colorCodeRegex = Regex("§[0-9a-fk-or]")
    private val rankStripRegex = Regex("\\[.+?]\\s*")
    private val partySenderRegex = Regex("^Party > (?:\\[.+?] )?(.+?):")
    private val cancelPatterns = listOf(
        Regex("^Party > (?:\\[.+?] )?(.+?):"),
        Regex("^Guild > (?:\\[.+?] )?(.+?):"),
        Regex("^\\[\\d+\\] (?:\\[.+?] )?(.+?):"),
        Regex("^(?:\\[.+?] )?(.+?):")
    )

    private val disbandPatterns = listOf(
        Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disbanded the party!$"),
        Regex("^You have been kicked from the party by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$"),
        Regex("^The party was disbanded because all invites expired and the party was empty.$"),
        Regex("^The party was disbanded because the party leader disconnected.$"),
        Regex("^You left the party.$"),
        Regex("^You are not currently in a party.$")
    )

    fun init() {
        ClientReceiveMessageEvents.GAME.register { message: Component, _: Boolean ->
            handleMessage(message.string)
        }
    }

    private fun handleMessage(message: String) {
        joinedOther.find(message)?.let { PartyUtils.addMember(it.groupValues[2]); return }
        joinedSelf.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            PartyUtils.partyLeader = it.groupValues[2]
            PartyUtils.addMember(mc.player?.name?.string ?: return)
            return
        }
        joinedLobby.find(message)?.let {
            val playerName = it.groupValues[2]
            val myName = mc.player?.name?.string
            if (myName != null && playerName.equals(myName, ignoreCase = true)) {
                AutoPartyListUpdater.refresh()
            }
            return
        }
        leftParty.find(message)?.let { PartyUtils.removeMember(it.groupValues[2]); return }
        kickedParty.find(message)?.let { PartyUtils.removeMember(it.groupValues[2]); return }
        kickedOffline.find(message)?.let { PartyUtils.removeMemberWithOffline(it.groupValues[2]); return }
        kickedDisconnected.find(message)?.let { PartyUtils.removeMemberWithOffline(it.groupValues[2]); return }
        leaderDisconnected.find(message)?.let { PartyUtils.markOffline(it.groupValues[2]); return }
        leaderRejoined.find(message)?.let { PartyUtils.markOnline(it.groupValues[2]); return }
        transferBy.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            PartyUtils.addMember(it.groupValues[4])
            PartyUtils.partyLeader = it.groupValues[2]
            return
        }
        transferLeave.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            PartyUtils.partyLeader = it.groupValues[2]
            PartyUtils.removeMember(it.groupValues[4])
            return
        }
        leaderDisconnected.find(message)?.let { PartyUtils.partyLeader = it.groupValues[2]; return }
        leaderRejoined.find(message)?.let {
            PartyUtils.markOnline(it.groupValues[2])
            PartyUtils.partyLeader = it.groupValues[2]
            return
        }
        memberDisconnected.find(message)?.let { PartyUtils.markOffline(it.groupValues[2]); return }
        memberRejoined.find(message)?.let { PartyUtils.markOnline(it.groupValues[2]); return }
        partyInvite.find(message)?.let {
            val inviter = it.groupValues[2]
            val invited = it.groupValues[4]
            val myName = mc.player?.name?.string
            if (!PartyUtils.isInParty) {
                PartyUtils.addMember(inviter)
                PartyUtils.partyLeader = inviter
                if (myName != null && inviter.equals(myName, ignoreCase = true)) {
                    PartyUtils.addMember(myName)
                }
            } else {
                PartyUtils.addMember(inviter)
            }
            return
        }
        for (pattern in disbandPatterns) {
            if (pattern.containsMatchIn(message)) { PartyUtils.disband(); return }
        }
        dungeonJoin.find(message)?.let { PartyUtils.addMember(it.groupValues[1]); return }
        kuudraJoin.find(message)?.let { PartyUtils.addMember(it.groupValues[2]); return }
        partyFinderQueued.find(message)?.let {
            if (!PartyUtils.isInParty) {
                PartyUtils.isInParty = true
                AutoPartyListUpdater.refresh()
            }
            return
        }
        handleCancelCommand(message)
        handleModCommand(message)
    }

    private fun handleModCommand(message: String) {
        if (!SRConfig.settings.partyCommands.mod) return
        val cleanMessage = message.replace(colorCodeRegex, "")
        if (!cleanMessage.startsWith("Party >")) return
        if (!cleanMessage.contains("!mod")) return

        val match = partySenderRegex.find(cleanMessage) ?: return
        val senderRaw = match.groupValues[1].trim()
        val senderClean = senderRaw.replace(rankStripRegex, "").trim()
        val myName = mc.player?.name?.string ?: return
        if (senderClean.equals(myName, ignoreCase = true)) return

        Scheduler.schedule(MOD_REPLY_DELAY_MS) {
            mc.execute { sendPartyChat(Component.translatable("sraddons.chat.autoreply.mod").string) }
        }
        Scheduler.schedule(GITHUB_REPLY_DELAY_MS) {
            mc.execute { sendPartyChat(Component.translatable("sraddons.chat.autoreply.github").string) }
        }
    }

    private fun handleCancelCommand(message: String) {
        if (!message.contains("!cancel")) return
        val myName = mc.player?.name?.string ?: return
        val cleanMessage = message.replace(colorCodeRegex, "")
        for (pattern in cancelPatterns) {
            val match = pattern.find(cleanMessage) ?: continue
            val senderRaw = match.groupValues[1].trim()
            val senderClean = senderRaw.replace(rankStripRegex, "").trim()
            if (senderClean.equals(myName, ignoreCase = true)) return
            CountdownManager.tryCancelFromPartyChat(senderClean)
            return
        }
    }
}
