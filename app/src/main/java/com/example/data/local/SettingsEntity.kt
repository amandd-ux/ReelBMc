package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val maxDurationMinutes: Int = 2,
    val smartShuffle: Boolean = true,
    val loopSingleVideo: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val themeMode: String = "SYSTEM",
    val showFileNames: Boolean = true,
    val lastWatchedVideoId: Long? = null,
    val lastWatchedPositionMs: Long = 0L
)
