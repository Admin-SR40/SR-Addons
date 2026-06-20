package com.sraddons.feature.helper

import com.google.gson.reflect.TypeToken
import com.sraddons.util.GsonProvider
import com.sraddons.util.saveJsonAtomic
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import java.io.File

object ReplaceTextsData {
    private val LOGGER = LogManager.getLogger("SR-Addons-ReplaceTexts")
    private val GSON = GsonProvider.PRETTY
    private val FILE = File(
        FabricLoader.getInstance().configDir.toFile(),
        "sraddons-replace-texts.json"
    )

    fun load(): MutableMap<String, String> {
        if (!FILE.exists()) return mutableMapOf()
        return try {
            GSON.fromJson(FILE.readText(), object : TypeToken<MutableMap<String, String>>() {}.type)
                ?: mutableMapOf()
        } catch (e: Exception) {
            LOGGER.error("Failed to load replace-texts data", e)
            mutableMapOf()
        }
    }

    fun save(data: Map<String, String>) {
        saveJsonAtomic(FILE, GSON, data, LOGGER)
    }
}
