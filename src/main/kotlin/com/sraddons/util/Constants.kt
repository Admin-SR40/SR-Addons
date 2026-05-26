package com.sraddons.util

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component

object Constants {
    const val MOD_ID = "sraddons"
    const val GITHUB_REPO = "Admin-SR40/SR-Addons"

    val MOD_VERSION: String by lazy {
        FabricLoader.getInstance().getModContainer(MOD_ID)
            .map { it.metadata.version.friendlyString }
            .orElse("1.5.5")
    }

    fun makePrefix(): Component {
        return GradientText.cyanToLightBlue("[SR-Addons] ")
    }
}
