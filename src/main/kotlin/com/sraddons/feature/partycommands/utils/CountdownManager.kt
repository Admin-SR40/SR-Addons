package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import kotlin.concurrent.thread

object CountdownManager {
    private val mc = Minecraft.getInstance()

    private var currentCountdown: Countdown? = null

    private val floorInstances = mapOf(
        "F1" to "catacombs_floor_one", "F2" to "catacombs_floor_two", "F3" to "catacombs_floor_three",
        "F4" to "catacombs_floor_four", "F5" to "catacombs_floor_five", "F6" to "catacombs_floor_six",
        "F7" to "catacombs_floor_seven", "M1" to "master_catacombs_floor_one",
        "M2" to "master_catacombs_floor_two", "M3" to "master_catacombs_floor_three",
        "M4" to "master_catacombs_floor_four", "M5" to "master_catacombs_floor_five",
        "M6" to "master_catacombs_floor_six", "M7" to "master_catacombs_floor_seven",
        "T1" to "kuudra_normal", "T2" to "kuudra_hot", "T3" to "kuudra_burning",
        "T4" to "kuudra_fiery", "T5" to "kuudra_infernal"
    )

    data class Countdown(
        val totalSeconds: Int,
        val label: String,
        var remainingSeconds: Int
    )

    fun startCountdown(seconds: Int, label: String = "Custom"): Boolean {
        if (seconds <= 0) return false
        currentCountdown = Countdown(seconds, label, seconds)
        val timeStr = formatTime(seconds)
        val displayLabel = if (label == "Custom") "Custom" else label

        if (label == "Custom") {
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - $displayLabel - Started: $timeStr")
            } else {
                modMessage(formatResponse("Countdown", "\u00a7aStarted: $timeStr ($displayLabel)", ""))
            }
        } else {
            if (PartyUtils.isInParty) {
                sendPartyChat("Queued for $displayLabel - entering in $timeStr")
                thread {
                    Thread.sleep(500)
                    sendPartyChat("Type !cancel to abort the queue")
                }
            } else {
                modMessage(formatResponse("Countdown", "\u00a7aStarted: $timeStr ($displayLabel)", ""))
                modMessage("\u00a77Type \u00a7c!clear \u00a77to stop the timer")
            }
        }
        return true
    }

    fun clearCountdown() {
        if (currentCountdown != null) {
            currentCountdown = null
            modMessage(formatResponse("Countdown", "\u00a7cCleared", ""))
        } else {
            modMessage(formatResponse("Countdown", "\u00a77No active countdown", ""))
        }
    }

    fun onTick() {
        val countdown = currentCountdown ?: return
        if (mc.player?.tickCount?.rem(20) != 0) return

        countdown.remainingSeconds--

        if (countdown.remainingSeconds <= 0) {
            playLevelUpSound()
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - ${countdown.label} - Time's up!")
            } else {
                modMessage(formatResponse("Countdown", "\u00a7a\u00a7lTime's up! (${countdown.label})", ""))
            }
            if (countdown.label != "Custom") {
                val instanceId = floorInstances[countdown.label]
                if (instanceId != null) {
                    sendCommand("joininstance $instanceId")
                    modMessage(formatResponse("Queue", "\u00a7eAuto-joining ${countdown.label}...", ""))
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
            countdown.label != "Custom" && remaining % 30 == 0 -> sendReminder(countdown)
            countdown.label == "Custom" -> checkCustomReminder(countdown)
        }
    }

    private fun sendReminder(countdown: Countdown) {
        val timeStr = formatTime(countdown.remainingSeconds)
        playCountdownSound()
        if (countdown.label == "Custom") {
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - Custom - $timeStr remaining")
            } else {
                modMessage(formatResponse("Countdown", "\u00a7e$timeStr \u00a77remaining", ""))
            }
        } else {
            val color = when {
                countdown.remainingSeconds <= 5 -> "\u00a7c"
                countdown.remainingSeconds <= 10 -> "\u00a76"
                else -> "\u00a7e"
            }
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - ${countdown.label} - $timeStr remaining")
            } else {
                modMessage(formatResponse("Countdown", "$color$timeStr \u00a77remaining (${countdown.label})", ""))
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
                sendPartyChat("Countdown - Custom - $timeStr remaining")
            } else {
                modMessage(formatResponse("Countdown", "\u00a7e$timeStr \u00a77remaining", ""))
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
        if (countdown.label == "Custom") return false
        currentCountdown = null
        sendPartyChat("Countdown cancelled by $playerName")
        return true
    }
}
