package com.example.ui.reels

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.LocalVideo
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonViolet
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ReelsScreen(
    videos: List<LocalVideo>,
    isLoading: Boolean,
    settings: AppSettings,
    onScanRequest: () -> Unit,
    onLikeToggle: (Long) -> Unit,
    onDislikeToggle: (Long) -> Unit,
    onFavoriteToggle: (Long) -> Unit,
    onHideFromFeed: (Long) -> Unit,
    onDeleteSuccess: (Long) -> Unit,
    onRecordProgress: (videoId: Long, positionMs: Long, totalDurationMs: Long, watchTimeIncrementMs: Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedMenuVideo by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedInfoVideo by remember { mutableStateOf<LocalVideo?>(null) }
    var singleLoopVideoId by remember { mutableStateOf<Long?>(null) }
    var videoToDelete by remember { mutableStateOf<LocalVideo?>(null) }

    // MediaStore delete request launcher for Android 11+ (API 30+)
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            videoToDelete?.let { v ->
                onDeleteSuccess(v.id)
                Toast.makeText(context, "Video deleted successfully", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Delete canceled", Toast.LENGTH_SHORT).show()
        }
        videoToDelete = null
    }

    fun handleDeleteVideo(video: LocalVideo) {
        videoToDelete = video
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video.id)
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, listOf(uri))
                deleteLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to create delete request", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Fallback for older Android versions
            try {
                context.contentResolver.delete(video.contentUri, null, null)
                onDeleteSuccess(video.id)
                Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Could not delete file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = NeonViolet)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Scanning local videos...", color = Color.White)
            }
        }
    } else if (videos.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0C20), Color(0xFF1F1A3A))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonViolet, NeonMagenta)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Videos Found",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            val maxDurLabel = if (settings.maxDurationMinutes > 0) "${settings.maxDurationMinutes} minutes" else "Unlimited"
            Text(
                text = "No local videos match your current filter (Max duration: $maxDurLabel). Try adding videos to your phone or increasing max duration in Settings.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onScanRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("rescan_videos_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rescan Device Videos", fontWeight = FontWeight.SemiBold)
            }
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { videos.size })

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val video = videos[page]
            val isCurrentPage = pagerState.currentPage == page
            val isLooping = settings.loopSingleVideo || (singleLoopVideoId == video.id)

            ReelVideoItem(
                video = video,
                isCurrentPage = isCurrentPage,
                isLooping = isLooping,
                showFileName = settings.showFileNames,
                playbackSpeed = settings.playbackSpeed,
                onLikeToggle = { onLikeToggle(video.id) },
                onDislikeToggle = {
                    onDislikeToggle(video.id)
                    Toast.makeText(context, "Video hidden from feed", Toast.LENGTH_SHORT).show()
                },
                onFavoriteToggle = { onFavoriteToggle(video.id) },
                onMenuClick = { selectedMenuVideo = video },
                onRecordProgress = onRecordProgress
            )
        }

        // Three-Dot Menu Bottom Sheet
        selectedMenuVideo?.let { video ->
            VideoMenuBottomSheet(
                video = video,
                isLooping = settings.loopSingleVideo || (singleLoopVideoId == video.id),
                onDismiss = { selectedMenuVideo = null },
                onSurpriseMe = {
                    if (videos.size > 1) {
                        val randomIndex = Random.nextInt(videos.size)
                        scope.launch {
                            pagerState.animateScrollToPage(randomIndex)
                        }
                    }
                },
                onHideFromFeed = {
                    onHideFromFeed(video.id)
                    Toast.makeText(context, "Hidden from feed", Toast.LENGTH_SHORT).show()
                },
                onToggleLoop = {
                    singleLoopVideoId = if (singleLoopVideoId == video.id) null else video.id
                },
                onVideoInformation = {
                    selectedInfoVideo = video
                },
                onOpenFileLocation = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(video.contentUri, "video/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Open Video"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "No app available to open location", Toast.LENGTH_SHORT).show()
                    }
                },
                onShareVideo = {
                    try {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/*"
                            putExtra(Intent.EXTRA_STREAM, video.contentUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Video"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not share file", Toast.LENGTH_SHORT).show()
                    }
                },
                onDeleteVideo = {
                    handleDeleteVideo(video)
                },
                onSkipToNext = {
                    if (pagerState.currentPage < videos.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )
        }

        // Video Info Dialog
        selectedInfoVideo?.let { video ->
            VideoInfoDialog(video = video, onDismiss = { selectedInfoVideo = null })
        }
    }
}
