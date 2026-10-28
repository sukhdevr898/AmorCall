package com.example.data.avatar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AvatarRepository private constructor() {

    private var cachedCatalog: List<Avatar>? = null

    // Fallback curated catalog of all 20 avatars (10 boys, 10 girls)
    val fallbackAvatars: List<Avatar> = listOf(
        // Boys
        Avatar("boy_001", "Kai (Anime Classic)", "BOY", "", null, listOf("Anime", "Stylish")),
        Avatar("boy_002", "Ren (Cool Jacket)", "BOY", "", null, listOf("Stylish", "Minimal")),
        Avatar("boy_003", "Leo (Warm Smile)", "BOY", "", null, listOf("Cute", "Anime")),
        Avatar("boy_004", "Haru (Playful)", "BOY", "", null, listOf("Cute", "Stylish")),
        Avatar("boy_005", "Zane (Mystic)", "BOY", "", null, listOf("Anime", "Minimal")),
        Avatar("boy_006", "Tora (Casual Boy)", "BOY", "", null, listOf("Minimal", "Stylish")),
        Avatar("boy_007", "Kenji (Heart Eyes)", "BOY", "", null, listOf("Cute", "Anime")),
        Avatar("boy_008", "Ryu (Night City)", "BOY", "", null, listOf("Stylish", "Anime")),
        Avatar("boy_009", "Yuki (Anime Prince)", "BOY", "", null, listOf("Anime", "Cute")),
        Avatar("boy_010", "Sora (Sweetheart)", "BOY", "", null, listOf("Cute", "Minimal")),
        // Girls
        Avatar("girl_001", "Aria (Sweet Bow)", "GIRL", "", null, listOf("Cute", "Anime")),
        Avatar("girl_002", "Mei (Summer Hat)", "GIRL", "", null, listOf("Stylish", "Minimal")),
        Avatar("girl_003", "Yuna (Soft Glow)", "GIRL", "", null, listOf("Cute", "Stylish")),
        Avatar("girl_004", "Luna (Cat Ears)", "GIRL", "", null, listOf("Cute", "Anime")),
        Avatar("girl_005", "Hana (Pastel Bloom)", "GIRL", "", null, listOf("Anime", "Minimal")),
        Avatar("girl_006", "Chloe (Sunny Beret)", "GIRL", "", null, listOf("Stylish", "Cute")),
        Avatar("girl_007", "Kira (Stylish Ribbon)", "GIRL", "", null, listOf("Stylish", "Anime")),
        Avatar("girl_008", "Miku (Warm Knitted)", "GIRL", "", null, listOf("Cute", "Minimal")),
        Avatar("girl_009", "Emi (Heart Smile)", "GIRL", "", null, listOf("Cute", "Anime")),
        Avatar("girl_010", "Rin (Anime Idol)", "GIRL", "", null, listOf("Anime", "Stylish"))
    )

    fun initFromAssets(context: android.content.Context) {
        if (cachedCatalog != null && cachedCatalog!!.isNotEmpty()) return
        try {
            val jsonStr = context.assets.open("avatar_catalog.json").bufferedReader().use { it.readText() }
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<Avatar>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.optString("name", id)
                val cat = obj.optString("category", "BOY")
                val imgUrl = obj.optString("uploadThingUrl", obj.optString("imageUrl", ""))
                val tagsArr = obj.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsArr != null) {
                    for (t in 0 until tagsArr.length()) tags.add(tagsArr.getString(t))
                }
                list.add(Avatar(id, name, cat, imgUrl, imgUrl, tags))
            }
            if (list.isNotEmpty()) {
                cachedCatalog = list
            }
        } catch (e: Exception) {
            android.util.Log.w("AvatarRepository", "Failed to load bundled avatar_catalog: ${e.message}")
        }
    }

    fun resolveAvatarUrl(avatarId: String, serverUrl: String): String {
        if (avatarId.isBlank()) return ""
        if (avatarId.startsWith("http://") || avatarId.startsWith("https://")) {
            return avatarId
        }
        val found = cachedCatalog?.find { it.id.equals(avatarId, ignoreCase = true) }
        if (found != null && found.imageUrl.isNotBlank()) {
            return found.imageUrl
        }
        val base = sanitizeHttpUrl(serverUrl)
        return "$base/api/profile/dp/$avatarId"
    }

    fun resolveAvatarThumbnailUrl(avatarId: String, serverUrl: String): String {
        return resolveAvatarUrl(avatarId, serverUrl)
    }

    suspend fun fetchAvatars(
        serverUrl: String,
        category: String? = null,
        tag: String? = null
    ): List<Avatar> = withContext(Dispatchers.IO) {
        val base = sanitizeHttpUrl(serverUrl)
        val queryParams = mutableListOf<String>()
        if (!category.isNullOrBlank() && !category.equals("ALL", ignoreCase = true)) {
            queryParams.add("category=$category")
        }
        if (!tag.isNullOrBlank() && !tag.equals("All", ignoreCase = true)) {
            queryParams.add("tag=$tag")
        }
        val queryStr = if (queryParams.isNotEmpty()) "?" + queryParams.joinToString("&") else ""
        val fullUrl = "$base/api/avatars$queryStr"

        try {
            val url = URL(fullUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                val json = JSONObject(response)
                val itemsArray = json.optJSONArray("items")
                if (itemsArray != null) {
                    val parsed = mutableListOf<Avatar>()
                    for (i in 0 until itemsArray.length()) {
                        val item = itemsArray.getJSONObject(i)
                        val id = item.getString("id")
                        val name = item.optString("name", id)
                        val cat = item.optString("category", if (id.startsWith("girl")) "GIRL" else "BOY")
                        val imgUrl = item.optString("imageUrl", resolveAvatarUrl(id, serverUrl))
                        val thumbUrl = item.optString("thumbnailUrl", imgUrl)
                        val tagsArray = item.optJSONArray("tags")
                        val tags = mutableListOf<String>()
                        if (tagsArray != null) {
                            for (t in 0 until tagsArray.length()) {
                                tags.add(tagsArray.getString(t))
                            }
                        }
                        parsed.add(Avatar(id, name, cat, imgUrl, thumbUrl, tags))
                    }
                    if (parsed.isNotEmpty()) {
                        cachedCatalog = parsed
                        return@withContext parsed
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback to loaded catalog or curated fallback
        val source = cachedCatalog?.takeIf { it.isNotEmpty() } ?: fallbackAvatars
        source.filter { a ->
            (category.isNullOrBlank() || category.equals("ALL", ignoreCase = true) || a.category.equals(category, ignoreCase = true)) &&
            (tag.isNullOrBlank() || tag.equals("All", ignoreCase = true) || a.tags.any { it.equals(tag, ignoreCase = true) })
        }
    }

    suspend fun saveAvatarToServer(
        serverUrl: String,
        pairCode: String,
        avatarId: String
    ): Boolean = withContext(Dispatchers.IO) {
        val base = sanitizeHttpUrl(serverUrl)
        val endpoint = "$base/api/profile/me/avatar"
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val body = JSONObject().apply {
                put("pairCode", pairCode)
                put("avatarId", avatarId)
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(body.toString())
            writer.flush()
            writer.close()

            conn.responseCode in 200..299
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchDirectDpUrl(
        serverUrl: String,
        pairCode: String
    ): String? = withContext(Dispatchers.IO) {
        if (pairCode.isBlank()) return@withContext null
        val base = sanitizeHttpUrl(serverUrl)
        val endpoint = "$base/api/profile/dp/${pairCode.trim().uppercase()}"
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(text)
                if (json.optBoolean("success", false)) {
                    val urlStr = json.optString("avatarUrl", "")
                    if (urlStr.isNotBlank()) return@withContext urlStr
                }
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun uploadCustomDp(
        serverUrl: String,
        pairCode: String,
        base64Data: String,
        mimeType: String = "image/jpeg"
    ): String? = withContext(Dispatchers.IO) {
        if (pairCode.isBlank() || base64Data.isBlank()) return@withContext null
        val base = sanitizeHttpUrl(serverUrl)
        val endpoint = "$base/api/profile/dp/upload"
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val body = JSONObject().apply {
                put("pairCode", pairCode.trim().uppercase())
                put("imageBase64", base64Data)
                put("mimeType", mimeType)
            }

            OutputStreamWriter(conn.outputStream).use {
                it.write(body.toString())
                it.flush()
            }

            if (conn.responseCode in 200..299) {
                val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(text)
                if (json.optBoolean("success", false)) {
                    val resUrl = json.optString("url", json.optString("avatarUrl", ""))
                    if (resUrl.isNotBlank()) return@withContext resUrl
                }
            }
        } catch (_: Exception) {}
        null
    }

    private fun sanitizeHttpUrl(serverUrl: String): String {
        var clean = serverUrl.trim()
        if (clean.startsWith("ws://")) {
            clean = "http://" + clean.substring(5)
        } else if (clean.startsWith("wss://")) {
            clean = "https://" + clean.substring(6)
        }
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://$clean"
        }
        return clean.trimEnd('/')
    }

    companion object {
        @Volatile
        private var instance: AvatarRepository? = null

        fun getInstance(): AvatarRepository {
            return instance ?: synchronized(this) {
                instance ?: AvatarRepository().also { instance = it }
            }
        }
    }
}
