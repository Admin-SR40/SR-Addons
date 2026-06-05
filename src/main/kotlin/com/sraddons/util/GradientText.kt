package com.sraddons.util

import net.minecraft.network.chat.Component

object GradientText {

    private fun create(start: Int, end: Int, text: String): Component {
        val length = maxOf(text.length, 1)
        var result = Component.literal("")

        val sr = (start shr 16) and 0xFF
        val sg = (start shr 8) and 0xFF
        val sb = start and 0xFF
        val er = (end shr 16) and 0xFF
        val eg = (end shr 8) and 0xFF
        val eb = end and 0xFF

        for (i in text.indices) {
            val percent = i.toDouble() / maxOf(length - 1, 1)
            val r = (sr * (1 - percent) + er * percent).toInt().coerceIn(0, 255)
            val g = (sg * (1 - percent) + eg * percent).toInt().coerceIn(0, 255)
            val b = (sb * (1 - percent) + eb * percent).toInt().coerceIn(0, 255)

            val color = (r shl 16) or (g shl 8) or b
            result = result.append(Component.literal(text[i].toString()).withColor(color))
        }

        return result
    }

    private fun rgb(r: Int, g: Int, b: Int) = (r shl 16) or (g shl 8) or b

    fun goldToYellow(text: String): Component = create(rgb(255, 170, 0), rgb(255, 255, 85), text)
    fun aquaToGreen(text: String): Component = create(rgb(85, 255, 255), rgb(85, 255, 85), text)
    fun cyanToLightBlue(text: String): Component = create(rgb(135, 206, 250), rgb(0, 100, 160), text)
    fun redToOrange(text: String): Component = create(rgb(255, 85, 85), rgb(255, 170, 0), text)
    fun purpleToPink(text: String): Component = create(rgb(170, 0, 170), rgb(255, 85, 255), text)
}
