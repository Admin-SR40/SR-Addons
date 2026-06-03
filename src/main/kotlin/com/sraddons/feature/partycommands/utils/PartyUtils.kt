package com.sraddons.feature.partycommands.utils

import net.minecraft.client.Minecraft

object PartyUtils {
    private val mc = Minecraft.getInstance()

    private val members = mutableListOf<String>()
    private val memberColors = mutableMapOf<String, String>()
    private val offlineMembers = mutableSetOf<String>()

    fun getMembers(): List<String> = synchronized(this) { members.toList() }

    private val RANK_PREFIXES = listOf(
        "[MVP++]", "[MVP+]", "[MVP]",
        "[VIP+]", "[VIP]",
        "[YOUTUBE]", "[ADMIN]", "[GM]", "[MOD]", "[HELPER]"
    )

    fun cleanPlayerName(name: String): String {
        var clean = name.noControlCodes
        for (prefix in RANK_PREFIXES) {
            clean = clean.replace(prefix, "")
        }
        return clean.replace("●", "").trim()
    }

    var partyLeader: String? = null
        @Synchronized internal set
        @Synchronized get

    var isInParty: Boolean = false
        @Synchronized internal set
        @Synchronized get

    fun isLeader(): Boolean = synchronized(this) {
        partyLeader == mc.player?.name?.string
    }

    fun addMember(playerName: String, coloredName: String? = null) {
        synchronized(this) {
            if (!isInParty) isInParty = true
            val cleanName = cleanPlayerName(playerName)
        if (cleanName.isEmpty()) return
        if (cleanName !in members) {
            members.add(cleanName)
        }
        if (coloredName != null) {
            memberColors[cleanName] = coloredName
        }
        }
    }

    fun removeMember(playerName: String) {
        synchronized(this) {
            val cleanName = playerName.noControlCodes
            if (cleanName !in members) return
            members.remove(cleanName)
            memberColors.remove(cleanName)
            if (members.isEmpty()) {
                disband()
            }
        }
    }

    fun disband() {
        synchronized(this) {
            members.clear()
        memberColors.clear()
        offlineMembers.clear()
        partyLeader = null
        isInParty = false
        }
    }

    fun markOffline(playerName: String) {
        synchronized(this) {
            offlineMembers.add(playerName.noControlCodes.lowercase())
        }
    }

    fun markOnline(playerName: String) {
        synchronized(this) {
            offlineMembers.remove(playerName.noControlCodes.lowercase())
        }
    }

    fun isOffline(playerName: String): Boolean = synchronized(this) {
        offlineMembers.contains(playerName.noControlCodes.lowercase())
    }

    fun removeMemberWithOffline(playerName: String) {
        synchronized(this) {
            val cleanName = playerName.noControlCodes
            members.remove(cleanName)
            memberColors.remove(cleanName)
            offlineMembers.remove(cleanName)
            if (members.isEmpty()) {
                disband()
            }
        }
    }

    fun findMember(partialName: String): String = synchronized(this) {
        members.find { it.contains(partialName, ignoreCase = true) } ?: partialName
    }

    fun getMemberWithColor(cleanName: String): String = synchronized(this) {
        memberColors[cleanName] ?: "\u00a77$cleanName"
    }

    fun reset() {
        disband()
    }
}
