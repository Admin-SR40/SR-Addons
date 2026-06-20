package com.sraddons.feature.helper

import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.COLOR_CODE_REGEX
import com.sraddons.util.TitleUtil
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import java.util.concurrent.ConcurrentHashMap

object ChatKeywordAlert {

    private val cooldowns = ConcurrentHashMap<String, Long>()

    fun init() {
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            if (!SRConfig.settings.chatAlert.enabled) return@register
            val text = message.string.replace(COLOR_CODE_REGEX, "").trim()

            for (entry in SRConfig.settings.chatAlert.entries) {
                val parts = entry.split(" | ", limit = 5)
                val keyword = parts.getOrElse(0) { "" }.trim()
                val subtitle = parts.getOrElse(1) { "" }.trim()
                val cooldownSec = parts.getOrElse(2) { "5" }.trim().toIntOrNull()?.coerceAtLeast(0) ?: 5
                val ignorePrefix = parts.getOrElse(3) { "yes" }.trim().equals("yes", ignoreCase = true)
                val ignoreSuffix = parts.getOrElse(4) { "yes" }.trim().equals("yes", ignoreCase = true)

                if (keyword.isEmpty() || subtitle.isEmpty()) continue

                val matched = when {
                    !ignorePrefix && !ignoreSuffix -> text.equals(keyword, ignoreCase = true)
                    !ignorePrefix -> text.startsWith(keyword, ignoreCase = true)
                    !ignoreSuffix -> text.endsWith(keyword, ignoreCase = true)
                    else -> text.contains(keyword, ignoreCase = true)
                }

                if (!matched) continue

                if (cooldownSec > 0) {
                    val now = System.currentTimeMillis()
                    val last = cooldowns[keyword] ?: 0L
                    if (now - last < cooldownSec * 1000L) continue
                    cooldowns[keyword] = now
                }

                TitleUtil.showSubtitle(subtitle)
                Minecraft.getInstance().soundManager.play(
                    SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f)
                )
                break
            }
        }
    }
}
