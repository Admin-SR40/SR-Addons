package com.sraddons.feature.tooltip

import com.sraddons.config.SRConfig
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
    var collapsed: Boolean = false
) {
    var totalTextHeight: Int = 0
        private set
    var maxScroll: Int = 0
        private set
    private val basePadding = 4
    private val collapsedH = 14

    var expandedHeight: Int = 0
        private set
    var rawWidth: Int = 0
        private set

    private val scale: Float get() = SRConfig.settings.general.pinTooltipScale

    fun recalculate(font: Font) {
        val lineH = font.lineHeight + 2
        rawWidth = (lines.maxOfOrNull { font.width(it) } ?: 0) + basePadding * 2
        width = rawWidth
        totalTextHeight = lines.size * lineH
        val ratio = 0.6
        val maxVisible = (Minecraft.getInstance().window.guiScaledHeight * ratio).toInt().coerceAtLeast(lineH * 3)
        maxScroll = maxOf(0, totalTextHeight - maxVisible)
        val visibleH = minOf(totalTextHeight, maxVisible)
        expandedHeight = visibleH + basePadding * 2
        height = if (collapsed) collapsedH else expandedHeight
    }

    fun toggleCollapsed() {
        collapsed = !collapsed
        height = if (collapsed) collapsedH else expandedHeight
    }

    fun contains(mx: Double, my: Double): Boolean {
        val h = if (collapsed) collapsedH else (height * scale).toInt()
        return mx >= x && mx <= x + (width * scale) && my >= y && my <= y + h
    }

    fun scroll(amount: Double) {
        scrollOffset = (scrollOffset - amount.toInt()).coerceIn(0, maxScroll)
    }

    fun adjustScale(delta: Float) {
        SRConfig.update { it.general.pinTooltipScale = (it.general.pinTooltipScale + delta).coerceIn(0.5f, 3.0f) }
        SRConfig.save()
        recalculate(Minecraft.getInstance().font)
    }

    fun render(font: Font, extractor: GuiGraphicsExtractor) {
        val s = scale
        val xi = x.toInt()
        val yi = y.toInt()

        val pose = extractor.pose()
        pose.pushMatrix()
        val m = pose as org.joml.Matrix3x2f
        m.translate(xi.toFloat(), yi.toFloat())
        m.scale(s, s)
        m.translate(-xi.toFloat(), -yi.toFloat())

        if (collapsed) {
            val firstW = if (lines.isNotEmpty()) font.width(lines[0]) + 4 else font.width("…") + 4
            val barW = firstW.coerceIn(40, rawWidth)
            val barColor = 0xFF3A3A3A.toInt()
            extractor.fill(xi, yi, xi + barW, yi + collapsedH, barColor)
            if (lines.isNotEmpty()) extractor.text(font, lines[0], xi + 2, yi + 2, 0xFFFFFFFF.toInt())
            else extractor.text(font, Component.literal("…"), xi + 2, yi + 2, 0xFFFFFFFF.toInt())
            pose.popMatrix()
            return
        }

        val lineH = font.lineHeight + 2
        val padded = basePadding

        val bgColor = 0xC0100010.toInt()
        val borderTop = 0xFF6B3FA0.toInt()
        val borderSide = 0xFF25004B.toInt()

        extractor.fill(xi, yi, xi + width, yi + height, bgColor)
        extractor.fill(xi, yi, xi + width, yi + 1, borderTop)
        extractor.fill(xi, yi + height - 1, xi + width, yi + height, borderSide)
        extractor.fill(xi, yi, xi + 1, yi + height, borderSide)
        extractor.fill(xi + width - 1, yi, xi + width, yi + height, borderSide)

        val visibleLines = maxOf(1, (height - padded * 2) / lineH)
        val startLine = (scrollOffset / lineH).coerceAtLeast(0)
        val endLine = minOf(lines.size, startLine + visibleLines + 1)

        for (i in startLine until endLine) {
            val textY = yi + padded + (i - startLine) * lineH + 1
            extractor.text(font, lines[i], xi + padded, textY, 0xFFFFFFFF.toInt())
        }

        if (maxScroll > 0) {
            val barH = maxOf(20, ((height.toDouble() / totalTextHeight) * height).toInt())
            val barY = yi + ((scrollOffset.toDouble() / maxScroll) * (height - barH)).toInt()
            extractor.fill(xi + width - 2, barY, xi + width, barY + barH, 0x60FFFFFF.toInt())
        }

        pose.popMatrix()
    }
}
