package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos WHERE isDisliked = 0 AND isHidden = 0")
    fun getFeedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDisliked = 0 AND isHidden = 0 ORDER BY recommendationScore DESC")
    fun getExploreVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isFavorite = 1 AND isDisliked = 0 AND isHidden = 0 ORDER BY dateAdded DESC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isLiked = 1 ORDER BY dateAdded DESC")
    fun getLikedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDisliked = 1 ORDER BY dateAdded DESC")
    fun getDislikedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isHidden = 1 ORDER BY dateAdded DESC")
    fun getHiddenVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoEntity?

    @Query("SELECT * FROM videos")
    suspend fun getAllVideosOnce(): List<VideoEntity>

    @Query("SELECT * FROM videos")
    fun getAllVideosFlow(): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Update
    suspend fun updateVideos(videos: List<VideoEntity>)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("DELETE FROM videos WHERE id NOT IN (:validIds)")
    suspend fun deleteMissingVideos(validIds: List<Long>)

    // History DAO methods
    @Query("SELECT * FROM watch_history ORDER BY watchedTimestamp DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()

    @Query("DELETE FROM watch_history WHERE id = :historyId")
    suspend fun deleteWatchHistoryItem(historyId: Long)

    // Settings DAO methods
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsEntity)

    // Batch management
    @Query("UPDATE videos SET isLiked = 0")
    suspend fun clearAllLikes()

    @Query("UPDATE videos SET isDisliked = 0")
    suspend fun clearAllDislikes()

    @Query("UPDATE videos SET isHidden = 0")
    suspend fun restoreAllHidden()

    @Query("UPDATE videos SET watchCount = 0, completionCount = 0, totalWatchTimeMs = 0, lastWatchedTimestamp = 0, lastPlaybackPositionMs = 0, recommendationScore = 50")
    suspend fun resetRecommendationEngine()
}
