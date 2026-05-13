package com.sraddons.util

import net.minecraft.network.chat.Component
import java.awt.Color

object GradientText {

    private fun create(start: Color, end: Color, text: String): Component {
        val length = maxOf(text.length, 1)
        var result = Component.literal("")

        for (i in text.indices) {
            val percent = i.toDouble() / maxOf(length - 1, 1)
            val r = (start.red * (1 - percent) + end.red * percent).toInt().coerceIn(0, 255)
            val g = (start.green * (1 - percent) + end.green * percent).toInt().coerceIn(0, 255)
            val b = (start.blue * (1 - percent) + end.blue * percent).toInt().coerceIn(0, 255)

            val color = (r shl 16) or (g shl 8) or b
            result = result.append(Component.literal(text[i].toString()).withColor(color))
        }

        return result
    }

    fun goldToYellow(text: String): Component = create(Color(255, 170, 0), Color(255, 255, 85), text)
    fun aquaToGreen(text: String): Component = create(Color(85, 255, 255), Color(85, 255, 85), text)
    fun cyanToLightBlue(text: String): Component = create(Color(135, 206, 250), Color(0, 100, 160), text)
    fun redToOrange(text: String): Component = create(Color(255, 85, 85), Color(255, 170, 0), text)
    fun purpleToPink(text: String): Component = create(Color(170, 0, 170), Color(255, 85, 255), text)
}
