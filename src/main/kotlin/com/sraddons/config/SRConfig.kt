package com.sraddons.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object SRConfig {
    private val LOGGER = LogManager.getLogger("SR-Addons-Config")
    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    private val CONFIG_FILE = File(
        FabricLoader.getInstance().configDir.toFile(),
        "sraddons.json"
    )

    @Volatile
    var settings = SRConfigData()

    data class EntityFireConfigData(
        var hiddenFire: Boolean = false
    )

    data class PartyCommandsConfigData(
        var enabled: Boolean = true,
        var prefix: String = "!",
        // Info commands
        var ping: Boolean = true,
        var tps: Boolean = true,
        var fps: Boolean = true,
        var time: Boolean = true,
        var location: Boolean = true,
        var coords: Boolean = true,
        var holding: Boolean = true,
        var status: Boolean = true,
        // Party management (require leader)
        var warp: Boolean = true,
        var allinvite: Boolean = true,
        var kick: Boolean = true,
        var kickoffline: Boolean = true,
        var kickall: Boolean = true,
        var promote: Boolean = true,
        var demote: Boolean = true,
        var transfer: Boolean = true,
        var disband: Boolean = true,
        // Party commands (no leader needed)
        var leave: Boolean = true,
        // Fun commands
        var coinflip: Boolean = true,
        var eightball: Boolean = true,
        var dice: Boolean = true,
        // Dungeon
        var queueF1: Boolean = true,
        var queueF2: Boolean = true,
        var queueF3: Boolean = true,
        var queueF4: Boolean = true,
        var queueF5: Boolean = true,
        var queueF6: Boolean = true,
        var queueF7: Boolean = true,
        var queueM1: Boolean = true,
        var queueM2: Boolean = true,
        var queueM3: Boolean = true,
        var queueM4: Boolean = true,
        var queueM5: Boolean = true,
        var queueM6: Boolean = true,
        var queueM7: Boolean = true,
        var queueT1: Boolean = true,
        var queueT2: Boolean = true,
        var queueT3: Boolean = true,
        var queueT4: Boolean = true,
        var queueT5: Boolean = true,
        // Other
        var boop: Boolean = true,
        var invite: Boolean = true,
        var countdown: Boolean = true,
        var mod: Boolean = true,
        // Response settings
        var respondInPartyChat: Boolean = true,
        var showResponseLocally: Boolean = true,
        var removeSeparator: Boolean = true,
        // Note & sound
        var note: String = "",
        var countdownSound: Boolean = true
    )

    data class CarryHighlightConfig(
        var enabled: Boolean = true,
        var colorRed: Int = 255,
        var colorGreen: Int = 255,
        var colorBlue: Int = 0,
        var colorAlpha: Int = 200
    )

    data class CarryConfigData(
        var enabled: Boolean = true,
        var clientHighlight: CarryHighlightConfig = CarryHighlightConfig(colorRed = 57, colorGreen = 255, colorBlue = 20, colorAlpha = 200),
        var bossHighlight: CarryHighlightConfig = CarryHighlightConfig(colorRed = 255, colorGreen = 50, colorBlue = 50, colorAlpha = 200),
        var minibossHighlight: CarryHighlightConfig = CarryHighlightConfig(colorRed = 255, colorGreen = 165, colorBlue = 0, colorAlpha = 200),
        var minibossMaxDistance: Int = 16,
        var bossSpawnNotification: Boolean = true,
        var bossSpawnNotificationText: String = "BOSS SPAWNED",
        var seeThroughWalls: Boolean = false,
        var renderMode: String = "BOTH",
        var lineWidth: Int = 3,
        var maxDistance: Int = 64
    )

    data class StarredMobConfigData(
        var enabled: Boolean = true,
        var colorRed: Int = 255,
        var colorGreen: Int = 255,
        var colorBlue: Int = 0,
        var colorAlpha: Int = 200,
        var seeThroughWalls: Boolean = false,
        var renderMode: String = "BOTH",
        var lineWidth: Int = 3,
        var maxDistance: Int = 64
    )

    data class SRConfigData(
        var entityFire: EntityFireConfigData = EntityFireConfigData(),
        var partyCommands: PartyCommandsConfigData = PartyCommandsConfigData(),
        var starredMob: StarredMobConfigData = StarredMobConfigData(),
        var carry: CarryConfigData = CarryConfigData()
    )

    fun load() {
        synchronized(this) {
            if (!CONFIG_FILE.exists()) {
                migrateFromOldConfigs()
                save()
                return
            }

            try {
                InputStreamReader(FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8).use { reader ->
                    val data = GSON.fromJson(reader, SRConfigData::class.java)
                    if (data != null) {
                        settings = data
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to load config, resetting to defaults", e)
                save()
            }
        }
    }

    fun save() {
        synchronized(this) {
            val tmpFile = File(CONFIG_FILE.parentFile, "${CONFIG_FILE.name}.tmp")
            try {
                OutputStreamWriter(FileOutputStream(tmpFile), StandardCharsets.UTF_8).use { writer ->
                    GSON.toJson(settings, writer)
                }
                if (!tmpFile.renameTo(CONFIG_FILE)) {
                    LOGGER.warn("Failed to rename config tmp file")
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to save config", e)
                tmpFile.delete()
            }
        }
    }

    private fun migrateFromOldConfigs() {
        val configDir = FabricLoader.getInstance().configDir.toFile()
        var migrated = false

        // Migrate EntityFire config
        val oldEntityFireFile = File(configDir, "entityfiremod.json")
        if (oldEntityFireFile.exists()) {
            try {
                InputStreamReader(FileInputStream(oldEntityFireFile), StandardCharsets.UTF_8).use { reader ->
                    val data = GSON.fromJson(reader, EntityFireConfigData::class.java)
                    if (data != null) {
                        settings.entityFire = data
                        migrated = true
                    }
                }
                val ok = oldEntityFireFile.renameTo(File(configDir, "entityfiremod.json.bak"))
                if (!ok) LOGGER.warn("Failed to rename entityfiremod.json")
            } catch (e: Exception) {
                LOGGER.error("Failed to migrate EntityFire config", e)
            }
        }

        // Migrate PartyCommands config
        val oldPCFile = File(configDir, "partycommands.json")
        if (oldPCFile.exists()) {
            try {
                InputStreamReader(FileInputStream(oldPCFile), StandardCharsets.UTF_8).use { reader ->
                    val data = GSON.fromJson(reader, PartyCommandsConfigData::class.java)
                    if (data != null) {
                        settings.partyCommands = data
                        migrated = true
                    }
                }
                val ok = oldPCFile.renameTo(File(configDir, "partycommands.json.bak"))
                if (!ok) LOGGER.warn("Failed to rename partycommands.json")
            } catch (e: Exception) {
                LOGGER.error("Failed to migrate PartyCommands config", e)
            }
        }

        // Migrate StarredMob config
        val oldSMFile = File(configDir, "starredmobhighlighter.json")
        if (oldSMFile.exists()) {
            try {
                InputStreamReader(FileInputStream(oldSMFile), StandardCharsets.UTF_8).use { reader ->
                    val data = GSON.fromJson(reader, StarredMobConfigData::class.java)
                    if (data != null) {
                        settings.starredMob = data
                        migrated = true
                    }
                }
                val ok = oldSMFile.renameTo(File(configDir, "starredmobhighlighter.json.bak"))
                if (!ok) LOGGER.warn("Failed to rename starredmobhighlighter.json")
            } catch (e: Exception) {
                LOGGER.error("Failed to migrate StarredMob config", e)
            }
        }

        if (migrated) {
            save()
        }
    }
}
