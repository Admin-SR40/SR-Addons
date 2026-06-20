package com.sraddons.feature.hud

import com.sraddons.config.SRConfig
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements

object HudElementHider {

    fun init() {
        hideArmorBar()
        hideHungerBar()
    }

    /**
     * Replaces the vanilla armor bar renderer. When [SRConfig.GeneralConfigData.hideArmorBar] is
     * enabled, the armor bar is hidden. Config changes take effect on the next render tick.
     */
    private fun hideArmorBar() {
        HudElementRegistry.replaceElement(VanillaHudElements.ARMOR_BAR) { vanilla ->
            HudElement { extractor, deltaTracker ->
                if (!SRConfig.settings.general.hideArmorBar) {
                    vanilla.extractRenderState(extractor, deltaTracker)
                }
            }
        }
    }

    /**
     * Replaces the vanilla food/hunger bar renderer. When [SRConfig.GeneralConfigData.hideHungerBar] is
     * enabled, the hunger bar is hidden. Config changes take effect on the next render tick.
     */
    private fun hideHungerBar() {
        HudElementRegistry.replaceElement(VanillaHudElements.FOOD_BAR) { vanilla ->
            HudElement { extractor, deltaTracker ->
                if (!SRConfig.settings.general.hideHungerBar) {
                    vanilla.extractRenderState(extractor, deltaTracker)
                }
            }
        }
    }
}
