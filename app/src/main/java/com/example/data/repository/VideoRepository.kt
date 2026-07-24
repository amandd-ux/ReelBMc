package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsEntity
import com.example.data.local.VideoEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.AppSettings
import com.example.data.model.LocalVideo
import com.example.data.model.VideoStats
import com.example.data.model.WatchHistoryItem
import com.example.data.scanner.VideoScanner
import com.example.util.RecommendationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VideoRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val videoDao = db.videoDao()
    private val scanner = VideoScanner(context, videoDao)

    val settingsFlow: Flow<AppSettings> = videoDao.getSettingsFlow().map { entity ->
        entity?.toAppSettings() ?: AppSettings()
    }

    val feedVideosFlow: Flow<List<LocalVideo>> = videoDao.getFeedVideos().map { list ->
        list.map { it.toLocalVideo() }
    }

    val exploreVideosFlow: Flow<List<LocalVideo>> = videoDao.getExploreVideos().map { list ->
        list.map { it.toLocalVideo() }
    }

    val favoriteVideosFlow: Flow<List<LocalVideo>> = videoDao.getFavoriteVideos().map { list ->
        list.map { it.toLocalVideo() }
    }

    val likedVideosFlow: Flow<List<LocalVideo>> = videoDao.getLikedVideos().map { list ->
        list.map { it.toLocalVideo() }
    }

    val dislikedVideosFlow: Flow<List<LocalVideo>> = videoDao.getDislikedVideos().map { list ->
        list.map { it.toLocalVideo() }
    }

    val hiddenVideosFlow: Flow<List<LocalVideo>> = videoDao.getHiddenVideos().map { list ->
        list.map { it.toLocalVideo() }
    }

    val watchHistoryFlow: Flow<List<WatchHistoryItem>> = videoDao.getWatchHistory().map { historyEntities ->
        val videoMap = videoDao.getAllVideosOnce().associateBy { it.id }
        historyEntities.map { h ->
            val v = videoMap[h.videoId]
            WatchHistoryItem(
                id = h.id,
                videoId = h.videoId,
                contentUri = v?.contentUri?.let { Uri.parse(it) } ?: Uri.EMPTY,
                displayName = v?.displayName ?: "Video #${h.videoId}",
                filePath = v?.filePath ?: "",
                durationMs = v?.durationMs ?: 0L,
                watchedTimestamp = h.watchedTimestamp,
                watchDurationMs = h.watchDurationMs,
                completed = h.completed
            )
        }
    }

    val statsFlow: Flow<VideoStats> = videoDao.getAllVideosFlow().map { allVideos ->
        var totalDur = 0L
        var likedCount = 0
        var dislikedCount = 0
        var watchedCount = 0
        var favCount = 0
        var hiddenCount = 0

        for (v in allVideos) {
            totalDur += v.durationMs
            if (v.isLiked) likedCount++
            if (v.isDisliked) dislikedCount++
            if (v.watchCount > 0) watchedCount++
            if (v.isFavorite) favCount++
            if (v.isHidden) hiddenCount++
        }

        VideoStats(
            totalVideos = allVideos.size,
            totalDurationMs = totalDur,
            likedCount = likedCount,
            dislikedCount = dislikedCount,
            watchedCount = watchedCount,
            favoriteCount = favCount,
            hiddenCount = hiddenCount
        )
    }

    suspend fun scanVideos() = withContext(Dispatchers.IO) {
        val currentSettings = settingsFlow.firstOrNull() ?: AppSettings()
        scanner.scanAndSync(currentSettings.maxDurationMinutes, currentSettings.smartShuffle)
    }

    suspend fun toggleLike(videoId: Long) = withContext(Dispatchers.IO) {
        val v = videoDao.getVideoById(videoId) ?: return@withContext
        val newLiked = !v.isLiked
        val newDisliked = if (newLiked) false else v.isDisliked
        val updated = v.copy(isLiked = newLiked, isDisliked = newDisliked)
        val currentSettings = settingsFlow.firstOrNull() ?: AppSettings()
        val newScore = RecommendationEngine.calculateScore(updated, currentSettings.smartShuffle)
        videoDao.updateVideo(updated.copy(recommendationScore = newScore))
    }

    suspend fun toggleDislike(videoId: Long) = withContext(Dispatchers.IO) {
        val v = videoDao.getVideoById(videoId) ?: return@withContext
        val newDisliked = !v.isDisliked
        val newLiked = if (newDisliked) false else v.isLiked
        val updated = v.copy(isDisliked = newDisliked, isLiked = newLiked)
        val currentSettings = settingsFlow.firstOrNull() ?: AppSettings()
        val newScore = RecommendationEngine.calculateScore(updated, currentSettings.smartShuffle)
        videoDao.updateVideo(updated.copy(recommendationScore = newScore))
    }

    suspend fun toggleFavorite(videoId: Long) = withContext(Dispatchers.IO) {
        val v = videoDao.getVideoById(videoId) ?: return@withContext
        val updated = v.copy(isFavorite = !v.isFavorite)
        val currentSettings = settingsFlow.firstOrNull() ?: AppSettings()
        val newScore = RecommendationEngine.calculateScore(updated, currentSettings.smartShuffle)
        videoDao.updateVideo(updated.copy(recommendationScore = newScore))
    }

    suspend fun hideFromFeed(videoId: Long) = withContext(Dispatchers.IO) {
        val v = videoDao.getVideoById(videoId) ?: return@withContext
        val updated = v.copy(isHidden = true)
        videoDao.updateVideo(updated)
    }

    suspend fun restoreVideo(videoId: Long) = withContext(Dispatchers.IO) {
        val v = videoDao.getVideoById(videoId) ?: return@withContext
        val updated = v.copy(isHidden = false, isDisliked = false)
        val currentSettings = settingsFlow.firstOrNull() ?: AppSettings()
        val newScore = RecommendationEngine.calculateScore(updated, currentSettings.smartShuffle)
        videoDao.updateVideo(updated.copy(recommendationScore = newScore))
    }

    suspend fun recordWatchProgress(
        videoId: Long,
        positionMs: Long,
        totalDurationMs: Long,
        watchedTimeIncrementMs: Long
    ) = withContext(Dispatchers.IO) {
        val v = videoDao.getVideoById(videoId) ?: return@withContext
        val isCompleted = totalDurationMs > 0 && positionMs >= (totalDurationMs - 1500)
        val newWatchCount = v.watchCount + 1
        val newCompletionCount = if (isCompleted) v.completionCount + 1 else v.completionCount
        val newTotalWatchTime = v.totalWatchTimeMs + watchedTimeIncrementMs
        val now = System.currentTimeMillis()

        val updated = v.copy(
            watchCount = newWatchCount,
            completionCount = newCompletionCount,
            totalWatchTimeMs = newTotalWatchTime,
            lastWatchedTimestamp = now,
            lastPlaybackPositionMs = positionMs
        )
        val currentSettings = settingsFlow.firstOrNull() ?: AppSettings()
        val newScore = RecommendationEngine.calculateScore(updated, currentSettings.smartShuffle)
        videoDao.updateVideo(updated.copy(recommendationScore = newScore))

        // Save Watch History log
        videoDao.insertWatchHistory(
            WatchHistoryEntity(
                videoId = videoId,
                watchedTimestamp = now,
                watchDurationMs = watchedTimeIncrementMs,
                completed = isCompleted
            )
        )

        // Save last watched position in settings
        val currentSettingsEntity = videoDao.getSettingsOnce() ?: SettingsEntity()
        videoDao.insertOrUpdateSettings(
            currentSettingsEntity.copy(
                lastWatchedVideoId = videoId,
                lastWatchedPositionMs = positionMs
            )
        )
    }

    suspend fun updateAppSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        videoDao.insertOrUpdateSettings(
            SettingsEntity(
                id = 1,
                maxDurationMinutes = settings.maxDurationMinutes,
                smartShuffle = settings.smartShuffle,
                loopSingleVideo = settings.loopSingleVideo,
                playbackSpeed = settings.playbackSpeed,
                themeMode = settings.themeMode,
                showFileNames = settings.showFileNames,
                lastWatchedVideoId = settings.lastWatchedVideoId,
                lastWatchedPositionMs = settings.lastWatchedPositionMs
            )
        )
        scanVideos()
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        videoDao.clearWatchHistory()
    }

    suspend fun clearLikes() = withContext(Dispatchers.IO) {
        videoDao.clearAllLikes()
    }

    suspend fun clearDislikes() = withContext(Dispatchers.IO) {
        videoDao.clearAllDislikes()
    }

    suspend fun resetRecommendationEngine() = withContext(Dispatchers.IO) {
        videoDao.resetRecommendationEngine()
        scanVideos()
    }

    suspend fun deleteVideoRecord(videoId: Long) = withContext(Dispatchers.IO) {
        videoDao.deleteVideoById(videoId)
    }

    private fun VideoEntity.toLocalVideo(): LocalVideo {
        return LocalVideo(
            id = id,
            contentUri = Uri.parse(contentUri),
            filePath = filePath,
            displayName = displayName,
            durationMs = durationMs,
            sizeBytes = sizeBytes,
            dateAdded = dateAdded,
            width = width,
            height = height,
            bucketName = bucketName,
            isLiked = isLiked,
            isDisliked = isDisliked,
            isFavorite = isFavorite,
            isHidden = isHidden,
            watchCount = watchCount,
            completionCount = completionCount,
            totalWatchTimeMs = totalWatchTimeMs,
            lastWatchedTimestamp = lastWatchedTimestamp,
            lastPlaybackPositionMs = lastPlaybackPositionMs,
            recommendationScore = recommendationScore
        )
    }

    private fun SettingsEntity.toAppSettings(): AppSettings {
        return AppSettings(
            maxDurationMinutes = maxDurationMinutes,
            smartShuffle = smartShuffle,
            loopSingleVideo = loopSingleVideo,
            playbackSpeed = playbackSpeed,
            themeMode = themeMode,
            showFileNames = showFileNames,
            lastWatchedVideoId = lastWatchedVideoId,
            lastWatchedPositionMs = lastWatchedPositionMs
        )
    }
}
