package com.sraddons.update

import com.google.gson.annotations.SerializedName
import com.sraddons.util.Constants
import com.sraddons.util.GsonProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

object UpdateChecker {

    private val GSON = GsonProvider.PLAIN

    data class GithubRelease(
        @SerializedName("tag_name") val tagName: String,
        @SerializedName("html_url") val htmlUrl: String
    )

    data class UpdateResult(
        val latestVersion: String,
        val downloadUrl: String? = null
    )

    suspend fun check(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val url = URI.create("https://api.github.com/repos/${Constants.GITHUB_REPO}/releases/latest")
            val connection = url.toURL().openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "application/vnd.github+json")

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val release = GSON.fromJson(body, GithubRelease::class.java)
            val latestVersion = release.tagName.removePrefix("v")

            if (latestVersion != Constants.MOD_VERSION) {
                UpdateResult(latestVersion, release.htmlUrl)
            } else {
                UpdateResult(latestVersion)
            }
        } catch (e: Exception) {
            UpdateResult("unknown")
        }
    }
}
