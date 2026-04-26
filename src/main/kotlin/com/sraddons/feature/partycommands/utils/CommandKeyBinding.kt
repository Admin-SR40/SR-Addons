package com.sraddons.feature.partycommands.utils

import com.sraddons.config.SRConfig
import com.sraddons.gui.SRConfigGui
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.resources.Identifier
import com.mojang.blaze3d.platform.InputConstants
import org.lwjgl.glfw.GLFW

object CommandKeyBinding {

    private lateinit var commandKey: KeyMapping
    private lateinit var guiKey: KeyMapping
    private lateinit var toggleKey: KeyMapping

    private val CATEGORY_SR_ADDONS = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("sraddons", "general")
    )

    fun init() {
        commandKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.sraddons.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                CATEGORY_SR_ADDONS
            )
        )

        guiKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.sraddons.gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_SR_ADDONS
            )
        )

        toggleKey = KeyBindingHelper.registerKeyBinding(
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
                modMessage("\u00a7aOpening config GUI...")
            }

            while (toggleKey.consumeClick()) {
                SRConfig.settings.partyCommands.enabled = !SRConfig.settings.partyCommands.enabled
                SRConfig.save()
                val status = if (SRConfig.settings.partyCommands.enabled) "\u00a7aenabled" else "\u00a7cdisabled"
                modMessage("\u00a7eSR-Addons PartyCommands \u00a77is now $status")
            }
        }
    }
}
