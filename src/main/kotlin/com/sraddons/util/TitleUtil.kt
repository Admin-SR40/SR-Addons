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

    private val colorCodeRegex = Regex("&(&|[0-9a-fk-orA-FK-OR])")

    fun parseColorCodes(text: String): String {
        return colorCodeRegex.replace(text) { mr ->
            if (mr.groupValues[1] == "&") "&" else "§${mr.groupValues[1]}"
        }
    }
}
