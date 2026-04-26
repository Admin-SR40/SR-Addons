package com.sraddons.feature.partycommands.utils

import net.minecraft.client.Minecraft

object ServerUtils {
    private val mc = Minecraft.getInstance()

    private val tpsHistory = mutableListOf<Double>()
    private var lastTime = System.currentTimeMillis()
    private var lastGameTime = 0L

    val currentPing: Int
        get() {
            val pingLog = mc.gui.debugOverlay.pingLogger
            return if (pingLog.size() > 0) pingLog.get(0).toInt() else 0
        }

    var averagePing: Int = 0
        private set

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
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastTime

        if (elapsed >= 1000) {
            val level = mc.level
            if (level != null) {
                val gameTime = level.gameTime
                if (lastGameTime != 0L) {
                    val timeDiff = gameTime - lastGameTime
                    val tps = (timeDiff * 1000.0 / elapsed).coerceAtMost(20.0)
                    tpsHistory.add(tps)
                    if (tpsHistory.size > 10) {
                        tpsHistory.removeAt(0)
                    }
                }
                lastGameTime = gameTime
            }
            lastTime = currentTime
        }
    }

    @JvmStatic
    fun onPongResponse(time: Long) {
        val pingLog = mc.gui.debugOverlay.pingLogger
        val sampleSize = minOf(pingLog.size(), 20)

        if (sampleSize == 0) {
            averagePing = 0
            return
        }

        var total = 0L
        for (i in 0 until sampleSize) {
            total += pingLog.get(i)
        }
        averagePing = (total / sampleSize).toInt()
    }
}
