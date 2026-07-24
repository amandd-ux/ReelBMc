package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.model.LocalVideo
import com.example.data.model.VideoStats
import com.example.data.model.WatchHistoryItem
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val settings: StateFlow<AppSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    val feedVideos: StateFlow<List<LocalVideo>> = repository.feedVideosFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val exploreVideos: StateFlow<List<LocalVideo>> = repository.exploreVideosFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteVideos: StateFlow<List<LocalVideo>> = repository.favoriteVideosFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val watchHistory: StateFlow<List<WatchHistoryItem>> = repository.watchHistoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dislikedVideos: StateFlow<List<LocalVideo>> = repository.dislikedVideosFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val hiddenVideos: StateFlow<List<LocalVideo>> = repository.hiddenVideosFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats: StateFlow<VideoStats> = repository.statsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VideoStats()
    )

    fun scanDeviceVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.scanVideos()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(videoId: Long) {
        viewModelScope.launch { repository.toggleLike(videoId) }
    }

    fun toggleDislike(videoId: Long) {
        viewModelScope.launch { repository.toggleDislike(videoId) }
    }

    fun toggleFavorite(videoId: Long) {
        viewModelScope.launch { repository.toggleFavorite(videoId) }
    }

    fun hideFromFeed(videoId: Long) {
        viewModelScope.launch { repository.hideFromFeed(videoId) }
    }

    fun restoreVideo(videoId: Long) {
        viewModelScope.launch { repository.restoreVideo(videoId) }
    }

    fun recordWatchProgress(videoId: Long, positionMs: Long, totalDurationMs: Long, watchTimeIncrementMs: Long) {
        viewModelScope.launch {
            repository.recordWatchProgress(videoId, positionMs, totalDurationMs, watchTimeIncrementMs)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch { repository.updateAppSettings(newSettings) }
    }

    fun clearWatchHistory() {
        viewModelScope.launch { repository.clearWatchHistory() }
    }

    fun clearLikes() {
        viewModelScope.launch { repository.clearLikes() }
    }

    fun clearDislikes() {
        viewModelScope.launch { repository.clearDislikes() }
    }

    fun resetRecommendationEngine() {
        viewModelScope.launch { repository.resetRecommendationEngine() }
    }

    fun onDeleteSuccess(videoId: Long) {
        viewModelScope.launch { repository.deleteVideoRecord(videoId) }
    }
}
