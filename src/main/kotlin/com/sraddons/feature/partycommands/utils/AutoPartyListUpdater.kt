package com.sraddons.feature.partycommands.utils

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

object AutoPartyListUpdater {
    private val mc = Minecraft.getInstance()
    private var wasInGame = false
    private var hasDoneFirstUpdate = false
    private var lastUpdateTime = 0L
    private const val UPDATE_COOLDOWN = 60000L

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            checkGameState()
        }
    }

    private fun checkGameState() {
        val isInGame = mc.player != null && mc.connection != null

        if (!wasInGame && isInGame && !hasDoneFirstUpdate) {
            Thread {
                Thread.sleep(500)
                mc.execute {
                    if (shouldUpdate()) scheduleUpdate()
                }
            }.start()
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

        Thread {
            Thread.sleep(1500)
            mc.execute {
                if (mc.player != null && !mc.isSingleplayer) {
                    PartyListHandler.startAutoWaiting()
                    sendCommand("p list")
                }
            }
        }.start()
    }

    fun refresh() {
        if (!mc.isSingleplayer && mc.player != null) {
            scheduleUpdate()
        }
    }
}
