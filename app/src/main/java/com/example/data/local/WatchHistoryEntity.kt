package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: Long,
    val watchedTimestamp: Long = System.currentTimeMillis(),
    val watchDurationMs: Long = 0L,
    val completed: Boolean = false
)
