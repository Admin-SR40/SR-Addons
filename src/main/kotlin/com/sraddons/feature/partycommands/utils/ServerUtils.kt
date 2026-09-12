package com.sraddons.feature.partycommands.utils

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft

object ServerUtils {
    private val mc = Minecraft.getInstance()
    private val tickRate = TickRateTracker()

    val currentPing: Int
        get() {
            val pingLog = mc.debugOverlay.pingLogger
            val size = pingLog.size()
            return if (size > 0) pingLog.get(size - 1).toInt() else 0
        }

    val currentFps: Int
        get() = mc.fps

    /**
     * Server TPS, or a negative value while the tracker is still calculating.
     *
     * A value of `0.0` means no server ticks arrived for at least a second (limbo,
     * connection loss, …), matching SkyHanni's behaviour.
     */
    val currentTps: Double
        get() = tickRate.tps()

    /** True while the tracker has tick data that is less than a second old. */
    val hasFreshTps: Boolean
        get() = tickRate.hasFreshData()

    /** Seconds until [currentTps] stops being negative; 0 when a value is available. */
    fun tpsCalculatingSeconds(): Int = tickRate.calculatingRemainingSeconds()

    /**
     * Called once per received [net.minecraft.network.protocol.common.ClientboundPingPacket]
     * — that is, once per server tick. Invoked from the network thread.
     */
    fun onServerTick(pingId: Int) {
        tickRate.onServerTick(pingId)
    }

    fun init() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> tickRate.onWorldChange() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> tickRate.onWorldChange() }
    }
}
