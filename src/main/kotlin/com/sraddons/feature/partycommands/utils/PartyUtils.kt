package com.sraddons.feature.partycommands.utils

import net.minecraft.client.Minecraft

object PartyUtils {
    private val mc = Minecraft.getInstance()

    val members = mutableListOf<String>()
    private val memberColors = mutableMapOf<String, String>()
    private val offlineMembers = mutableSetOf<String>()

    var partyLeader: String? = null
        internal set

    var isInParty: Boolean = false
        internal set

    fun isLeader(): Boolean {
        return partyLeader == mc.player?.name?.string
    }

    fun addMember(playerName: String, coloredName: String? = null) {
        if (!isInParty) isInParty = true
        val cleanName = playerName.noControlCodes
            .replace("[MVP++]", "")
            .replace("[MVP+]", "")
            .replace("[MVP]", "")
            .replace("[VIP+]", "")
            .replace("[VIP]", "")
            .replace("[YOUTUBE]", "")
            .replace("[ADMIN]", "")
            .replace("[GM]", "")
            .replace("[MOD]", "")
            .replace("[HELPER]", "")
            .replace("\u25cf", "")
            .trim()
        if (cleanName.isEmpty()) return
        if (cleanName !in members) {
            members.add(cleanName)
        }
        if (coloredName != null) {
            memberColors[cleanName] = coloredName
        }
    }

    fun removeMember(playerName: String) {
        val cleanName = playerName.noControlCodes
        if (cleanName !in members) return
        members.remove(cleanName)
        memberColors.remove(cleanName)
        if (members.isEmpty()) {
            disband()
        }
    }

    fun disband() {
        members.clear()
        memberColors.clear()
        offlineMembers.clear()
        partyLeader = null
        isInParty = false
    }

    fun markOffline(playerName: String) {
        offlineMembers.add(playerName.noControlCodes.lowercase())
    }

    fun markOnline(playerName: String) {
        offlineMembers.remove(playerName.noControlCodes.lowercase())
    }

    fun isOffline(playerName: String): Boolean {
        return offlineMembers.contains(playerName.noControlCodes.lowercase())
    }

    fun removeMemberWithOffline(playerName: String) {
        val cleanName = playerName.noControlCodes
        members.remove(cleanName)
        memberColors.remove(cleanName)
        offlineMembers.remove(cleanName)
        if (members.isEmpty()) {
            disband()
        }
    }

    fun findMember(partialName: String): String {
        return members.find { it.contains(partialName, ignoreCase = true) } ?: partialName
    }

    fun getMemberWithColor(cleanName: String): String {
        return memberColors[cleanName] ?: "\u00a77$cleanName"
    }

    fun reset() {
        disband()
    }
}
