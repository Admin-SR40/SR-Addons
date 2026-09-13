package com.sraddons.config

import com.sraddons.util.GsonProvider
import com.sraddons.util.saveJsonAtomic
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.charset.StandardCharsets

object SRConfig {
    private val LOGGER = LogManager.getLogger("SR-Addons-Config")
    private val GSON = GsonProvider.PRETTY
    private val CONFIG_FILE =
        File(
            FabricLoader.getInstance().configDir.toFile(),
            "sraddons.json",
        )

    @Volatile
    var settings = SRConfigData()

    data class GeneralConfigData(
        // Display
        var showOwnNameInThirdPerson: Boolean = true,
        var removeSeparator: Boolean = true,
        var autoCheckUpdates: Boolean = false,
        // Visual Tweaks
        var hideArmorBar: Boolean = false,
        var hideHungerBar: Boolean = false,
        var hideEntityFire: Boolean = false,
        var fullbright: Boolean = false,
        var betterFov: Boolean = false,
        // Text
        var replaceTextsEnabled: Boolean = false,
        var highlightDevName: Boolean = true,
        // Quick Tools
        var enableStandaloneCalc: Boolean = false,
        var pinTooltip: Boolean = true,
        var pinTooltipScale: Float = 1.0f,
    )

    data class PartyCommandsConfigData(
        var enabled: Boolean = true,
        var prefix: String = "!",
        var disabledCommands: MutableSet<String> = mutableSetOf(),
        var respondInPartyChat: Boolean = true,
        var showResponseLocally: Boolean = true,
        var note: String = "",
        var countdownSound: Boolean = true,
        var mod: Boolean = true,
        var autoReplyModDelayMs: Int = 500,
        var autoReplyGithubDelayMs: Int = 800,
        var chatSendIntervalMs: Int = 500,
        var partyListUpdateCooldownMs: Int = 60000,
        var partyListInitialDelayMs: Int = 500,
        var partyListUpdateDelayMs: Int = 1500,
    )

    fun isCommandEnabled(cmd: String): Boolean = cmd !in settings.partyCommands.disabledCommands

    interface HighlightColorConfig {
        val colorRed: Int
        val colorGreen: Int
        val colorBlue: Int
        val colorAlpha: Int
    }

    data class CarryHighlightConfig(
        var enabled: Boolean = true,
        override var colorRed: Int = 255,
        override var colorGreen: Int = 255,
        override var colorBlue: Int = 0,
        override var colorAlpha: Int = 200,
    ) : HighlightColorConfig

    data class CarryConfigData(
        var enabled: Boolean = true,
        var clientHighlight: CarryHighlightConfig = CarryHighlightConfig(colorRed = 57, colorGreen = 255, colorBlue = 20, colorAlpha = 200),
        var bossHighlight: CarryHighlightConfig = CarryHighlightConfig(colorRed = 255, colorGreen = 50, colorBlue = 50, colorAlpha = 200),
        var minibossHighlight: CarryHighlightConfig =
            CarryHighlightConfig(colorRed = 255, colorGreen = 165, colorBlue = 0, colorAlpha = 200),
        var minibossMaxDistance: Int = 16,
        var minibossNames: List<String> =
            listOf(
                "Revenant Sycophant",
                "Revenant Champion",
                "Deformed Revenant",
                "Atoned Champion",
                "Atoned Revenant",
                "Tarantula Vermin",
                "Tarantula Beast",
                "Mutant Tarantula",
                "Primordial Jockey",
                "Primordial Viscount",
                "Pack Enforcer",
                "Sven Follower",
                "Sven Alpha",
                "Voidling Devotee",
                "Voidling Radical",
                "Voidcrazed Maniac",
                "Flare Demon",
                "Kindleheart Demon",
                "Burningsoul Demon",
            ),
        var bossSpawnNotification: Boolean = true,
        var bossSpawnNotificationText: String = "&cBOSS SPAWNED",
        var renderMode: String = "BOTH",
        var lineWidth: Int = 3,
        var maxDistance: Int = 64,
        var bossUuidPruneInterval: Int = 1200,
    )

    data class RagnarockConfigData(
        var enabled: Boolean = true,
        var castNotification: Boolean = true,
        var cancelNotification: Boolean = true,
        var castMessage: String = "&aCasted Rag",
        var cancelMessage: String = "&cRagnarock Cancelled!",
        var showStrengthGained: Boolean = true,
        var announceStrengthInParty: Boolean = false,
        var playSound: Boolean = true,
    )

    data class PingAlertConfigData(
        var enabled: Boolean = false,
        var threshold: Int = 400,
        var delaySeconds: Int = 3,
        var message: String = "&cHigh Ping",
        var playSound: Boolean = true,
    )

    data class TpsAlertConfigData(
        var enabled: Boolean = false,
        var threshold: Double = 16.0,
        var delaySeconds: Int = 3,
        var message: String = "&cLow TPS",
        var playSound: Boolean = true,
    )

    data class StarredMobConfigData(
        var enabled: Boolean = true,
        override var colorRed: Int = 255,
        override var colorGreen: Int = 255,
        override var colorBlue: Int = 0,
        override var colorAlpha: Int = 200,
        var renderMode: String = "BOTH",
        var lineWidth: Int = 3,
        var maxDistance: Int = 64,
    ) : HighlightColorConfig

    data class ChatAlertConfigData(
        var enabled: Boolean = true,
        var entries: MutableList<String> = mutableListOf(),
    )

    data class SRConfigData(
        var general: GeneralConfigData = GeneralConfigData(),
        var partyCommands: PartyCommandsConfigData = PartyCommandsConfigData(),
        var starredMob: StarredMobConfigData = StarredMobConfigData(),
        var carry: CarryConfigData = CarryConfigData(),
        var ragnarock: RagnarockConfigData = RagnarockConfigData(),
        var pingAlert: PingAlertConfigData = PingAlertConfigData(),
        var tpsAlert: TpsAlertConfigData = TpsAlertConfigData(),
        var chatAlert: ChatAlertConfigData = ChatAlertConfigData(),
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
                backupUnreadableConfig()
                settings = SRConfigData()
                save()
            }
        }
    }

    /**
     * Keeps a copy of a config file that could not be parsed so that the reset
     * below never destroys user data without a trace.
     */
    private fun backupUnreadableConfig() {
        try {
            val backup = File(CONFIG_FILE.parentFile, "${CONFIG_FILE.name}.bak")
            CONFIG_FILE.copyTo(backup, overwrite = true)
            LOGGER.warn("Copied unreadable config to {}", backup.name)
        } catch (e: Exception) {
            LOGGER.error("Failed to back up unreadable config", e)
        }
    }

    // Migrate old "helper" { ... } block to top-level fields
    private fun migrateFromHelper(root: com.google.gson.JsonObject) {
        try {
            settings = SRConfigData()

            // Preserve top-level fields that already exist in new format
            settings.general = root.read("general", settings.general)
            settings.partyCommands = root.read("partyCommands", settings.partyCommands)
            settings.starredMob = root.read("starredMob", settings.starredMob)
            settings.carry = root.read("carry", settings.carry)

            // Migrate entityFire.hiddenFire → general.hideEntityFire
            root["entityFire"]?.asJsonObject?.booleanOrNull("hiddenFire")?.let {
                settings.general.hideEntityFire = it
            }

            // Migrate helper sub-objects
            val helper = root["helper"]?.asJsonObject
            if (helper != null) {
                // Complex objects → top-level
                settings.ragnarock = helper.read("ragnarock", settings.ragnarock)
                settings.pingAlert = helper.read("pingAlert", settings.pingAlert)
                settings.tpsAlert = helper.read("tpsAlert", settings.tpsAlert)

                // Simple booleans → general
                helper["calculator"]?.asJsonObject?.booleanOrNull("enableStandaloneCalc")?.let {
                    settings.general.enableStandaloneCalc = it
                }
                helper["replaceTexts"]?.asJsonObject?.let { replaceTexts ->
                    replaceTexts.booleanOrNull("enabled")?.let { settings.general.replaceTextsEnabled = it }
                    replaceTexts.booleanOrNull("highlightDevName")?.let { settings.general.highlightDevName = it }
                }
                helper["betterFov"]?.asJsonObject?.booleanOrNull("enabled")?.let { settings.general.betterFov = it }
                helper["fullbright"]?.asJsonObject?.booleanOrNull("enabled")?.let { settings.general.fullbright = it }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to migrate old helper config, resetting to defaults", e)
            settings = SRConfigData()
        }
    }

    /** Reads a nested config object, keeping [fallback] when the key is absent or malformed. */
    private inline fun <reified T : Any> com.google.gson.JsonObject.read(
        key: String,
        fallback: T,
    ): T = get(key)?.let { GSON.fromJson(it, T::class.java) } ?: fallback

    private fun com.google.gson.JsonObject.booleanOrNull(key: String): Boolean? = get(key)?.asBoolean

    fun save() {
        synchronized(this) {
            saveJsonAtomic(CONFIG_FILE, GSON, settings, LOGGER)
        }
    }

    /** Runs [block] while holding the config lock, so a mutation and a following [save] stay consistent. */
    inline fun update(block: (SRConfigData) -> Unit) {
        synchronized(this) { block(settings) }
    }
}

fun SRConfig.HighlightColorConfig.toColor() =
    java.awt.Color(
        colorRed.coerceIn(0, 255),
        colorGreen.coerceIn(0, 255),
        colorBlue.coerceIn(0, 255),
        colorAlpha.coerceIn(0, 255),
    )

fun SRConfig.HighlightColorConfig.toARGB(): Int =
    net.minecraft.util.ARGB.color(
        colorAlpha.coerceIn(0, 255),
        colorRed.coerceIn(0, 255),
        colorGreen.coerceIn(0, 255),
        colorBlue.coerceIn(0, 255),
    )
