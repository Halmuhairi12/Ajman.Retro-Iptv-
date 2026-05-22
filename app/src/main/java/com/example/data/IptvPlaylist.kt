package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class IptvPlaylist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String, // Can be empty for custom/manual lists
    val isSample: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
