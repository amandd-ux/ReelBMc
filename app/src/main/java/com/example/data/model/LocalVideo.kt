package com.example.data.model

import android.net.Uri

data class LocalVideo(
    val id: Long,
    val contentUri: Uri,
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
