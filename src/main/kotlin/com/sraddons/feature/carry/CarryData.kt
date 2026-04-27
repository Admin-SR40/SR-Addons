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
    var completed: Int = 0
)

data class CarryStatus(
    var totalOrders: Int = 0,
    var totalCarries: Int = 0,
    var totalEarned: Long = 0L
)

object CarryState {
    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    private val HISTORY_FILE = File(
        FabricLoader.getInstance().configDir.toFile(),
        "srac-history.json"
    )

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

    fun reset() {
        types.clear()
        clients.clear()
        status = CarryStatus()
    }
}
