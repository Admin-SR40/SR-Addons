package com.sraddons.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonProvider {
    val PRETTY: Gson = GsonBuilder().setPrettyPrinting().create()
    val PLAIN: Gson = Gson()
}
