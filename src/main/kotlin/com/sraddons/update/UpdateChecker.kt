package com.sraddons.update

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.sraddons.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object UpdateChecker {

    private val GSON = Gson()

    data class GitHubRelease(
        @SerializedName("tag_name") val tagName: String,
        @SerializedName("html_url") val htmlUrl: String,
        @SerializedName("body") val body: String?
    )

    data class UpdateResult(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val downloadUrl: String?
    )

    suspend fun check(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/${Constants.GITHUB_REPO}/releases/latest")
            val connection = url.openConnection()
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val text = connection.getInputStream().bufferedReader().use { it.readText() }
            val release = GSON.fromJson(text, GitHubRelease::class.java)

            val latestVersion = release.tagName.removePrefix("v")
            val hasUpdate = latestVersion != Constants.MOD_VERSION

            UpdateResult(hasUpdate, latestVersion, release.htmlUrl.takeIf { hasUpdate })
        } catch (e: Exception) {
            UpdateResult(hasUpdate = false, latestVersion = "unknown", downloadUrl = null)
        }
    }
}
