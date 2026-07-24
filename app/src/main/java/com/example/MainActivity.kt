package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.PermissionHandler
import com.example.ui.explore.ExploreScreen
import com.example.ui.navigation.Screen
import com.example.ui.reels.ReelsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ReelLocalTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
            val feedVideos by viewModel.feedVideos.collectAsStateWithLifecycle()
            val exploreVideos by viewModel.exploreVideos.collectAsStateWithLifecycle()
            val favoriteVideos by viewModel.favoriteVideos.collectAsStateWithLifecycle()
            val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
            val dislikedVideos by viewModel.dislikedVideos.collectAsStateWithLifecycle()
            val hiddenVideos by viewModel.hiddenVideos.collectAsStateWithLifecycle()
            val stats by viewModel.stats.collectAsStateWithLifecycle()

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Reels) }

            val items = listOf(Screen.Reels, Screen.Explore, Screen.Settings)

            ReelLocalTheme(themeMode = settings.themeMode) {
                PermissionHandler(
                    onPermissionGranted = {
                        viewModel.scanDeviceVideos()
                    }
                ) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = Color.Black.copy(alpha = 0.95f),
                                contentColor = Color.White,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                items.forEach { screen ->
                                    val isSelected = currentScreen.route == screen.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = screen },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = Color.White,
                                            indicatorColor = NeonViolet,
                                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                            unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.testTag("nav_${screen.route}")
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        ScaffoldContent(
                            modifier = Modifier.padding(innerPadding),
                            currentScreen = currentScreen,
                            viewModel = viewModel,
                            settings = settings,
                            isLoading = isLoading,
                            feedVideos = feedVideos,
                            exploreVideos = exploreVideos,
                            favoriteVideos = favoriteVideos,
                            watchHistory = watchHistory,
                            dislikedVideos = dislikedVideos,
                            hiddenVideos = hiddenVideos,
                            stats = stats
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScaffoldContent(
    modifier: Modifier = Modifier,
    currentScreen: Screen,
    viewModel: MainViewModel,
    settings: com.example.data.model.AppSettings,
    isLoading: Boolean,
    feedVideos: List<com.example.data.model.LocalVideo>,
    exploreVideos: List<com.example.data.model.LocalVideo>,
    favoriteVideos: List<com.example.data.model.LocalVideo>,
    watchHistory: List<com.example.data.model.WatchHistoryItem>,
    dislikedVideos: List<com.example.data.model.LocalVideo>,
    hiddenVideos: List<com.example.data.model.LocalVideo>,
    stats: com.example.data.model.VideoStats
) {
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.Reels -> {
                ReelsScreen(
                    videos = feedVideos,
                    isLoading = isLoading,
                    settings = settings,
                    onScanRequest = { viewModel.scanDeviceVideos() },
                    onLikeToggle = { id -> viewModel.toggleLike(id) },
                    onDislikeToggle = { id -> viewModel.toggleDislike(id) },
                    onFavoriteToggle = { id -> viewModel.toggleFavorite(id) },
                    onHideFromFeed = { id -> viewModel.hideFromFeed(id) },
                    onDeleteSuccess = { id -> viewModel.onDeleteSuccess(id) },
                    onRecordProgress = { id, pos, total, elapsed ->
                        viewModel.recordWatchProgress(id, pos, total, elapsed)
                    }
                )
            }
            Screen.Explore -> {
                ExploreScreen(
                    videos = exploreVideos,
                    isLoading = isLoading,
                    settings = settings,
                    onLikeToggle = { id -> viewModel.toggleLike(id) },
                    onDislikeToggle = { id -> viewModel.toggleDislike(id) },
                    onFavoriteToggle = { id -> viewModel.toggleFavorite(id) },
                    onHideFromFeed = { id -> viewModel.hideFromFeed(id) },
                    onDeleteSuccess = { id -> viewModel.onDeleteSuccess(id) },
                    onRecordProgress = { id, pos, total, elapsed ->
                        viewModel.recordWatchProgress(id, pos, total, elapsed)
                    }
                )
            }
            Screen.Settings -> {
                SettingsScreen(
                    settings = settings,
                    stats = stats,
                    favorites = favoriteVideos,
                    watchHistory = watchHistory,
                    dislikedVideos = dislikedVideos,
                    hiddenVideos = hiddenVideos,
                    onUpdateSettings = { newS -> viewModel.updateSettings(newS) },
                    onRestoreVideo = { id -> viewModel.restoreVideo(id) },
                    onClearWatchHistory = { viewModel.clearWatchHistory() },
                    onClearLikes = { viewModel.clearLikes() },
                    onClearDislikes = { viewModel.clearDislikes() },
                    onResetRecommendationEngine = { viewModel.resetRecommendationEngine() }
                )
            }
        }
    }
}
