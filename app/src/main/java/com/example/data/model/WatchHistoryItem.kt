package com.example.data.model

import android.net.Uri

data class WatchHistoryItem(
    val id: Long,
    val videoId: Long,
    val contentUri: Uri,
    val displayName: String,
    val filePath: String,
    val durationMs: Long,
    val watchedTimestamp: Long,
    val watchDurationMs: Long,
    val completed: Boolean
)
