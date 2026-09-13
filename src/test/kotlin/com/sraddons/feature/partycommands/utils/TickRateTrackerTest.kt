package com.sraddons.feature.partycommands.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TickRateTrackerTest {
    private val base = 1_000_000_000_000L
    private val ms = 1_000_000L

    /** A tracker whose world-switch delay has already elapsed. */
    private fun readyTracker(): TickRateTracker =
        TickRateTracker().also {
            it.onWorldChange(base - 10_000 * ms)
        }

    @Test
    fun `reports 20 tps for 50 ms ticks`() {
        val tracker = readyTracker()
        var now = base
        repeat(20) { tick ->
            tracker.onServerTick(tick, now)
            now += 50 * ms
        }
        assertEquals(20.0, tracker.tps(now), 0.001)
    }

    @Test
    fun `reports 10 tps for 100 ms ticks`() {
        val tracker = readyTracker()
        var now = base
        repeat(20) { tick ->
            tracker.onServerTick(tick, now)
            now += 100 * ms
        }
        assertEquals(10.0, tracker.tps(now), 0.001)
    }

    @Test
    fun `clamps the value to 20 tps when ticks arrive faster than expected`() {
        val tracker = readyTracker()
        var now = base
        repeat(10) { tick ->
            tracker.onServerTick(tick, now)
            now += 25 * ms
        }
        assertEquals(20.0, tracker.tps(now), 0.001)
    }

    @Test
    fun `ignores duplicate ping ids`() {
        val tracker = readyTracker()
        tracker.onServerTick(1, base)
        tracker.onServerTick(1, base + 50 * ms)
        tracker.onServerTick(2, base + 100 * ms)

        // Only one duration was recorded: 100 ms between the two distinct ticks → 10 TPS.
        assertEquals(10.0, tracker.tps(base + 100 * ms), 0.001)
    }

    @Test
    fun `reports zero when no tick arrived for a second`() {
        val tracker = readyTracker()
        tracker.onServerTick(1, base)
        tracker.onServerTick(2, base + 50 * ms)

        assertEquals(0.0, tracker.tps(base + 1_500 * ms), 0.001)
        assertFalse(tracker.hasFreshData(base + 1_500 * ms))
        assertTrue(tracker.hasFreshData(base + 100 * ms))
    }

    @Test
    fun `calculates for five seconds after a world change`() {
        val tracker = TickRateTracker()
        tracker.onWorldChange(base)

        // One tick every 50 ms so data is always fresh when the delay elapses.
        var now = base
        var id = 0
        while (now <= base + 5_000 * ms) {
            tracker.onServerTick(id++, now)
            now += 50 * ms
        }

        assertEquals(TickRateTracker.CALCULATING, tracker.tps(base + 1_000 * ms), 0.001)
        assertEquals(4, tracker.calculatingRemainingSeconds(base + 1_000 * ms))
        assertEquals(1, tracker.calculatingRemainingSeconds(base + 4_500 * ms))
        assertEquals(0, tracker.calculatingRemainingSeconds(base + 5_000 * ms))
        assertEquals(20.0, tracker.tps(base + 5_000 * ms), 0.001)
    }
}
