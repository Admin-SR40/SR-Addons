package com.sraddons.feature.partycommands.utils

import net.minecraft.client.Minecraft

object ServerUtils {
    private val mc = Minecraft.getInstance()

    private val tpsHistory = mutableListOf<Double>()
    private var lastTime = System.nanoTime()
    private var lastGameTime = 0L

    val currentPing: Int
        get() {
            val pingLog = mc.gui.debugOverlay.pingLogger
            val size = pingLog.size()
            return if (size > 0) pingLog.get(size - 1).toInt() else 0
        }

    val currentFps: Int
        get() = mc.fps

    val averageTps: Double
        get() {
            return if (tpsHistory.isNotEmpty()) {
                tpsHistory.average()
            } else {
                20.0
            }
        }

    fun updateTps() {
        val now = System.nanoTime()
        val elapsedNs = now - lastTime

        if (elapsedNs >= 1_000_000_000L) {
            // Ignore if elapsed exceeds 5s — likely paused or tabbed out
            if (elapsedNs > 5_000_000_000L) {
                lastTime = now
                return
            }
            val level = mc.level
            if (level != null) {
                val gameTime = level.gameTime
                if (lastGameTime != 0L) {
                    val timeDiff = gameTime - lastGameTime
                    // Skip if no server ticks arrived in this window
                    if (timeDiff > 0) {
                        val tps = (timeDiff * 1_000_000_000.0 / elapsedNs).coerceAtMost(20.0)
                        synchronized(this) {
                            tpsHistory.add(tps)
                            if (tpsHistory.size > 10) {
                                tpsHistory.removeAt(0)
                            }
                        }
                    }
                }
                lastGameTime = gameTime
            }
            lastTime = now
        }
    }

}
