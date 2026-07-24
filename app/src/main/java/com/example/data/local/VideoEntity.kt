package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: Long,
    val contentUri: String,
    val filePath: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAdded: Long,
    val width: Int,
    val height: Int,
    val bucketName: String,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val watchCount: Int = 0,
    val completionCount: Int = 0,
    val totalWatchTimeMs: Long = 0L,
    val lastWatchedTimestamp: Long = 0L,
    val lastPlaybackPositionMs: Long = 0L,
    val recommendationScore: Float = 50f
)
