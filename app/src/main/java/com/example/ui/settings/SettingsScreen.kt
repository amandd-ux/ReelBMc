package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.LocalVideo
import com.example.data.model.VideoStats
import com.example.data.model.WatchHistoryItem
import com.example.ui.theme.DislikeGray
import com.example.ui.theme.HeartRed
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StarGold
import com.example.util.FormatUtils

@Composable
fun SettingsScreen(
    settings: AppSettings,
    stats: VideoStats,
    favorites: List<LocalVideo>,
    watchHistory: List<WatchHistoryItem>,
    dislikedVideos: List<LocalVideo>,
    hiddenVideos: List<LocalVideo>,
    onUpdateSettings: (AppSettings) -> Unit,
    onRestoreVideo: (Long) -> Unit,
    onClearWatchHistory: () -> Unit,
    onClearLikes: () -> Unit,
    onClearDislikes: () -> Unit,
    onResetRecommendationEngine: () -> Unit
) {
    val context = LocalContext.current

    var confirmDialogType by remember { mutableStateOf<String?>(null) }
    var activeSubSection by remember { mutableStateOf<String?>(null) }

    val durationOptions = listOf(
        30 to "30 sec",
        1 to "1 min",
        2 to "2 min",
        5 to "5 min",
        10 to "10 min",
        0 to "Unlimited"
    )

    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    val themeOptions = listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = NeonViolet,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Settings & Data",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // --- SECTION 1: GENERAL SETTINGS ---
        item {
            SectionHeader(title = "General Settings")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Maximum Video Duration Filter
                    Text(
                        text = "Maximum Video Duration",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Filter out videos longer than duration",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(durationOptions) { (mins, label) ->
                            val isSelected = settings.maxDurationMinutes == mins
                            Surface(
                                onClick = { onUpdateSettings(settings.copy(maxDurationMinutes = mins)) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NeonViolet else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("dur_opt_$mins")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Smart Shuffle
                    SettingSwitchRow(
                        icon = Icons.Default.Shuffle,
                        title = "Smart Shuffle",
                        subtitle = "Mix top recommendations with fresh videos",
                        checked = settings.smartShuffle,
                        onCheckedChange = { onUpdateSettings(settings.copy(smartShuffle = it)) }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Enable/Disable Loop
                    SettingSwitchRow(
                        icon = Icons.Default.Loop,
                        title = "Enable Continuous Loop",
                        subtitle = "Automatically repeat single video",
                        checked = settings.loopSingleVideo,
                        onCheckedChange = { onUpdateSettings(settings.copy(loopSingleVideo = it)) }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Show/Hide File Names
                    SettingSwitchRow(
                        icon = Icons.Default.Title,
                        title = "Show File Names",
                        subtitle = "Display video file titles on reels player",
                        checked = settings.showFileNames,
                        onCheckedChange = { onUpdateSettings(settings.copy(showFileNames = it)) }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Playback Speed Selector
                    Text(
                        text = "Playback Speed",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(speedOptions) { spd ->
                            val isSelected = settings.playbackSpeed == spd
                            Surface(
                                onClick = { onUpdateSettings(settings.copy(playbackSpeed = spd)) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NeonViolet else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            ) {
                                Text(
                                    text = "${spd}x",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Theme Selector
                    Text(
                        text = "App Theme",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        themeOptions.forEach { (modeKey, modeLabel) ->
                            val isSelected = settings.themeMode == modeKey
                            Surface(
                                onClick = { onUpdateSettings(settings.copy(themeMode = modeKey)) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NeonViolet else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: STATISTICS & DATA ---
        item {
            SectionHeader(title = "Device Video Statistics")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatGrid(stats = stats)
                }
            }
        }

        item {
            SectionHeader(title = "Data Management")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    DataClickableRow(
                        icon = Icons.Default.Star,
                        tint = StarGold,
                        title = "Favorites",
                        subtitle = "${favorites.size} saved favorite videos",
                        onClick = { activeSubSection = if (activeSubSection == "FAVORITES") null else "FAVORITES" }
                    )

                    if (activeSubSection == "FAVORITES") {
                        SubListContent(
                            items = favorites,
                            emptyMessage = "No favorite videos saved yet",
                            onRestore = null
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    DataClickableRow(
                        icon = Icons.Default.History,
                        tint = NeonViolet,
                        title = "Watch History",
                        subtitle = "${watchHistory.size} history records",
                        onClick = { activeSubSection = if (activeSubSection == "HISTORY") null else "HISTORY" }
                    )

                    if (activeSubSection == "HISTORY") {
                        HistorySubListContent(items = watchHistory)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    DataClickableRow(
                        icon = Icons.Default.VisibilityOff,
                        tint = NeonMagenta,
                        title = "Hidden Videos",
                        subtitle = "${hiddenVideos.size} videos hidden from feed",
                        onClick = { activeSubSection = if (activeSubSection == "HIDDEN") null else "HIDDEN" }
                    )

                    if (activeSubSection == "HIDDEN") {
                        SubListContent(
                            items = hiddenVideos,
                            emptyMessage = "No hidden videos",
                            onRestore = { id -> onRestoreVideo(id) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    DataClickableRow(
                        icon = Icons.Default.ThumbDown,
                        tint = DislikeGray,
                        title = "Disliked Videos",
                        subtitle = "${dislikedVideos.size} disliked videos",
                        onClick = { activeSubSection = if (activeSubSection == "DISLIKED") null else "DISLIKED" }
                    )

                    if (activeSubSection == "DISLIKED") {
                        SubListContent(
                            items = dislikedVideos,
                            emptyMessage = "No disliked videos",
                            onRestore = { id -> onRestoreVideo(id) }
                        )
                    }
                }
            }
        }

        // --- SECTION 3: MANAGEMENT & RESET ---
        item {
            SectionHeader(title = "Management Actions")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ActionRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Watch History",
                        subtitle = "Remove all watch logs and timestamps",
                        onClick = { confirmDialogType = "CLEAR_HISTORY" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    ActionRow(
                        icon = Icons.Default.Favorite,
                        title = "Clear All Likes",
                        subtitle = "Reset all liked video markers",
                        onClick = { confirmDialogType = "CLEAR_LIKES" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    ActionRow(
                        icon = Icons.Default.ThumbDown,
                        title = "Clear All Dislikes",
                        subtitle = "Restore all disliked videos back to feed",
                        onClick = { confirmDialogType = "CLEAR_DISLIKES" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    ActionRow(
                        icon = Icons.Default.RestartAlt,
                        title = "Reset Recommendation Engine",
                        subtitle = "Clear all recommendation scores and start fresh",
                        tint = Color(0xFFFF3B30),
                        onClick = { confirmDialogType = "RESET_ENGINE" }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Confirmation Dialogs
    confirmDialogType?.let { type ->
        val title = when (type) {
            "CLEAR_HISTORY" -> "Clear Watch History?"
            "CLEAR_LIKES" -> "Clear All Likes?"
            "CLEAR_DISLIKES" -> "Clear All Dislikes?"
            "RESET_ENGINE" -> "Reset Recommendation Engine?"
            else -> "Confirm Action"
        }
        val message = when (type) {
            "CLEAR_HISTORY" -> "This will permanently remove all watch history records. Your video files will not be deleted."
            "CLEAR_LIKES" -> "This will remove the liked status from all videos."
            "CLEAR_DISLIKES" -> "This will clear all dislikes and restore those videos to your main feed."
            "RESET_ENGINE" -> "This will reset all watch counts and recommendation scores back to default."
            else -> "Are you sure you want to proceed?"
        }

        AlertDialog(
            onDismissRequest = { confirmDialogType = null },
            title = { Text(text = title, fontWeight = FontWeight.Bold) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (type) {
                            "CLEAR_HISTORY" -> {
                                onClearWatchHistory()
                                Toast.makeText(context, "Watch history cleared", Toast.LENGTH_SHORT).show()
                            }
                            "CLEAR_LIKES" -> {
                                onClearLikes()
                                Toast.makeText(context, "Likes cleared", Toast.LENGTH_SHORT).show()
                            }
                            "CLEAR_DISLIKES" -> {
                                onClearDislikes()
                                Toast.makeText(context, "Dislikes cleared", Toast.LENGTH_SHORT).show()
                            }
                            "RESET_ENGINE" -> {
                                onResetRecommendationEngine()
                                Toast.makeText(context, "Recommendation engine reset", Toast.LENGTH_SHORT).show()
                            }
                        }
                        confirmDialogType = null
                    }
                ) {
                    Text("Confirm", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDialogType = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = NeonViolet,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = NeonViolet, checkedTrackColor = NeonViolet.copy(alpha = 0.4f))
        )
    }
}

@Composable
private fun StatGrid(stats: VideoStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(title = "Total Videos", value = "${stats.totalVideos}", modifier = Modifier.weight(1f))
            StatCard(title = "Total Duration", value = FormatUtils.formatDuration(stats.totalDurationMs), modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(title = "Liked Videos", value = "${stats.likedCount}", color = HeartRed, modifier = Modifier.weight(1f))
            StatCard(title = "Favorite Videos", value = "${stats.favoriteCount}", color = StarGold, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(title = "Watched Videos", value = "${stats.watchedCount}", modifier = Modifier.weight(1f))
            StatCard(title = "Disliked / Hidden", value = "${stats.dislikedCount + stats.hiddenCount}", color = DislikeGray, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color = NeonViolet, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DataClickableRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = tint)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun SubListContent(
    items: List<LocalVideo>,
    emptyMessage: String,
    onRestore: ((Long) -> Unit)?
) {
    if (items.isEmpty()) {
        Text(
            text = emptyMessage,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(16.dp)
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            items.take(10).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = FormatUtils.formatDuration(item.durationMs),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    if (onRestore != null) {
                        Surface(
                            onClick = { onRestore(item.id) },
                            shape = RoundedCornerShape(8.dp),
                            color = NeonViolet.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Undo, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", fontSize = 11.sp, color = NeonViolet, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySubListContent(items: List<WatchHistoryItem>) {
    if (items.isEmpty()) {
        Text(
            text = "No watch history recorded yet",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(16.dp)
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            items.take(10).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${FormatUtils.formatDate(item.watchedTimestamp)} · Watched ${FormatUtils.formatDuration(item.watchDurationMs)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
