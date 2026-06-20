package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import com.sraddons.util.Scheduler
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component

object ChatListener {

    // ── Regex constants ──────────────────────────────────────────────
    // RANK: optional bracket rank like [MVP++], [ADMIN], etc.
    // NAME: player name (alphanumeric + underscore, 1-16 chars)
    private const val RANK = "(?:\\[[^]]+\\] )?"
    private const val NAME = "(\\w{1,16})"
    private const val RN = "$RANK$NAME"   // captures name in group 1, full match includes rank

    // ── Party join / leave ───────────────────────────────────────────
    private val invitedPattern = Regex("^${RN} invited ${RN} to the party! They have 60 seconds to accept\\.\$")
    private val joinedPartyPattern = Regex("^${RN} joined the party\\.\$")
    private val joinedSelfPattern = Regex("^You have joined ${RN}'s party!\$")
    private val partyingWithPattern = Regex("^You'll be partying with: (.+)\$")
    private val leftPartyPattern = Regex("^${RN} has left the party\\.\$")
    private val leftSelfPattern = Regex("^You left the party\\.\$")

    // ── Disband ──────────────────────────────────────────────────────
    private val disbandByPattern = Regex("^${RN} has disbanded the party!\$")
    private val disbandEmptyPattern = Regex("^The party was disbanded because all invites expired and the party was empty\\.\$")
    private val disbandLeaderOfflinePattern = Regex("^The party was disbanded because the party leader disconnected\\.\$")
    private val notInPartyPattern = Regex("^You are not currently in a party\\.\$")

    // ── Transfer / Promote ───────────────────────────────────────────
    private val transferByPattern = Regex("^The party was transferred to ${RN} by ${RN}\$")
    private val transferLeavePattern = Regex("^The party was transferred to ${RN} because ${RN} left\$")
    private val promotedToLeaderPattern = Regex("^${RN} has promoted ${RN} to Party Leader\$")
    private val promotedToModPattern = Regex("^${RN} is now a Party Moderator\$")

    // ── Kick ─────────────────────────────────────────────────────────
    private val kickedPattern = Regex("^${RN} has been removed from the party\\.\$")
    private val kickedSelfPattern = Regex("^You have been kicked from the party by ${RN}\\.?\$")
    private val kickedOfflinePattern = Regex("^Kicked ${RN} because they were offline\\.\$")
    private val kickedDisconnectedPattern = Regex("^${RN} was removed from your party because they disconnected\\.\$")

    // ── Disconnect / Reconnect ───────────────────────────────────────
    private val leaderDisconnectedPattern = Regex("^The party leader, ${RN} has disconnected, they have 5 minutes to rejoin before the party is disbanded\\.\$")
    private val leaderReconnectedPattern = Regex("^The party leader ${RN} has rejoined\\.\$")
    private val memberDisconnectedPattern = Regex("^${RN} has disconnected, they have 5 minutes to rejoin before they are removed from the party\\.\$")
    private val memberReconnectedPattern = Regex("^${RN} has rejoined\\.\$")

    // ── Party Finder ─────────────────────────────────────────────────
    private val pfQueuedPattern = Regex("^Party Finder > Your party has been queued in the (?:dungeon|party) finder!\$")
    private val pfJoinedPattern = Regex("^Party Finder > (\\w{1,16}) joined the (?:dungeon|kuudra) group!.*\$")
    private val pfRemovedPattern = Regex("^Party Finder > Your group has been removed from the party finder!\$")

    // ── Lobby join (triggers auto-update) ────────────────────────────
    private val joinedLobbyPattern = Regex("^${RN} joined the lobby!\$")

    // ── Guild invite ─────────────────────────────────────────────────
    private val guildInvitePattern = Regex("^Invited (\\d+) to your party!\$")

    // ── Other utilities ──────────────────────────────────────────────
    private val rankStripRegex = Regex("\\[.+?]\\s*")
    private val partySenderRegex = Regex("^Party > (?:\\[.+?] )?(.+?):")
    private val cancelPatterns = listOf(
        Regex("^Party > (?:\\[.+?] )?(.+?):"),
        Regex("^Guild > (?:\\[.+?] )?(.+?):"),
        Regex("^\\[\\d+\\] (?:\\[.+?] )?(.+?):"),
        Regex("^(?:\\[.+?] )?(.+?):")
    )

    fun init() {
        ClientReceiveMessageEvents.GAME.register { message: Component, _: Boolean ->
            handleMessage(message.string)
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Message dispatcher
    // ══════════════════════════════════════════════════════════════════
    private fun handleMessage(message: String) {
        val clean = message.replace(COLOR_CODE_REGEX, "")

        // ── Join / Leave ─────────────────────────────────────────
        invitedPattern.find(clean)?.let { m ->
            val inviter = m.groupValues[1]
            val invited = m.groupValues[2]
            val myName = mc.player?.name?.string ?: return
            if (!PartyUtils.isInParty) {
                PartyUtils.addMember(inviter)
                PartyUtils.partyLeader = inviter
                if (inviter.equals(myName, ignoreCase = true)) {
                    PartyUtils.addMember(myName)
                }
            } else {
                PartyUtils.addMember(inviter)
            }
            return
        }
        joinedPartyPattern.find(clean)?.let {
            PartyUtils.addMember(it.groupValues[1])
            return
        }
        joinedSelfPattern.find(clean)?.let {
            val leaderName = it.groupValues[1]
            PartyUtils.partyLeader = leaderName
            PartyUtils.addMember(leaderName)
            PartyUtils.addMember(mc.player?.name?.string ?: return)
            return
        }
        partyingWithPattern.find(clean)?.let { m ->
            m.groupValues[1].split(",").forEach { raw ->
                val name = raw.replace(rankStripRegex, "").trim()
                if (name.isNotEmpty()) PartyUtils.addMember(name)
            }
            return
        }
        leftPartyPattern.find(clean)?.let {
            PartyUtils.removeMember(it.groupValues[1])
            return
        }
        leftSelfPattern.find(clean)?.let {
            PartyUtils.disband()
            return
        }

        // ── Disband ──────────────────────────────────────────────
        disbandByPattern.find(clean)?.let {
            PartyUtils.disband()
            return
        }
        if (disbandEmptyPattern.containsMatchIn(clean) ||
            disbandLeaderOfflinePattern.containsMatchIn(clean) ||
            notInPartyPattern.containsMatchIn(clean)
        ) {
            PartyUtils.disband()
            return
        }

        // ── Transfer / Promote ───────────────────────────────────
        transferByPattern.find(clean)?.let { m ->
            val newLeader = m.groupValues[1]
            val oldLeader = m.groupValues[2]
            PartyUtils.partyLeader = newLeader
            PartyUtils.addMember(newLeader)
            PartyUtils.addMember(oldLeader)
            return
        }
        transferLeavePattern.find(clean)?.let { m ->
            val newLeader = m.groupValues[1]
            val leaver = m.groupValues[2]
            PartyUtils.partyLeader = newLeader
            PartyUtils.addMember(newLeader)
            PartyUtils.removeMember(leaver)
            return
        }
        promotedToLeaderPattern.find(clean)?.let { m ->
            val newLeader = m.groupValues[2]
            PartyUtils.partyLeader = newLeader
            PartyUtils.addMember(newLeader)
            return
        }
        promotedToModPattern.find(clean)?.let { m ->
            PartyUtils.addMember(m.groupValues[1])
            return
        }

        // ── Kick ─────────────────────────────────────────────────
        kickedPattern.find(clean)?.let {
            PartyUtils.removeMember(it.groupValues[1])
            return
        }
        kickedSelfPattern.find(clean)?.let {
            PartyUtils.disband()
            return
        }
        kickedOfflinePattern.find(clean)?.let {
            PartyUtils.removeMemberWithOffline(it.groupValues[1])
            return
        }
        kickedDisconnectedPattern.find(clean)?.let {
            PartyUtils.removeMemberWithOffline(it.groupValues[1])
            return
        }

        // ── Disconnect / Reconnect ───────────────────────────────
        leaderDisconnectedPattern.find(clean)?.let { m ->
            PartyUtils.markOffline(m.groupValues[1])
            PartyUtils.partyLeader = m.groupValues[1]
            return
        }
        leaderReconnectedPattern.find(clean)?.let { m ->
            PartyUtils.markOnline(m.groupValues[1])
            PartyUtils.partyLeader = m.groupValues[1]
            return
        }
        memberDisconnectedPattern.find(clean)?.let {
            PartyUtils.markOffline(it.groupValues[1])
            return
        }
        memberReconnectedPattern.find(clean)?.let {
            PartyUtils.markOnline(it.groupValues[1])
            return
        }

        // ── Party Finder ─────────────────────────────────────────
        pfQueuedPattern.find(clean)?.let {
            if (!PartyUtils.isInParty) {
                PartyUtils.isInParty = true
                AutoPartyListUpdater.refresh()
            }
            return
        }
        pfJoinedPattern.find(clean)?.let {
            PartyUtils.addMember(it.groupValues[1])
            return
        }
        pfRemovedPattern.find(clean)?.let {
            // Party Finder removal — no state change needed
            return
        }

        // ── Lobby join → refresh party list ──────────────────────
        joinedLobbyPattern.find(clean)?.let { m ->
            val name = m.groupValues[1]
            val myName = mc.player?.name?.string
            if (myName != null && name.equals(myName, ignoreCase = true)) {
                AutoPartyListUpdater.refresh()
            }
            return
        }

        // ── Guild invite → refresh party list ────────────────────
        guildInvitePattern.find(clean)?.let {
            AutoPartyListUpdater.refresh()
            return
        }

        // ── Auto-reply & cancel ──────────────────────────────────
        handleCancelCommand(clean)
        handleModCommand(message)
    }

    // ══════════════════════════════════════════════════════════════════
    // Auto-reply: !mod
    // ══════════════════════════════════════════════════════════════════
    private fun handleModCommand(message: String) {
        if (!SRConfig.settings.partyCommands.mod) return
        val cleanMessage = message.replace(COLOR_CODE_REGEX, "")
        if (!cleanMessage.startsWith("Party >")) return
        if (!cleanMessage.contains("!mod")) return

        val match = partySenderRegex.find(cleanMessage) ?: return
        val senderRaw = match.groupValues[1].trim()
        val senderClean = senderRaw.replace(rankStripRegex, "").trim()
        val myName = mc.player?.name?.string ?: return
        if (senderClean.equals(myName, ignoreCase = true)) return

        Scheduler.schedule(SRConfig.settings.partyCommands.autoReplyModDelayMs.toLong()) {
            mc.execute { sendPartyChat(Component.translatable("sraddons.chat.autoreply.mod").string) }
        }
        Scheduler.schedule(SRConfig.settings.partyCommands.autoReplyGithubDelayMs.toLong()) {
            mc.execute { sendPartyChat(Component.translatable("sraddons.chat.autoreply.github").string) }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Cancel countdown from party chat
    // ══════════════════════════════════════════════════════════════════
    private fun handleCancelCommand(cleanMessage: String) {
        if (!cleanMessage.contains("!cancel")) return
        val myName = mc.player?.name?.string ?: return
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
