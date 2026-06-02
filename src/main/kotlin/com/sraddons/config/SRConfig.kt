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

    data class GeneralConfigData(
        var showOwnNameInThirdPerson: Boolean = true,
        var removeSeparator: Boolean = true,
        var autoCheckUpdates: Boolean = false
    )

    data class EntityFireConfigData(
        var hiddenFire: Boolean = false
    )

    data class PartyCommandsConfigData(
        var enabled: Boolean = true,
        var prefix: String = "!",
        var disabledCommands: MutableSet<String> = mutableSetOf(),
        // Response settings
        var respondInPartyChat: Boolean = true,
        var showResponseLocally: Boolean = true,
        // Note & sound
        var note: String = "",
        var countdownSound: Boolean = true,
        var mod: Boolean = true // auto-reply to !mod
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

    data class CalculatorConfigData(
        var enableStandaloneCalc: Boolean = false
    )

    data class ReplaceTextsConfigData(
        var enabled: Boolean = false,
        var highlightDevName: Boolean = true
    )

    data class BetterFovConfigData(
        var enabled: Boolean = false
    )

    data class HelperConfigData(
        var ragnarock: RagnarockConfigData = RagnarockConfigData(),
        var calculator: CalculatorConfigData = CalculatorConfigData(),
        var pingAlert: PingAlertConfigData = PingAlertConfigData(),
        var tpsAlert: TpsAlertConfigData = TpsAlertConfigData(),
        var replaceTexts: ReplaceTextsConfigData = ReplaceTextsConfigData(),
        var betterFov: BetterFovConfigData = BetterFovConfigData()
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

    data class SRConfigData(
        var general: GeneralConfigData = GeneralConfigData(),
        var entityFire: EntityFireConfigData = EntityFireConfigData(),
        var partyCommands: PartyCommandsConfigData = PartyCommandsConfigData(),
        var starredMob: StarredMobConfigData = StarredMobConfigData(),
        var carry: CarryConfigData = CarryConfigData(),
        var helper: HelperConfigData = HelperConfigData()
    )

    fun load() {
        synchronized(this) {
            if (!CONFIG_FILE.exists()) {
                migrateFromOldConfigs()
                save()
                return
            }

            try {
                val rawJson = CONFIG_FILE.readText(StandardCharsets.UTF_8)
                val data = GSON.fromJson(rawJson, SRConfigData::class.java)
                if (data != null) {
                    settings = data
                    if (needsPartyCommandsMigration(rawJson)) {
                        migratePartyCommandsFromBooleans(rawJson)
                        save()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to load config, resetting to defaults", e)
                save()
            }
        }
    }

    private fun needsPartyCommandsMigration(rawJson: String): Boolean {
        if (settings.partyCommands.disabledCommands.isNotEmpty()) return false
        return LEGACY_PC_BOOLEAN_FIELDS.any { rawJson.contains("\"$it\"") }
    }

    private fun migratePartyCommandsFromBooleans(rawJson: String) {
        try {
            val old = GSON.fromJson(rawJson, PartyCommandsConfigDataV0::class.java)
            val disabled = settings.partyCommands.disabledCommands
            if (old?.ping == false) disabled.add("ping")
            if (old?.tps == false) disabled.add("tps")
            if (old?.fps == false) disabled.add("fps")
            if (old?.time == false) disabled.add("time")
            if (old?.location == false) disabled.add("location")
            if (old?.coords == false) disabled.add("coords")
            if (old?.holding == false) disabled.add("holding")
            if (old?.status == false) disabled.add("status")
            if (old?.warp == false) disabled.add("warp")
            if (old?.allinvite == false) disabled.add("allinvite")
            if (old?.kick == false) disabled.add("kick")
            if (old?.kickoffline == false) disabled.add("kickoffline")
            if (old?.kickall == false) disabled.add("kickall")
            if (old?.promote == false) disabled.add("promote")
            if (old?.demote == false) disabled.add("demote")
            if (old?.transfer == false) disabled.add("transfer")
            if (old?.disband == false) disabled.add("disband")
            if (old?.leave == false) disabled.add("leave")
            if (old?.coinflip == false) disabled.add("coinflip")
            if (old?.eightball == false) disabled.add("eightball")
            if (old?.dice == false) disabled.add("dice")
            if (old?.queueF1 == false) disabled.add("f1")
            if (old?.queueF2 == false) disabled.add("f2")
            if (old?.queueF3 == false) disabled.add("f3")
            if (old?.queueF4 == false) disabled.add("f4")
            if (old?.queueF5 == false) disabled.add("f5")
            if (old?.queueF6 == false) disabled.add("f6")
            if (old?.queueF7 == false) disabled.add("f7")
            if (old?.queueM1 == false) disabled.add("m1")
            if (old?.queueM2 == false) disabled.add("m2")
            if (old?.queueM3 == false) disabled.add("m3")
            if (old?.queueM4 == false) disabled.add("m4")
            if (old?.queueM5 == false) disabled.add("m5")
            if (old?.queueM6 == false) disabled.add("m6")
            if (old?.queueM7 == false) disabled.add("m7")
            if (old?.queueT1 == false) disabled.add("t1")
            if (old?.queueT2 == false) disabled.add("t2")
            if (old?.queueT3 == false) disabled.add("t3")
            if (old?.queueT4 == false) disabled.add("t4")
            if (old?.queueT5 == false) disabled.add("t5")
            if (old?.boop == false) disabled.add("boop")
            if (old?.invite == false) disabled.add("invite")
            if (old?.countdown == false) disabled.add("countdown")
            if (old?.removeSeparator != null) settings.general.removeSeparator = old.removeSeparator!!
        } catch (e: Exception) {
            LOGGER.warn("Failed to migrate old PartyCommands boolean format", e)
        }
    }

    // Legacy format used only for migration from boolean-based config
    @Suppress("unused")
    private data class PartyCommandsConfigDataV0(
        val ping: Boolean? = null, val tps: Boolean? = null, val fps: Boolean? = null,
        val time: Boolean? = null, val location: Boolean? = null, val coords: Boolean? = null,
        val holding: Boolean? = null, val status: Boolean? = null,
        val warp: Boolean? = null, val allinvite: Boolean? = null,
        val kick: Boolean? = null, val kickoffline: Boolean? = null, val kickall: Boolean? = null,
        val promote: Boolean? = null, val demote: Boolean? = null, val transfer: Boolean? = null,
        val disband: Boolean? = null, val leave: Boolean? = null,
        val coinflip: Boolean? = null, val eightball: Boolean? = null, val dice: Boolean? = null,
        val queueF1: Boolean? = null, val queueF2: Boolean? = null, val queueF3: Boolean? = null,
        val queueF4: Boolean? = null, val queueF5: Boolean? = null, val queueF6: Boolean? = null,
        val queueF7: Boolean? = null, val queueM1: Boolean? = null, val queueM2: Boolean? = null,
        val queueM3: Boolean? = null, val queueM4: Boolean? = null, val queueM5: Boolean? = null,
        val queueM6: Boolean? = null, val queueM7: Boolean? = null,
        val queueT1: Boolean? = null, val queueT2: Boolean? = null, val queueT3: Boolean? = null,
        val queueT4: Boolean? = null, val queueT5: Boolean? = null,
        val boop: Boolean? = null, val invite: Boolean? = null,
        val countdown: Boolean? = null, val mod: Boolean? = null,
        val removeSeparator: Boolean? = null
    )

    private val LEGACY_PC_BOOLEAN_FIELDS = listOf(
            "ping", "tps", "fps", "time", "location", "coords", "holding", "status",
            "warp", "allinvite", "kick", "kickoffline", "kickall", "promote", "demote",
            "transfer", "disband", "leave", "coinflip", "eightball", "dice",
            "queueF1", "queueF2", "queueF3", "queueF4", "queueF5", "queueF6", "queueF7",
            "queueM1", "queueM2", "queueM3", "queueM4", "queueM5", "queueM6", "queueM7",
            "queueT1", "queueT2", "queueT3", "queueT4", "queueT5",
            "boop", "invite", "countdown"
    )

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

        migrateOldConfig<EntityFireConfigData>(File(configDir, "entityfiremod.json")) { settings.entityFire = it; migrated = true }
        migrateOldConfig<PartyCommandsConfigData>(File(configDir, "partycommands.json")) { settings.partyCommands = it; migrated = true }
        migrateOldConfig<StarredMobConfigData>(File(configDir, "starredmobhighlighter.json")) { settings.starredMob = it; migrated = true }

        if (migrated) {
            save()
        }
    }

    private inline fun <reified T> migrateOldConfig(file: File, assign: (T) -> Unit) {
        if (!file.exists()) return
        try {
            InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
                val data = GSON.fromJson(reader, T::class.java)
                if (data != null) {
                    assign(data)
                    val bak = File(file.parentFile, "${file.name}.bak")
                    file.renameTo(bak)
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to migrate ${file.name}", e)
        }
    }
}

fun SRConfig.CarryHighlightConfig.toColor() = java.awt.Color(
    colorRed.coerceIn(0, 255), colorGreen.coerceIn(0, 255),
    colorBlue.coerceIn(0, 255), colorAlpha.coerceIn(0, 255)
)

fun SRConfig.StarredMobConfigData.toColor() = java.awt.Color(
    colorRed.coerceIn(0, 255), colorGreen.coerceIn(0, 255),
    colorBlue.coerceIn(0, 255), colorAlpha.coerceIn(0, 255)
)
