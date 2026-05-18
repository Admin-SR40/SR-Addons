package com.sraddons.util

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object TitleUtil {
    fun showSubtitle(text: String, fadeIn: Int = 0, stay: Int = 20, fadeOut: Int = 0) {
        val mc = Minecraft.getInstance()
        mc.gui.setTimes(fadeIn, stay, fadeOut)
        mc.gui.setTitle(Component.empty())
        mc.gui.setSubtitle(Component.literal(parseColorCodes(text)))
    }

    fun parseColorCodes(text: String): String {
        val sb = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '&' && i + 1 < text.length) {
                val next = text[i + 1]
                if (next == '&') {
                    sb.append('&')
                    i += 2
                } else if (next in "0123456789abcdefklmnorABCDEFKLMNOR") {
                    sb.append('§').append(next)
                    i += 2
                } else {
                    sb.append('&')
                    i++
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}
