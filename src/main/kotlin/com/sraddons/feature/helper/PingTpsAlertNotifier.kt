package com.sraddons.feature.helper

import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.ServerUtils
import com.sraddons.util.TitleUtil
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object PingTpsAlertNotifier {

    private var lastCheckTime = 0L
    private var pingTriggeredMs = 0L
    private var tpsTriggeredMs = 0L
    private var pingFired = false
    private var tpsFired = false
    private var pingRecoveryMs = 0L
    private var tpsRecoveryMs = 0L
    private var worldJoinTime = 0L

    private const val RECOVERY_COOLDOWN_MS = 60_000L
    private const val WORLD_JOIN_GRACE_MS = 10_000L

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            tick()
        }
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            worldJoinTime = System.currentTimeMillis()
        }
    }

    private fun tick() {
        val mc = Minecraft.getInstance()
        if (mc.level == null || mc.player == null) return

        val now = System.currentTimeMillis()
        if (now - worldJoinTime < WORLD_JOIN_GRACE_MS) return

        if (lastCheckTime == 0L) {
            lastCheckTime = now
            return
        }
        val delta = now - lastCheckTime
        lastCheckTime = now

        val pingCfg = SRConfig.settings.helper.pingAlert
        val tpsCfg = SRConfig.settings.helper.tpsAlert

        if (pingCfg.enabled) tickPing(pingCfg, delta)
        if (tpsCfg.enabled) tickTps(tpsCfg, delta)
    }

    private fun tickPing(cfg: SRConfig.PingAlertConfigData, deltaMs: Long) {
        val pingHigh = ServerUtils.currentPing > cfg.threshold

        if (pingHigh) {
            if (pingFired) {
                pingRecoveryMs = 0L
            } else {
                pingTriggeredMs += deltaMs
                if (pingTriggeredMs >= cfg.delaySeconds * 1000L) {
                    fireAlert(cfg.message, cfg.playSound)
                    pingFired = true
                    pingTriggeredMs = 0L
                }
            }
        } else {
            if (pingFired) {
                pingRecoveryMs += deltaMs
                if (pingRecoveryMs >= RECOVERY_COOLDOWN_MS) {
                    pingFired = false
                    pingRecoveryMs = 0L
                }
            } else {
                pingTriggeredMs = 0L
            }
        }
    }

    private fun tickTps(cfg: SRConfig.TpsAlertConfigData, deltaMs: Long) {
        val tpsLow = ServerUtils.averageTps < cfg.threshold

        if (tpsLow) {
            if (tpsFired) {
                tpsRecoveryMs = 0L
            } else {
                tpsTriggeredMs += deltaMs
                if (tpsTriggeredMs >= cfg.delaySeconds * 1000L) {
                    fireAlert(cfg.message, cfg.playSound)
                    tpsFired = true
                    tpsTriggeredMs = 0L
                }
            }
        } else {
            if (tpsFired) {
                tpsRecoveryMs += deltaMs
                if (tpsRecoveryMs >= RECOVERY_COOLDOWN_MS) {
                    tpsFired = false
                    tpsRecoveryMs = 0L
                }
            } else {
                tpsTriggeredMs = 0L
            }
        }
    }

    private fun fireAlert(message: String, playSound: Boolean) {
        TitleUtil.showSubtitle(message)
        if (playSound) {
            val mc = Minecraft.getInstance()
            mc.soundManager.play(
                SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f)
            )
        }
    }
}
