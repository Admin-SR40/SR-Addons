package com.sraddons.feature.carry

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

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
    val clients: List<CarryClient>
)

object CarryState {
    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    private val CONFIG_DIR = FabricLoader.getInstance().configDir.toFile()
    private val HISTORY_FILE = File(CONFIG_DIR, "srac-history.json")
    private val DATA_FILE = File(CONFIG_DIR, "srac-data.json")

    val types = mutableMapOf<String, CarryType>()
    val clients = mutableMapOf<String, CarryClient>()
    var status = CarryStatus()

    fun loadHistory() {
        if (!HISTORY_FILE.exists()) return
        try {
            FileReader(HISTORY_FILE).use { reader ->
                val data = GSON.fromJson(reader, CarryStatus::class.java)
                if (data != null) {
                    status = data
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveHistory() {
        try {
            FileWriter(HISTORY_FILE).use { writer ->
                GSON.toJson(status, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveData() {
        try {
            FileWriter(DATA_FILE).use { writer ->
                val file = CarryDataFile(
                    types = types.values.toList(),
                    clients = clients.values.toList()
                )
                GSON.toJson(file, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadData() {
        if (!DATA_FILE.exists()) return
        try {
            FileReader(DATA_FILE).use { reader ->
                val file = GSON.fromJson(reader, CarryDataFile::class.java)
                if (file != null) {
                    file.types.forEach { types[it.name.lowercase()] = it }
                    file.clients.forEach { clients[it.playerName.lowercase()] = it }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reset() {
        types.clear()
        clients.clear()
        status = CarryStatus()
    }
}
