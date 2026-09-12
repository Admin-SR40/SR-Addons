package com.sraddons.feature.partycommands.utils

/**
 * Server tick rate tracker, following the same approach as SkyHanni's TPS counter.
 *
 * Hypixel sends one `ClientboundPingPacket` per server tick. Measuring the wall-clock
 * time between two of those packets and averaging the last [maxSamples] values gives a
 * TPS reading that does not depend on the client frame rate, unlike counting ticks per
 * elapsed client time.
 *
 * All methods are safe to call from any thread: packets arrive on the network thread
 * while commands read the value from the client thread.
 */
class TickRateTracker(
    private val maxSamples: Int = 100,
    private val staleAfterNanos: Long = 1_000_000_000L,
    private val worldSwitchDelayNanos: Long = 5_000_000_000L
) {
    private val tickDurationsMs = ArrayDeque<Double>()
    private var lastTickNanos = 0L
    private var lastPingId: Int? = null
    private var worldSwitchNanos = 0L

    /**
     * Records one server tick. Duplicate ping ids are ignored, because the same ping
     * packet can be observed more than once while it is being handled.
     */
    fun onServerTick(pingId: Int, nowNanos: Long = System.nanoTime()) {
        synchronized(this) {
            if (lastPingId == pingId) return
            lastPingId = pingId

            if (lastTickNanos != 0L) {
                tickDurationsMs.addLast((nowNanos - lastTickNanos) / 1_000_000.0)
                while (tickDurationsMs.size > maxSamples) tickDurationsMs.removeFirst()
            }
            lastTickNanos = nowNanos
        }
    }

    /** Called when a new world is joined so no stale data from the previous one is kept. */
    fun onWorldChange(nowNanos: Long = System.nanoTime()) {
        synchronized(this) {
            tickDurationsMs.clear()
            lastTickNanos = 0L
            lastPingId = null
            worldSwitchNanos = nowNanos
        }
    }

    /** Current TPS, or [CALCULATING] while the world switch delay has not elapsed yet. */
    fun tps(nowNanos: Long = System.nanoTime()): Double = synchronized(this) { computeTps(nowNanos) }

    /** Seconds until [tps] stops returning [CALCULATING]; 0 when a value is available. */
    fun calculatingRemainingSeconds(nowNanos: Long = System.nanoTime()): Int {
        synchronized(this) {
            val remaining = worldSwitchDelayNanos - (nowNanos - worldSwitchNanos)
            if (remaining <= 0L) return 0
            return ((remaining + 999_999_999L) / 1_000_000_000L).toInt()
        }
    }

    /** True while fresh tick data is available (a tick arrived less than a second ago). */
    fun hasFreshData(nowNanos: Long = System.nanoTime()): Boolean = synchronized(this) {
        tickDurationsMs.isNotEmpty() && nowNanos - lastTickNanos < staleAfterNanos
    }

    private fun computeTps(nowNanos: Long): Double {
        if (worldSwitchNanos != 0L && nowNanos - worldSwitchNanos < worldSwitchDelayNanos) return CALCULATING
        // No ticks for a while: the server is not ticking this world (limbo, disconnected, …).
        if (tickDurationsMs.isEmpty() || nowNanos - lastTickNanos >= staleAfterNanos) return 0.0

        val averageMsPerTick = tickDurationsMs.average()
        if (averageMsPerTick <= 0.0) return 0.0
        return (1000.0 / averageMsPerTick).coerceIn(0.0, 20.0)
    }

    companion object {
        /** Returned by [tps] while there is not enough data yet. */
        const val CALCULATING = -1.0
    }
}
