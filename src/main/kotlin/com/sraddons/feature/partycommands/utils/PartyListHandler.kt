package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import net.minecraft.network.chat.Component

object PartyListHandler {

    var isWaitingForList = false
    private var silentMode = false
    private var waitTicks = 0
    private val collectedLines = mutableListOf<String>()
    private var lastMessageWasNotInParty = false
    private var shouldInterceptNextSeparator = false

    private val notInPartyPattern = Regex("^You are not currently in a party\\.$")
    private val partySizePattern = Regex("^Party Members \\((\\d+)\\)$")
    private val leaderPattern = Regex("^Party Leader: (.+)$")
    private val membersPattern = Regex("^Party Members: (.+)$")
    private val memberWithBulletPattern = Regex("((?:\\[.+?])?\\s*[a-zA-Z0-9_]+)(?:\\s*(§[0-9a-f])?●)?")
    private val separatorLinePattern = Regex("^[\\-▬─══=\\s]+\$")

    private val interceptSeparatorPatterns = listOf(
        Regex("^Party Finder > .+ joined the dungeon group!"),
        Regex("^\\[.+?] .+ joined the party\\.$")
    )

    fun startWaiting() {
        synchronized(this) {
            isWaitingForList = true
            silentMode = false
            waitTicks = 0
            collectedLines.clear()
            lastMessageWasNotInParty = false
        }
    }

    fun startAutoWaiting() {
        synchronized(this) {
            isWaitingForList = true
            silentMode = true
            waitTicks = 0
            collectedLines.clear()
            lastMessageWasNotInParty = false
        }
    }

    fun onTick() {
        synchronized(this) {
            if (!isWaitingForList) return
            waitTicks++
            if (waitTicks > 40) {
                val wasSilent = silentMode
                isWaitingForList = false
                lastMessageWasNotInParty = false
                silentMode = false
                    if (!wasSilent) {
                    modMessage(formatResponse(
                        Component.translatable("sraddons.pc.party_list.title"),
                        Component.translatable("sraddons.pc.party_list.timeout").withColor(0xFF5555)
                    ))
                }
            }
        }
    }

    fun handleMessage(text: String): Boolean {
        synchronized(this) {
        val trimmed = text.trim()

        for (pattern in interceptSeparatorPatterns) {
            if (pattern.containsMatchIn(trimmed)) {
                shouldInterceptNextSeparator = true
                return false
            }
        }

        if (shouldInterceptNextSeparator && isSeparatorLine(trimmed)) {
            shouldInterceptNextSeparator = false
            if (SRConfig.settings.general.removeSeparator) {
                    return true
            }
        }

        if (!isWaitingForList && isSeparatorLine(trimmed)) {
            if (SRConfig.settings.general.removeSeparator) {
                    return true
            }
            return false
        }

        if (!isWaitingForList) {
            return false
        }

        if (isSeparatorLine(trimmed)) {
            if (lastMessageWasNotInParty) {
                isWaitingForList = false
                lastMessageWasNotInParty = false
                    return true
            }
            if (collectedLines.isEmpty()) {
                return true
            } else {
                if (silentMode) {
                    parseSilently()
                    silentMode = false
                } else {
                    parseAndDisplay()
                }
                isWaitingForList = false
                    return true
            }
        }

        if (notInPartyPattern.matches(trimmed)) {
            lastMessageWasNotInParty = true
            PartyUtils.disband()
            if (!silentMode) {
                modMessage(formatResponse(
                    Component.translatable("sraddons.pc.party_list.title"),
                    Component.translatable("sraddons.pc.party_list.not_in_party").withColor(0xFF5555)
                ))
            } else {
                silentMode = false
            }
            return true
        }

        if (collectedLines.isNotEmpty() || trimmed.startsWith("Party Members")) {
            collectedLines.add(trimmed)
            return true
        }

        return false
        }
    }

    private fun parseAndDisplay() {
        var leader: String? = null
        val members = mutableListOf<String>()
        var memberCount = 0

        for (line in collectedLines) {
            partySizePattern.find(line)?.let { memberCount = it.groupValues[1].toInt(); return@let }
            leaderPattern.find(line)?.let {
                val leaderName = it.groupValues[1]
                leader = formatMember(leaderName)
                val cleanName = PartyUtils.cleanPlayerName(leaderName)
                PartyUtils.partyLeader = cleanName
                PartyUtils.addMember(cleanName, leaderName)
                return@let
            }
            membersPattern.find(line)?.let { match ->
                val membersText = match.groupValues[1]
                membersText.split("\u25cf").forEach { member ->
                    val memberFull = member.trim()
                    if (memberFull.isNotEmpty()) {
                        val formatted = formatMember(memberFull)
                        members.add(formatted)
                        val cleanName = PartyUtils.cleanPlayerName(memberFull)
                        PartyUtils.addMember(cleanName, memberFull)
                    }
                }
            }
        }
        displayResult(leader, members, memberCount)
    }

    private fun parseSilently() {
        for (line in collectedLines) {
            leaderPattern.find(line)?.let {
                val leaderName = it.groupValues[1]
                val cleanName = PartyUtils.cleanPlayerName(leaderName)
                PartyUtils.partyLeader = cleanName
                PartyUtils.addMember(cleanName, leaderName)
                return@let
            }
            membersPattern.find(line)?.let { match ->
                val membersText = match.groupValues[1]
                memberWithBulletPattern.findAll(membersText).forEach { memberMatch ->
                    val memberName = memberMatch.groupValues[1].trim()
                    val bulletColor = memberMatch.groupValues[2]
                    if (memberName.isEmpty()) return@forEach
                    val cleanName = PartyUtils.cleanPlayerName(memberName)
                    if (cleanName.isEmpty()) return@forEach
                    PartyUtils.addMember(cleanName, memberName)
                    if (bulletColor == "\u00a7c") {
                        PartyUtils.markOffline(cleanName)
                    } else {
                        PartyUtils.markOnline(cleanName)
                    }
                }
            }
        }
    }

    private fun formatMember(text: String): String {
        val cleaned = text.replace("\u25cf", "").trim()
        val rankPattern = Regex("^(\\[.+?])?\\s*(.+?)$")
        val match = rankPattern.find(cleaned) ?: return "\u00a77$cleaned"
        val rank = match.groupValues[1]
        val name = match.groupValues[2]
        val nameColor = when {
            rank.contains("YOUTUBE") || rank.contains("ADMIN") -> "\u00a7c"
            rank.contains("MVP++") -> "\u00a76"
            rank.contains("MVP+") || rank.contains("MVP") -> "\u00a7b"
            rank.contains("VIP+") || rank.contains("VIP") -> "\u00a7a"
            else -> "\u00a77"
        }
        return "$nameColor$name"
    }

    private fun displayResult(leader: String?, members: List<String>, count: Int) {
        val mc = net.minecraft.client.Minecraft.getInstance()
        val myName = mc.player?.name?.string ?: ""
        val isLeader = leader?.noControlCodes == myName

        modMessage(Component.literal("§b§l").append(Component.translatable("sraddons.pc.party_list.title")))

        if (leader != null) {
            val leaderClean = leader.noControlCodes
            val isLeaderOffline = PartyUtils.isOffline(leaderClean)
            val offlineText = Component.translatable("sraddons.pc.party_list.offline").withColor(0xFF5555)
            val displayLeader = if (isLeaderOffline) "$leader §c(${offlineText.string})" else leader
            rawMessage(Component.literal("§e§l").append(Component.translatable("sraddons.pc.party_list.leader"))
                .append(Component.literal(":")))
            rawMessage(Component.literal(" §7- $displayLeader"))
        } else {
            rawMessage(Component.literal("§e§l")
                .append(Component.translatable("sraddons.pc.party_list.leader"))
                .append(Component.literal(": §7"))
                .append(Component.translatable("sraddons.pc.party_list.unknown")))
        }

        val otherMembers = members.filter {
            val memberClean = it.noControlCodes
            val isLdr = leader != null && memberClean.equals(leader.noControlCodes, ignoreCase = true)
            val isSelf = memberClean.equals(myName, ignoreCase = true)
            !isLdr && !isSelf
        }.toMutableList()

        if (!isLeader && myName.isNotEmpty()) {
            otherMembers.add(0, "§d${Component.translatable("sraddons.pc.party_list.you").string}")
        }

        val totalMembers = otherMembers.size
        val onlineMembers = otherMembers.count { member ->
            val memberClean = member.noControlCodes
            memberClean == myName || !PartyUtils.isOffline(memberClean)
        }
        val offlineMembers = totalMembers - onlineMembers

        val membersCountStr = if (offlineMembers > 0) {
            "§7(§a$onlineMembers§7/§f$totalMembers§7)"
        } else {
            "§7(§f$totalMembers§7)"
        }
        rawMessage(Component.literal("§e§l").append(Component.translatable("sraddons.pc.party_list.members"))
            .append(Component.literal(" $membersCountStr:")))

        if (otherMembers.isEmpty()) {
            rawMessage(Component.literal(" §7- §c").append(Component.translatable("sraddons.pc.party_list.none")))
        } else {
            for (member in otherMembers) {
                val memberClean = member.noControlCodes
                val isOffline = PartyUtils.isOffline(memberClean) && memberClean != myName
                val offlineText = Component.translatable("sraddons.pc.party_list.offline").string
                val displayMember = if (isOffline) "$member §c($offlineText)" else member
                rawMessage(Component.literal(" §7- $displayMember"))
            }
        }
    }

    private fun isSeparatorLine(text: String): Boolean {
        return text.matches(separatorLinePattern)
    }
}
