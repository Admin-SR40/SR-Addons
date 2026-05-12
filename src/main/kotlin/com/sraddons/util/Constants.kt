package com.sraddons.util

import net.minecraft.network.chat.Component

object Constants {
    const val MOD_ID = "sraddons"
    const val MOD_VERSION = "1.4.3"
    const val GITHUB_REPO = "Admin-SR40/SR-Addons"

    fun makePrefix(): Component {
        return GradientText.cyanToLightBlue("[SR-Addons] ")
    }
}
