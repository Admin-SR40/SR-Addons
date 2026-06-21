package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import com.sraddons.gui.SRConfigGui
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.network.chat.Component
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.resources.Identifier
import com.mojang.blaze3d.platform.InputConstants
import org.lwjgl.glfw.GLFW

object CommandKeyBinding {

    private lateinit var commandKey: KeyMapping
    private lateinit var guiKey: KeyMapping
    private lateinit var toggleKey: KeyMapping

    val CATEGORY_SR_ADDONS = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("sraddons", "general")
    )

    fun init() {
        commandKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.sraddons.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                CATEGORY_SR_ADDONS
            )
        )

        guiKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.sraddons.gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_SR_ADDONS
            )
        )

        toggleKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.sraddons.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_SR_ADDONS
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (commandKey.consumeClick()) {
                val prefix = SRConfig.settings.partyCommands.prefix
                client.setScreen(ChatScreen(prefix, false))
            }

            while (guiKey.consumeClick()) {
                SRConfigGui.open()
                modMessage(Component.translatable("sraddons.command.gui.opening").withColor(0x55FF55))
            }

            while (toggleKey.consumeClick()) {
                SRConfig.settings.partyCommands.enabled = !SRConfig.settings.partyCommands.enabled
                SRConfig.save()
                val statusKey = if (SRConfig.settings.partyCommands.enabled) "sraddons.key.toggle.enabled" else "sraddons.key.toggle.disabled"
                val statusColor = if (SRConfig.settings.partyCommands.enabled) 0x55FF55 else 0xFF5555
                modMessage(
                    Component.literal("§e")
                        .append(Component.translatable("sraddons.key.toggle.status"))
                        .append(Component.literal(" §7is now "))
                        .append(Component.translatable(statusKey).withColor(statusColor))
                )
            }
        }
    }
}
