package com.sraddons.feature.partycommands.utils

import com.sraddons.util.Scheduler
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

object AutoPartyListUpdater {
    private val mc = Minecraft.getInstance()
    private var wasInGame = false
    private var hasDoneFirstUpdate = false
    private var lastUpdateTime = 0L
    private const val UPDATE_COOLDOWN = 60000L
    private const val INITIAL_DELAY_MS = 500L
    private const val UPDATE_DELAY_MS = 1500L

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            checkGameState()
        }
    }

    private fun checkGameState() {
        val isInGame = mc.player != null && mc.connection != null

        if (!wasInGame && isInGame && !hasDoneFirstUpdate) {
            Scheduler.schedule(INITIAL_DELAY_MS) {
                mc.execute {
                    if (shouldUpdate()) scheduleUpdate()
                }
            }
            hasDoneFirstUpdate = true
        }

        if (wasInGame && !isInGame) {
            hasDoneFirstUpdate = false
        }
        wasInGame = isInGame
    }

    private fun shouldUpdate(): Boolean {
        if (mc.isSingleplayer) return false
        return mc.connection != null
    }

    private fun scheduleUpdate() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < UPDATE_COOLDOWN) return
        lastUpdateTime = currentTime

        Scheduler.schedule(UPDATE_DELAY_MS) {
            mc.execute {
                if (mc.player != null && !mc.isSingleplayer) {
                    PartyListHandler.startAutoWaiting()
                    sendCommand("p list")
                }
            }
        }
    }

    fun refresh() {
        if (!mc.isSingleplayer && mc.player != null) {
            scheduleUpdate()
        }
    }
}
