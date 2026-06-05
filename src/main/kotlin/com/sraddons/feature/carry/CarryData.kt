package com.sraddons.feature.carry

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sraddons.config.SRConfig
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

data class CarryType(
    val name: String,
    var price: Long = 0L,
    var bulkPrice: Long? = null,
    var bulkThreshold: Int = 10
)

data class CarryClient(
    val playerName: String,
    val typeName: String,
    var amount: Int,
    var completed: Int = 0,
    var useBulk: Boolean = false
)

data class CarryStatus(
    var totalOrders: Int = 0,
    var totalCarries: Int = 0,
    var totalEarned: Long = 0L
)

private data class CarryDataFile(
    val types: List<CarryType>,
    val clients: List<CarryClient>,
    val minibossNames: List<String>? = null
)

object CarryState {
    private val LOGGER = LogManager.getLogger("SR-Addons-Carry")
    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    private val CONFIG_DIR = FabricLoader.getInstance().configDir.toFile()
    private val HISTORY_FILE = File(CONFIG_DIR, "srac-history.json")
    private val DATA_FILE = File(CONFIG_DIR, "srac-data.json")

    val types = ConcurrentHashMap<String, CarryType>()
    val clients = ConcurrentHashMap<String, CarryClient>()
    val minibossNames: MutableSet<String> = ConcurrentHashMap.newKeySet()
    @Volatile
    var status = CarryStatus()

    fun loadHistory() {
        if (!HISTORY_FILE.exists()) return
        synchronized(this) {
            try {
                InputStreamReader(FileInputStream(HISTORY_FILE), StandardCharsets.UTF_8).use { reader ->
                    val data = GSON.fromJson(reader, CarryStatus::class.java)
                    if (data != null) {
                        status = data
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to load carry history", e)
            }
        }
    }

    fun saveHistory() {
        val snapshot: CarryStatus
        synchronized(this) { snapshot = status.copy() }
        java.util.concurrent.CompletableFuture.runAsync {
            val tmpFile = File(HISTORY_FILE.parentFile, "${HISTORY_FILE.name}.tmp")
            try {
                OutputStreamWriter(FileOutputStream(tmpFile), StandardCharsets.UTF_8).use { writer ->
                    GSON.toJson(snapshot, writer)
                }
                if (!tmpFile.renameTo(HISTORY_FILE)) {
                    LOGGER.warn("Failed to rename history tmp file")
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to save carry history", e)
                tmpFile.delete()
            }
        }
    }

    fun saveData() {
        val snapshot: CarryDataFile
        synchronized(this) {
            snapshot = CarryDataFile(
                types = types.values.toList(),
                clients = clients.values.toList(),
                minibossNames = minibossNames.toList()
            )
        }
        java.util.concurrent.CompletableFuture.runAsync {
            val tmpFile = File(DATA_FILE.parentFile, "${DATA_FILE.name}.tmp")
            try {
                OutputStreamWriter(FileOutputStream(tmpFile), StandardCharsets.UTF_8).use { writer ->
                    GSON.toJson(snapshot, writer)
                }
                if (!tmpFile.renameTo(DATA_FILE)) {
                    LOGGER.warn("Failed to rename carry data tmp file")
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to save carry data", e)
                tmpFile.delete()
            }
        }
    }

    fun loadData() {
        synchronized(this) {
            // Seed miniboss names from config defaults on first load
            if (minibossNames.isEmpty()) {
                minibossNames.addAll(SRConfig.settings.carry.minibossNames)
            }
            if (!DATA_FILE.exists()) return
            try {
                InputStreamReader(FileInputStream(DATA_FILE), StandardCharsets.UTF_8).use { reader ->
                    val file = GSON.fromJson(reader, CarryDataFile::class.java)
                    if (file != null) {
                        file.types.forEach { types[it.name.lowercase()] = it }
                        file.clients.forEach { clients[it.playerName.lowercase()] = it }
                        if (file.minibossNames != null) {
                            minibossNames.clear()
                            minibossNames.addAll(file.minibossNames)
                        }
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to load carry data", e)
            }
        }
    }

    fun reset() {
        synchronized(this) {
            types.clear()
            clients.clear()
            status = CarryStatus()
        }
    }

    // -- Undo support --

    private data class UndoSnapshot(
        val types: Map<String, CarryType>,
        val clients: Map<String, CarryClient>,
        val status: CarryStatus
    )

    @Volatile
    private var undoSnapshot: UndoSnapshot? = null

    fun saveUndo() {
        synchronized(this) {
            undoSnapshot = UndoSnapshot(
                types = types.mapValues { it.value.copy() },
                clients = clients.mapValues { it.value.copy() },
                status = status.copy()
            )
        }
    }

    fun undo(): Boolean {
        val snapshot = undoSnapshot ?: return false
        synchronized(this) {
            types.clear()
            clients.clear()
            snapshot.types.forEach { types[it.key] = it.value }
            snapshot.clients.forEach { clients[it.key] = it.value }
            status = snapshot.status.copy()
            undoSnapshot = null
            saveData()
            saveHistory()
            return true
        }
    }
}
