package com.example.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.LocalVideo
import com.example.ui.reels.ReelsScreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StarGold
import com.example.util.FormatUtils

data class ExploreCategory(
    val id: String,
    val name: String,
    val icon: ImageVector
)

val exploreCategories = listOf(
    ExploreCategory("FOR_YOU", "For You", Icons.Default.AutoAwesome),
    ExploreCategory("MOST_LIKED", "Most Liked", Icons.Default.Favorite),
    ExploreCategory("RECENT", "Newly Added", Icons.Default.TrendingUp),
    ExploreCategory("SHORTS", "Quick Clips", Icons.Default.Schedule),
    ExploreCategory("UNWATCHED", "Unwatched", Icons.Default.Star)
)

@Composable
fun ExploreScreen(
    videos: List<LocalVideo>,
    isLoading: Boolean,
    settings: AppSettings,
    onLikeToggle: (Long) -> Unit,
    onDislikeToggle: (Long) -> Unit,
    onFavoriteToggle: (Long) -> Unit,
    onHideFromFeed: (Long) -> Unit,
    onDeleteSuccess: (Long) -> Unit,
    onRecordProgress: (videoId: Long, positionMs: Long, totalDurationMs: Long, watchTimeIncrementMs: Long) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("FOR_YOU") }
    var selectedVideoForReel by remember { mutableStateOf<LocalVideo?>(null) }

    val filteredVideos = remember(videos, selectedCategory) {
        when (selectedCategory) {
            "MOST_LIKED" -> videos.filter { it.isLiked || it.isFavorite }.ifEmpty { videos }
            "RECENT" -> videos.sortedByDescending { it.dateAdded }
            "SHORTS" -> videos.filter { it.durationMs in 1..60000 }.ifEmpty { videos }
            "UNWATCHED" -> videos.filter { it.watchCount == 0 }.ifEmpty { videos }
            else -> videos.sortedByDescending { it.recommendationScore }
        }
    }

    if (selectedVideoForReel != null) {
        val startIndex = filteredVideos.indexOfFirst { it.id == selectedVideoForReel?.id }.coerceAtLeast(0)
        val orderedList = filteredVideos.subList(startIndex, filteredVideos.size) + filteredVideos.subList(0, startIndex)

        Box(modifier = Modifier.fillMaxSize()) {
            ReelsScreen(
                videos = orderedList,
                isLoading = isLoading,
                settings = settings,
                onScanRequest = {},
                onLikeToggle = onLikeToggle,
                onDislikeToggle = onDislikeToggle,
                onFavoriteToggle = onFavoriteToggle,
                onHideFromFeed = onHideFromFeed,
                onDeleteSuccess = onDeleteSuccess,
                onRecordProgress = onRecordProgress
            )

            // Back button to close reel view
            Surface(
                onClick = { selectedVideoForReel = null },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(start = 16.dp, top = 48.dp)
                    .align(Alignment.TopStart)
                    .testTag("explore_back_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("← Explore", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Explore Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NeonViolet.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonMagenta,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Explore & Recommended",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Smart offline recommendations based on your viewing history and likes",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(exploreCategories) { cat ->
                    val isSelected = selectedCategory == cat.id
                    Surface(
                        onClick = { selectedCategory = cat.id },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) NeonViolet else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("chip_${cat.id.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.name,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Video Feed List / Cards
            if (filteredVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No videos in this category yet.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                ReelsScreen(
                    videos = filteredVideos,
                    isLoading = isLoading,
                    settings = settings,
                    onScanRequest = {},
                    onLikeToggle = onLikeToggle,
                    onDislikeToggle = onDislikeToggle,
                    onFavoriteToggle = onFavoriteToggle,
                    onHideFromFeed = onHideFromFeed,
                    onDeleteSuccess = onDeleteSuccess,
                    onRecordProgress = onRecordProgress
                )
            }
        }
    }
}
