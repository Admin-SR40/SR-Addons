package com.sraddons.feature.tooltip

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

class PinnedTooltip(
    val lines: List<Component>,
    var x: Double,
    var y: Double,
    var width: Int = 0,
    var height: Int = 0,
    var scrollOffset: Int = 0,
    var isDragging: Boolean = false,
    var dragStartX: Double = 0.0,
    var dragStartY: Double = 0.0,
    var scale: Float = 1.0f
) {
    var totalTextHeight: Int = 0
        private set
    var maxScroll: Int = 0
        private set
    private val basePadding = 4

    var rawWidth: Int = 0
        private set

    fun recalculate(font: Font) {
        val lineH = font.lineHeight + 2
        rawWidth = (lines.maxOfOrNull { font.width(it) } ?: 0) + basePadding * 2
        width = (rawWidth * scale).toInt()
        totalTextHeight = lines.size * lineH
        val maxVisible = (Minecraft.getInstance().window.guiScaledHeight * 0.6).toInt()
        maxScroll = maxOf(0, totalTextHeight - maxVisible)
        val visibleH = minOf(totalTextHeight, maxVisible)
        height = ((visibleH + basePadding * 2) * scale).toInt()
    }

    fun contains(mx: Double, my: Double): Boolean =
        mx >= x && mx <= x + width && my >= y && my <= y + height

    fun scroll(amount: Double) {
        scrollOffset = (scrollOffset - amount.toInt()).coerceIn(0, maxScroll)
    }

    fun adjustScale(delta: Double) {
        scale = (scale + delta.toFloat() * 0.1f).coerceIn(0.5f, 3.0f)
        recalculate(Minecraft.getInstance().font)
    }

    fun render(font: Font, extractor: GuiGraphicsExtractor) {
        val xi = x.toInt()
        val yi = y.toInt()
        val lineH = font.lineHeight + 2
        val padded = (basePadding * scale).toInt()

        val bgColor = 0xC0100010.toInt()
        val borderTop = 0xFF6B3FA0.toInt()
        val borderSide = 0xFF25004B.toInt()

        extractor.fill(xi, yi, xi + width, yi + height, bgColor)
        extractor.fill(xi, yi, xi + width, yi + 1, borderTop)
        extractor.fill(xi, yi + height - 1, xi + width, yi + height, borderSide)
        extractor.fill(xi, yi, xi + 1, yi + height, borderSide)
        extractor.fill(xi + width - 1, yi, xi + width, yi + height, borderSide)

        val scaledLineH = (lineH * scale).toInt().coerceAtLeast(lineH)
        val visibleLines = maxOf(1, (height - padded * 2) / scaledLineH)
        val startLine = (scrollOffset / lineH).coerceAtLeast(0)
        val endLine = minOf(lines.size, startLine + visibleLines + 1)

        for (i in startLine until endLine) {
            val textY = yi + padded + (i - startLine) * scaledLineH + 1
            extractor.text(font, lines[i], xi + padded, textY, 0xFFFFFFFF.toInt())
        }

        if (maxScroll > 0) {
            val barH = maxOf(20, ((height.toDouble() / totalTextHeight) * height).toInt())
            val barY = yi + ((scrollOffset.toDouble() / maxScroll) * (height - barH)).toInt()
            extractor.fill(xi + width - 2, barY, xi + width, barY + barH, 0x60FFFFFF.toInt())
        }
    }
}
