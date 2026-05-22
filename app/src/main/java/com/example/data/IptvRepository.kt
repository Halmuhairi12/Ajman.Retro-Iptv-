package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class IptvRepository(private val iptvDao: IptvDao) {

    private val client = OkHttpClient()

    // --- Playlist Access ---
    val allPlaylists: Flow<List<IptvPlaylist>> = iptvDao.getAllPlaylists()

    suspend fun addPlaylist(name: String, url: String, rawContent: String): Long {
        return withContext(Dispatchers.IO) {
            val playlist = IptvPlaylist(
                name = name,
                url = url,
                lastUpdated = System.currentTimeMillis()
            )
            val playlistId = iptvDao.insertPlaylist(playlist)
            
            // Parse and insert channels
            val channels = parseM3u(playlistId, rawContent)
            if (channels.isNotEmpty()) {
                iptvDao.insertChannels(channels)
            }
            playlistId
        }
    }

    suspend fun deletePlaylist(playlistId: Long) {
        withContext(Dispatchers.IO) {
            iptvDao.deleteChannelsByPlaylist(playlistId)
            iptvDao.deletePlaylistById(playlistId)
        }
    }

    suspend fun downloadAndAddPlaylist(name: String, url: String): Long {
        return withContext(Dispatchers.IO) {
            val m3uContent = downloadM3u(url)
            addPlaylist(name, url, m3uContent)
        }
    }

    private fun downloadM3u(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to download playlist: HTTP ${response.code}")
            }
            return response.body?.string() ?: ""
        }
    }

    // --- Channel Access ---
    val allChannels: Flow<List<IptvChannel>> = iptvDao.getAllChannels()
    val favoriteChannels: Flow<List<IptvChannel>> = iptvDao.getFavoriteChannels()
    
    fun getRecentChannels(limit: Int = 20): Flow<List<IptvChannel>> = iptvDao.getRecentChannels(limit)
    
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<IptvChannel>> = iptvDao.getChannelsByPlaylist(playlistId)

    fun getChannelsByGroup(playlistId: Long, groupTitle: String): Flow<List<IptvChannel>> {
        return iptvDao.getChannelsByGroup(playlistId, groupTitle)
    }

    fun getGroupTitles(playlistId: Long): Flow<List<String>> = iptvDao.getGroupTitles(playlistId)

    fun searchChannels(query: String): Flow<List<IptvChannel>> = iptvDao.searchChannels(query)

    suspend fun toggleFavorite(channelId: Long, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            iptvDao.updateFavoriteStatus(channelId, isFavorite)
        }
    }

    suspend fun markChannelPlayed(channelId: Long) {
        withContext(Dispatchers.IO) {
            iptvDao.updateLastPlayed(channelId, System.currentTimeMillis())
        }
    }

    // --- Core M3U Parsing Engine ---
    fun parseM3u(playlistId: Long, m3uContent: String): List<IptvChannel> {
        val channels = mutableListOf<IptvChannel>()
        val lines = m3uContent.split("\n")
        var currentName = ""
        var currentLogo: String? = null
        var currentGroup = "General"

        val logoRegex = """tvg-logo="([^"]*)"""".toRegex()
        val groupRegex = """group-title="([^"]*)"""".toRegex()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("#EXTINF:")) {
                // Parse attributes
                val logoMatch = logoRegex.find(trimmed)
                currentLogo = logoMatch?.groupValues?.get(1)

                val groupMatch = groupRegex.find(trimmed)
                currentGroup = groupMatch?.groupValues?.get(1)?.trim() ?: "General"
                if (currentGroup.isEmpty()) currentGroup = "General"

                // Extract name
                val commaIndex = trimmed.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    "Unnamed Channel"
                }
            } else if (!trimmed.startsWith("#")) {
                // Read HTTP/HTTPS stream URL
                if (currentName.isEmpty()) {
                    currentName = "Channel ${channels.size + 1}"
                }
                channels.add(
                    IptvChannel(
                        playlistId = playlistId,
                        name = currentName,
                        streamUrl = trimmed,
                        logoUrl = currentLogo,
                        groupTitle = currentGroup
                    )
                )
                // Reset attributes for next entry
                currentName = ""
                currentLogo = null
                currentGroup = "General"
            }
        }
        return channels
    }
}
