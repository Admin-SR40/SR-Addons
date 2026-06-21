package com.sraddons.feature.tooltip

import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.CommandKeyBinding
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

object PinnedTooltipManager {

    val tooltips = mutableListOf<PinnedTooltip>()
    @Volatile
    var captureNext = false

    private val pinKey = KeyMapping(
        "key.sraddons.pin_tooltip",
        -1,
        CommandKeyBinding.CATEGORY_SR_ADDONS
    )

    fun init() {
        KeyMappingHelper.registerKeyMapping(pinKey)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!SRConfig.settings.general.pinTooltip) return@register

            val dragging = tooltips.firstOrNull { it.isDragging }
            if (dragging != null) {
                val mouse = client.mouseHandler
                val win = client.window
                dragging.x = mouse.getScaledXPos(win) - dragging.dragStartX
                dragging.y = mouse.getScaledYPos(win) - dragging.dragStartY
            }
        }

        ScreenEvents.AFTER_INIT.register { _: Minecraft, screen: Screen, _: Int, _: Int ->
            ScreenEvents.remove(screen).register { _ ->
                captureNext = false
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, event ->
                if (SRConfig.settings.general.pinTooltip && pinKey.matches(event) && screen is net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<*>) {
                    captureNext = true
                }
                true
            }

            ScreenEvents.afterExtract(screen).register { _, extractor, _, _, _ ->
                if (!SRConfig.settings.general.pinTooltip) return@register
                val font = Minecraft.getInstance().font
                for (tt in tooltips) {
                    tt.render(font, extractor)
                }
            }

            ScreenMouseEvents.allowMouseClick(screen).register { _, event ->
                if (!SRConfig.settings.general.pinTooltip || tooltips.isEmpty()) return@register true
                val hit = tooltips.findLast { it.contains(event.x, event.y) } ?: return@register true

                if (event.buttonInfo.button == 2) {
                    hit.toggleCollapsed()
                    return@register false
                }
                if (event.buttonInfo.button == 1) {
                    tooltips.remove(hit)
                    return@register false
                }
                if (event.buttonInfo.button == 0) {
                    hit.isDragging = true
                    val mouse = Minecraft.getInstance().mouseHandler
                    val win = Minecraft.getInstance().window
                    hit.dragStartX = mouse.getScaledXPos(win) - hit.x
                    hit.dragStartY = mouse.getScaledYPos(win) - hit.y
                    return@register false
                }
                true
            }

            ScreenMouseEvents.afterMouseRelease(screen).register { _, _, _ ->
                tooltips.forEach { it.isDragging = false }
                true
            }

            ScreenMouseEvents.allowMouseScroll(screen).register { _, mx, my, _, scrollY ->
                if (!SRConfig.settings.general.pinTooltip) return@register true
                val hit = tooltips.findLast { it.contains(mx, my) } ?: return@register true

                val ctrl = GLFW.glfwGetKey(Minecraft.getInstance().window.handle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                           GLFW.glfwGetKey(Minecraft.getInstance().window.handle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS
                val shift = GLFW.glfwGetKey(Minecraft.getInstance().window.handle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                            GLFW.glfwGetKey(Minecraft.getInstance().window.handle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS

                when {
                    ctrl -> hit.adjustScale(scrollY.toFloat() * 0.1f)
                    shift -> hit.scroll(scrollY * 10.0)
                    else -> hit.scroll(scrollY * 40.0)
                }
                return@register false
            }
        }
    }

    fun capture(lines: List<Component>) {
        if (!captureNext) return
        captureNext = false

        val font = Minecraft.getInstance().font
        val mc = Minecraft.getInstance()
        val screenH = mc.window.guiScaledHeight

        tooltips.clear()

        val tt = PinnedTooltip(lines.toList(), 0.0, 0.0)
        tt.recalculate(font)

        tt.x = 8.0
        tt.y = ((screenH - tt.height) / 2.0).coerceAtLeast(8.0)

        tooltips.add(tt)
    }
}
