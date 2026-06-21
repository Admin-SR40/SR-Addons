package com.sraddons.util

import com.google.gson.Gson
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

fun <T> saveJsonAtomic(file: File, gson: Gson, data: T, logger: Logger) {
    val tmpFile = File(file.parentFile, "${file.name}.tmp")
    try {
        OutputStreamWriter(FileOutputStream(tmpFile), StandardCharsets.UTF_8).use { writer ->
            gson.toJson(data, writer)
        }
        if (!tmpFile.renameTo(file)) {
            logger.warn("Failed to rename tmp file for ${file.name}")
        }
    } catch (e: Exception) {
        logger.error("Failed to save ${file.name}", e)
        tmpFile.delete()
    }
}

object Constants {
    const val MOD_ID = "sraddons"
    const val GITHUB_REPO = "Admin-SR40/SR-Addons"

    val MOD_VERSION: String by lazy {
        FabricLoader.getInstance().getModContainer(MOD_ID)
            .map { it.metadata.version.friendlyString }
            .orElse("1.7.3")
    }

    val PREFIX: Component by lazy { GradientText.cyanToLightBlue("[SR-Addons] ") }

    fun makePrefix(): Component = PREFIX
}
