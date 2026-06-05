package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import com.sraddons.util.Scheduler
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

object CountdownManager {
    private val mc = Minecraft.getInstance()
    private const val CANCEL_HINT_DELAY_MS = 500L

    private var currentCountdown: Countdown? = null

    private const val CUSTOM_LABEL = "Custom"

    private fun getInstanceId(label: String): String? = FloorData.INSTANCES[label.lowercase()]

    class Countdown(
        val totalSeconds: Int,
        val label: String,
        remainingSeconds: Int
    ) {
        var remainingSeconds = remainingSeconds
            internal set
    }

    fun startCountdown(seconds: Int, label: String = CUSTOM_LABEL): Boolean {
        if (seconds <= 0) return false
        currentCountdown = Countdown(seconds, label, seconds)
        val timeStr = formatTime(seconds)
        val displayLabel = if (label == CUSTOM_LABEL) CUSTOM_LABEL else label

        if (label == CUSTOM_LABEL) {
            if (PartyUtils.isInParty) {
                sendPartyChat(Component.translatable("sraddons.pc.countdown.started.party",
                    Component.literal(timeStr), Component.literal(displayLabel)).string)
            } else {
                modMessage(formatResponse(
                    cdLabel(),
                    Component.translatable("sraddons.pc.countdown.started", Component.literal("$timeStr ($displayLabel)")).withColor(0x55FF55)
                ))
            }
        } else {
            if (PartyUtils.isInParty) {
                sendPartyChat(Component.translatable("sraddons.pc.countdown.party.queued",
                    Component.literal(displayLabel), Component.literal(timeStr)).string)
                Scheduler.schedule(CANCEL_HINT_DELAY_MS) {
                    sendPartyChat(Component.translatable("sraddons.pc.countdown.party.cancel_hint").string)
                }
            } else {
                modMessage(formatResponse(
                    cdLabel(),
                    Component.translatable("sraddons.pc.countdown.started", Component.literal("$timeStr ($displayLabel)")).withColor(0x55FF55)
                ))
                modMessage(Component.translatable("sraddons.pc.cd.stop_hint", Component.literal("§c!clear")).withColor(0xAAAAAA))
            }
        }
        return true
    }

    fun clearCountdown() {
        if (currentCountdown != null) {
            currentCountdown = null
            modMessage(formatResponse(cdLabel(), Component.translatable("sraddons.pc.countdown.cleared").withColor(0xFF5555)))
        } else {
            modMessage(formatResponse(cdLabel(), Component.translatable("sraddons.pc.countdown.no_active").withColor(0xAAAAAA)))
        }
    }

    fun onTick() {
        val countdown = currentCountdown ?: return
        if (mc.player?.tickCount?.rem(20) != 0) return

        countdown.remainingSeconds--

        if (countdown.remainingSeconds <= 0) {
            playLevelUpSound()
            if (PartyUtils.isInParty) {
                sendPartyChat(Component.translatable("sraddons.pc.countdown.times_up",
                    Component.literal(countdown.label)).string)
            } else {
                modMessage(formatResponse(
                    cdLabel(),
                    Component.translatable("sraddons.pc.countdown.times_up", Component.literal(countdown.label)).withColor(0x55FF55)
                ))
            }
            if (countdown.label != CUSTOM_LABEL) {
                val instanceId = getInstanceId(countdown.label)
                if (instanceId != null) {
                    sendCommand("joininstance $instanceId")
                    modMessage(formatResponse(
                        Component.translatable("sraddons.pc.queue.label"),
                        Component.translatable("sraddons.pc.countdown.auto_join", Component.literal(countdown.label)).withColor(0xFFFF55)
                    ))
                }
            }
            currentCountdown = null
            return
        }

        val remaining = countdown.remainingSeconds
        val total = countdown.totalSeconds
        when {
            remaining <= 5 -> sendReminder(countdown)
            remaining == 10 -> sendReminder(countdown)
            countdown.label != CUSTOM_LABEL && remaining % 30 == 0 -> sendReminder(countdown)
            countdown.label == CUSTOM_LABEL -> checkCustomReminder(countdown)
        }
    }

    private fun sendReminder(countdown: Countdown) {
        val timeStr = formatTime(countdown.remainingSeconds)
        playCountdownSound()
        if (countdown.label == CUSTOM_LABEL) {
            if (PartyUtils.isInParty) {
                sendPartyChat(Component.translatable("sraddons.pc.countdown.party.remaining",
                    Component.literal("Custom"), Component.literal(timeStr)).string)
            } else {
                modMessage(formatResponse(
                    cdLabel(),
                    Component.translatable("sraddons.pc.cd.remaining", Component.literal(timeStr)).withColor(0xFFFF55)
                ))
            }
        } else {
            val color = when {
                countdown.remainingSeconds <= 5 -> 0xFF5555
                countdown.remainingSeconds <= 10 -> 0xFFAA00
                else -> 0xFFFF55
            }
            if (PartyUtils.isInParty) {
                sendPartyChat(Component.translatable("sraddons.pc.countdown.party.remaining",
                    Component.literal(countdown.label), Component.literal(timeStr)).string)
            } else {
                modMessage(formatResponse(
                    cdLabel(),
                    Component.translatable("sraddons.pc.countdown.remaining", Component.literal(timeStr), Component.literal(countdown.label)).withColor(color)
                ))
            }
        }
    }

    private fun playCountdownSound() {
        if (!SRConfig.settings.partyCommands.countdownSound) return
        mc.execute {
            mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        }
    }

    private fun playLevelUpSound() {
        if (!SRConfig.settings.partyCommands.countdownSound) return
        mc.execute {
            mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f))
        }
    }

    private fun checkCustomReminder(countdown: Countdown) {
        val remaining = countdown.remainingSeconds
        val total = countdown.totalSeconds
        if (remaining <= 10) return

        val shouldRemind = when {
            remaining > 7200 -> remaining % 3600 == 0
            remaining > 3600 -> remaining % 1800 == 0
            remaining > 1800 -> remaining % 1200 == 0
            remaining > 600 -> remaining % 600 == 0
            remaining > 120 -> remaining % 300 == 0
            else -> remaining % 30 == 0
        }
        if (shouldRemind) {
            val timeStr = formatTime(remaining)
            playCountdownSound()
            if (PartyUtils.isInParty) {
                sendPartyChat(Component.translatable("sraddons.pc.countdown.party.remaining",
                    Component.literal("Custom"), Component.literal(timeStr)).string)
            } else {
                modMessage(formatResponse(
                    cdLabel(),
                    Component.translatable("sraddons.pc.cd.remaining", Component.literal(timeStr)).withColor(0xFFFF55)
                ))
            }
        }
    }

    fun formatTime(seconds: Int): String = when {
        seconds >= 3600 -> {
            val hours = seconds / 3600
            val mins = (seconds % 3600) / 60
            val secs = seconds % 60
            if (mins > 0 || secs > 0) "${hours}h ${mins}m ${secs}s" else "${hours}h"
        }
        seconds >= 60 -> {
            val mins = seconds / 60
            val secs = seconds % 60
            if (secs > 0) "${mins}m ${secs}s" else "${mins}m"
        }
        else -> "${seconds}s"
    }

    fun parseTime(input: String): Int? {
        val trimmed = input.trim().lowercase().replace(" ", "")
        if (trimmed.isEmpty()) return null

        if (trimmed.all { it.isDigit() }) {
            val secs = trimmed.toIntOrNull() ?: return null
            return if (secs > 43200) null else secs
        }

        var totalSeconds = 0
        var currentNumber = StringBuilder()
        var hasUnit = false

        for (char in trimmed) {
            when (char) {
                'h', 'm', 's' -> {
                    if (currentNumber.isEmpty()) return null
                    val value = currentNumber.toString().toIntOrNull() ?: return null
                    if (value < 0) return null
                    totalSeconds += when (char) {
                        'h' -> value * 3600
                        'm' -> value * 60
                        's' -> value
                        else -> 0
                    }
                    currentNumber = StringBuilder()
                    hasUnit = true
                }
                in '0'..'9' -> currentNumber.append(char)
                else -> return null
            }
        }

        if (currentNumber.isNotEmpty()) {
            val value = currentNumber.toString().toIntOrNull() ?: return null
            if (value < 0) return null
            totalSeconds += value
        }

        if (!hasUnit && currentNumber.isEmpty()) return null
        return if (totalSeconds > 43200 || totalSeconds <= 0) null else totalSeconds
    }

    fun getCurrentCountdown(): Countdown? = currentCountdown

    fun tryCancelFromPartyChat(playerName: String): Boolean {
        val countdown = currentCountdown ?: return false
        if (countdown.label == CUSTOM_LABEL) return false
        currentCountdown = null
        sendPartyChat(Component.translatable("sraddons.pc.countdown.cancelled_by",
            Component.literal(playerName)).string)
        return true
    }

    private fun cdLabel() = Component.translatable("sraddons.pc.label.cd")
}
