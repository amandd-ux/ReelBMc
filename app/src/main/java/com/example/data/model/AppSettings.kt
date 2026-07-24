package com.example.data.model

data class AppSettings(
    val maxDurationMinutes: Int = 2, // 2 minutes default; 0 means Unlimited
    val smartShuffle: Boolean = true,
    val loopSingleVideo: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val showFileNames: Boolean = true,
    val lastWatchedVideoId: Long? = null,
    val lastWatchedPositionMs: Long = 0L
)
