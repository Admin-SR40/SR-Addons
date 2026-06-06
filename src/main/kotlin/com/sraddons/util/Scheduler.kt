package com.sraddons.util

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Scheduler {
    private val executor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable).apply {
            name = "SR-Addons-Scheduler"
            isDaemon = true
        }
    }

    fun schedule(delayMs: Long, action: () -> Unit) {
        executor.schedule({
            try { action() } catch (e: Exception) {
                org.apache.logging.log4j.LogManager.getLogger("SR-Addons-Scheduler").error("Scheduled task failed", e)
            }
        }, delayMs, TimeUnit.MILLISECONDS)
    }

    fun <T> scheduleStaggered(items: List<T>, delayMs: Long, action: (T) -> Unit) {
        items.forEachIndexed { index, item ->
            executor.schedule({
                try { action(item) } catch (e: Exception) {
                    org.apache.logging.log4j.LogManager.getLogger("SR-Addons-Scheduler").error("Staggered task failed", e)
                }
            }, delayMs * (index + 1), TimeUnit.MILLISECONDS)
        }
    }
}
