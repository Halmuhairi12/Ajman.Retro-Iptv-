package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class IptvChannel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String = "General",
    val isFavorite: Boolean = false,
    val lastPlayed: Long = 0L // 0 means never played, used for recents
)
