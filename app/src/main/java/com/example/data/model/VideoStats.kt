package com.example.data.model

data class VideoStats(
    val totalVideos: Int = 0,
    val totalDurationMs: Long = 0L,
    val likedCount: Int = 0,
    val dislikedCount: Int = 0,
    val watchedCount: Int = 0,
    val favoriteCount: Int = 0,
    val hiddenCount: Int = 0
)
