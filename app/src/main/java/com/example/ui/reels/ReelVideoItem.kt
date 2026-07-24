package com.example.ui.reels

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.LocalVideo
import com.example.ui.components.HeartBurstOverlay
import com.example.ui.components.HeartEffect
import com.example.ui.theme.DislikeGray
import com.example.ui.theme.HeartRed
import com.example.ui.theme.StarGold
import com.example.util.FormatUtils
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun ReelVideoItem(
    video: LocalVideo,
    isCurrentPage: Boolean,
    isLooping: Boolean,
    showFileName: Boolean,
    playbackSpeed: Float,
    onLikeToggle: () -> Unit,
    onDislikeToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onMenuClick: () -> Unit,
    onRecordProgress: (videoId: Long, positionMs: Long, totalDurationMs: Long, watchTimeIncrementMs: Long) -> Unit
) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    var showPlayPauseIndicator by remember { mutableStateOf(false) }
    var heartEffect by remember { mutableStateOf<HeartEffect?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentPositionMs by remember { mutableStateOf(0L) }

    val exoPlayer = remember(video.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.contentUri))
            prepare()
            repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            playbackParameters = playbackParameters.withSpeed(playbackSpeed)
            if (video.lastPlaybackPositionMs > 0 && video.lastPlaybackPositionMs < video.durationMs) {
                seekTo(video.lastPlaybackPositionMs)
            }
        }
    }

    DisposableEffect(video.id) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(isLooping) {
        exoPlayer.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = exoPlayer.playbackParameters.withSpeed(playbackSpeed)
    }

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            exoPlayer.playWhenReady = true
            isPlaying = true
        } else {
            exoPlayer.playWhenReady = false
            isPlaying = false
        }
    }

    // Periodic progress updates & watch tracking
    LaunchedEffect(isCurrentPage, isPlaying) {
        var lastTrackedTime = System.currentTimeMillis()
        while (isCurrentPage && exoPlayer.isPlaying) {
            currentPositionMs = exoPlayer.currentPosition
            val totalDur = exoPlayer.duration.coerceAtLeast(1L)
            progress = (currentPositionMs.toFloat() / totalDur.toFloat()).coerceIn(0f, 1f)

            val now = System.currentTimeMillis()
            val elapsed = now - lastTrackedTime
            if (elapsed >= 1000) {
                onRecordProgress(video.id, currentPositionMs, totalDur, elapsed)
                lastTrackedTime = now
            }
            delay(300)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(video.id) {
                detectTapGestures(
                    onTap = {
                        isPlaying = !isPlaying
                        exoPlayer.playWhenReady = isPlaying
                        showPlayPauseIndicator = true
                    },
                    onDoubleTap = { offset ->
                        heartEffect = HeartEffect(offset = offset)
                        if (!video.isLiked) {
                            onLikeToggle()
                        }
                    }
                )
            }
    ) {
        // Full screen video surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Gradient & Bottom Gradient Overlays
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Play / Pause central indicator flash
        LaunchedEffect(showPlayPauseIndicator) {
            if (showPlayPauseIndicator) {
                delay(600)
                showPlayPauseIndicator = false
            }
        }

        AnimatedVisibility(
            visible = showPlayPauseIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Heart burst animation overlay on double tap
        HeartBurstOverlay(heart = heartEffect) {
            heartEffect = null
        }

        // Right Action Buttons Stack
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Button
            ActionButton(
                icon = if (video.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                label = if (video.isLiked) "Liked" else "Like",
                tint = if (video.isLiked) HeartRed else Color.White,
                tag = "like_button",
                onClick = onLikeToggle
            )

            // Dislike Button
            ActionButton(
                icon = if (video.isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                label = "Dislike",
                tint = if (video.isDisliked) DislikeGray else Color.White,
                tag = "dislike_button",
                onClick = onDislikeToggle
            )

            // Favorite Button
            ActionButton(
                icon = if (video.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                label = if (video.isFavorite) "Saved" else "Favorite",
                tint = if (video.isFavorite) StarGold else Color.White,
                tag = "favorite_button",
                onClick = onFavoriteToggle
            )

            // Three-dot menu button
            ActionButton(
                icon = Icons.Default.MoreVert,
                label = "More",
                tint = Color.White,
                tag = "menu_button",
                onClick = onMenuClick
            )
        }

        // Bottom Left Info Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.75f)
                .padding(start = 16.dp, bottom = 32.dp)
        ) {
            if (showFileName) {
                Text(
                    text = video.displayName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = video.bucketName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "${FormatUtils.formatDuration(currentPositionMs)} / ${FormatUtils.formatDuration(video.durationMs)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom Progress Indicator
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter),
            color = HeartRed,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pointerInput(Unit) {}
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onClick() })
                }
                .testTag(tag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
