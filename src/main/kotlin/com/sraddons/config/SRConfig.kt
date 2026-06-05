package com.sraddons.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileOutputStream
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

    data class GeneralConfigData(
        // Display
        var showOwnNameInThirdPerson: Boolean = true,
        var removeSeparator: Boolean = true,
        var autoCheckUpdates: Boolean = false,
        // Visual Tweaks
        var hideEntityFire: Boolean = false,
        var fullbright: Boolean = false,
        var betterFov: Boolean = false,
        // Text
        var replaceTextsEnabled: Boolean = false,
        var highlightDevName: Boolean = true,
        // Quick Tools
        var enableStandaloneCalc: Boolean = false
    )

    data class PartyCommandsConfigData(
        var enabled: Boolean = true,
        var prefix: String = "!",
        var disabledCommands: MutableSet<String> = mutableSetOf(),
        var respondInPartyChat: Boolean = true,
        var showResponseLocally: Boolean = true,
        var note: String = "",
        var countdownSound: Boolean = true,
        var mod: Boolean = true
    )

    fun isCommandEnabled(cmd: String): Boolean = cmd !in settings.partyCommands.disabledCommands

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
        var minibossNames: List<String> = listOf(
            "Revenant Sycophant", "Revenant Champion", "Deformed Revenant",
            "Atoned Champion", "Atoned Revenant",
            "Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula",
            "Primordial Jockey", "Primordial Viscount",
            "Pack Enforcer", "Sven Follower", "Sven Alpha",
            "Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac",
            "Flare Demon", "Kindleheart Demon", "Burningsoul Demon"
        ),
        var bossSpawnNotification: Boolean = true,
        var bossSpawnNotificationText: String = "&cBOSS SPAWNED",
        var renderMode: String = "BOTH",
        var lineWidth: Int = 3,
        var maxDistance: Int = 64
    )

    data class RagnarockConfigData(
        var enabled: Boolean = true,
        var castNotification: Boolean = true,
        var cancelNotification: Boolean = true,
        var castMessage: String = "&aCasted Rag",
        var cancelMessage: String = "&cRagnarock Cancelled!",
        var showStrengthGained: Boolean = true,
        var announceStrengthInParty: Boolean = false,
        var playSound: Boolean = true
    )

    data class PingAlertConfigData(
        var enabled: Boolean = false,
        var threshold: Int = 400,
        var delaySeconds: Int = 3,
        var message: String = "&cHigh Ping",
        var playSound: Boolean = true
    )

    data class TpsAlertConfigData(
        var enabled: Boolean = false,
        var threshold: Double = 16.0,
        var delaySeconds: Int = 3,
        var message: String = "&cLow TPS",
        var playSound: Boolean = true
    )

    data class StarredMobConfigData(
        var enabled: Boolean = true,
        var colorRed: Int = 255,
        var colorGreen: Int = 255,
        var colorBlue: Int = 0,
        var colorAlpha: Int = 200,
        var renderMode: String = "BOTH",
        var lineWidth: Int = 3,
        var maxDistance: Int = 64
    )

    data class ChatAlertConfigData(
        var enabled: Boolean = true,
        var entries: MutableList<String> = mutableListOf()
    )

    data class SRConfigData(
        var general: GeneralConfigData = GeneralConfigData(),
        var partyCommands: PartyCommandsConfigData = PartyCommandsConfigData(),
        var starredMob: StarredMobConfigData = StarredMobConfigData(),
        var carry: CarryConfigData = CarryConfigData(),
        var ragnarock: RagnarockConfigData = RagnarockConfigData(),
        var pingAlert: PingAlertConfigData = PingAlertConfigData(),
        var tpsAlert: TpsAlertConfigData = TpsAlertConfigData(),
        var chatAlert: ChatAlertConfigData = ChatAlertConfigData()
    )

    fun load() {
        synchronized(this) {
            if (!CONFIG_FILE.exists()) {
                save()
                return
            }

            try {
                val rawJson = CONFIG_FILE.readText(StandardCharsets.UTF_8)
                val root = GSON.fromJson(rawJson, com.google.gson.JsonObject::class.java)
                if (root?.has("helper") == true) {
                    migrateFromHelper(root)
                    save()
                    return
                }
                val data = GSON.fromJson(root, SRConfigData::class.java)
                if (data != null) {
                    settings = data
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to load config, resetting to defaults", e)
                save()
            }
        }
    }

    private fun migrateFromHelper(root: com.google.gson.JsonObject) {
        try {

            settings = SRConfigData()

            root["general"]?.let { GSON.fromJson(it, GeneralConfigData::class.java)?.let { d -> settings.general = d } }
            root["partyCommands"]?.let { GSON.fromJson(it, PartyCommandsConfigData::class.java)?.let { d -> settings.partyCommands = d } }
            root["starredMob"]?.let { GSON.fromJson(it, StarredMobConfigData::class.java)?.let { d -> settings.starredMob = d } }
            root["carry"]?.let { GSON.fromJson(it, CarryConfigData::class.java)?.let { d -> settings.carry = d } }

            root["entityFire"]?.asJsonObject?.get("hiddenFire")?.let { settings.general.hideEntityFire = it.asBoolean }

            val helper = root["helper"]?.asJsonObject
            if (helper != null) {
                helper["ragnarock"]?.let { GSON.fromJson(it, RagnarockConfigData::class.java)?.let { d -> settings.ragnarock = d } }
                helper["pingAlert"]?.let { GSON.fromJson(it, PingAlertConfigData::class.java)?.let { d -> settings.pingAlert = d } }
                helper["tpsAlert"]?.let { GSON.fromJson(it, TpsAlertConfigData::class.java)?.let { d -> settings.tpsAlert = d } }

                helper["calculator"]?.asJsonObject?.get("enableStandaloneCalc")?.let { settings.general.enableStandaloneCalc = it.asBoolean }
                helper["replaceTexts"]?.asJsonObject?.let { o ->
                    o["enabled"]?.let { settings.general.replaceTextsEnabled = it.asBoolean }
                    o["highlightDevName"]?.let { settings.general.highlightDevName = it.asBoolean }
                }
                helper["betterFov"]?.asJsonObject?.get("enabled")?.let { settings.general.betterFov = it.asBoolean }
                helper["fullbright"]?.asJsonObject?.get("enabled")?.let { settings.general.fullbright = it.asBoolean }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to migrate old helper config, resetting to defaults", e)
            settings = SRConfigData()
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
}

fun SRConfig.CarryHighlightConfig.toColor() = java.awt.Color(
    colorRed.coerceIn(0, 255), colorGreen.coerceIn(0, 255),
    colorBlue.coerceIn(0, 255), colorAlpha.coerceIn(0, 255)
)

fun SRConfig.CarryHighlightConfig.toARGB(): Int = net.minecraft.util.ARGB.color(
    colorAlpha.coerceIn(0, 255), colorRed.coerceIn(0, 255),
    colorGreen.coerceIn(0, 255), colorBlue.coerceIn(0, 255)
)

fun SRConfig.StarredMobConfigData.toColor() = java.awt.Color(
    colorRed.coerceIn(0, 255), colorGreen.coerceIn(0, 255),
    colorBlue.coerceIn(0, 255), colorAlpha.coerceIn(0, 255)
)

fun SRConfig.StarredMobConfigData.toARGB(): Int = net.minecraft.util.ARGB.color(
    colorAlpha.coerceIn(0, 255), colorRed.coerceIn(0, 255),
    colorGreen.coerceIn(0, 255), colorBlue.coerceIn(0, 255)
)
