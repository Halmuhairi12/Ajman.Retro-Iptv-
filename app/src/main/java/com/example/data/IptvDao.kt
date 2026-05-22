package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IptvDao {

    // --- Playlist Queries ---
    @Query("SELECT * FROM playlists ORDER BY isSample DESC, lastUpdated DESC")
    fun getAllPlaylists(): Flow<List<IptvPlaylist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: IptvPlaylist): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Long): IptvPlaylist?

    // --- Channel Queries ---
    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<IptvChannel>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY name ASC")
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<IptvChannel>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND groupTitle = :groupTitle ORDER BY name ASC")
    fun getChannelsByGroup(playlistId: Long, groupTitle: String): Flow<List<IptvChannel>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<IptvChannel>>

    @Query("SELECT * FROM channels WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentChannels(limit: Int): Flow<List<IptvChannel>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchChannels(query: String): Flow<List<IptvChannel>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE playlistId = :playlistId ORDER BY groupTitle ASC")
    fun getGroupTitles(playlistId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<IptvChannel>)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    @Update
    suspend fun updateChannel(channel: IptvChannel)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun updateFavoriteStatus(channelId: Long, isFavorite: Boolean)

    @Query("UPDATE channels SET lastPlayed = :lastPlayed WHERE id = :channelId")
    suspend fun updateLastPlayed(channelId: Long, lastPlayed: Long)

    @Query("SELECT * FROM channels WHERE id = :channelId LIMIT 1")
    suspend fun getChannelById(channelId: Long): IptvChannel?
}
