package com.example.ui.reels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocalVideo
import com.example.ui.theme.NeonMagenta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoMenuBottomSheet(
    video: LocalVideo,
    isLooping: Boolean,
    onDismiss: () -> Unit,
    onSurpriseMe: () -> Unit,
    onHideFromFeed: () -> Unit,
    onToggleLoop: () -> Unit,
    onVideoInformation: () -> Unit,
    onOpenFileLocation: () -> Unit,
    onShareVideo: () -> Unit,
    onDeleteVideo: () -> Unit,
    onSkipToNext: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = video.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                maxLines = 1
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

            MenuItem(
                icon = Icons.Default.Casino,
                title = "Surprise Me",
                subtitle = "Jump to a random video",
                tag = "menu_surprise_me",
                onClick = { onDismiss(); onSurpriseMe() }
            )

            MenuItem(
                icon = Icons.Default.VisibilityOff,
                title = "Hide from Feed",
                subtitle = "Remove from main feed without deleting file",
                tag = "menu_hide_feed",
                onClick = { onDismiss(); onHideFromFeed() }
            )

            MenuItem(
                icon = Icons.Default.Loop,
                title = if (isLooping) "Looping Enabled" else "Loop Video",
                subtitle = if (isLooping) "Repeat single video continuously" else "Play video on continuous loop",
                tint = if (isLooping) NeonMagenta else MaterialTheme.colorScheme.onSurface,
                tag = "menu_loop_video",
                onClick = { onDismiss(); onToggleLoop() }
            )

            MenuItem(
                icon = Icons.Default.Info,
                title = "Video Information",
                subtitle = "Resolution, file size, duration & path",
                tag = "menu_video_info",
                onClick = { onDismiss(); onVideoInformation() }
            )

            MenuItem(
                icon = Icons.Default.Folder,
                title = "Open File Location",
                subtitle = "View file in system file manager",
                tag = "menu_open_location",
                onClick = { onDismiss(); onOpenFileLocation() }
            )

            MenuItem(
                icon = Icons.Default.Share,
                title = "Share Video",
                subtitle = "Share file with other apps",
                tag = "menu_share_video",
                onClick = { onDismiss(); onShareVideo() }
            )

            MenuItem(
                icon = Icons.Default.SkipNext,
                title = "Skip to Next Video",
                subtitle = "Scroll directly to next video",
                tag = "menu_skip_next",
                onClick = { onDismiss(); onSkipToNext() }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            MenuItem(
                icon = Icons.Default.Delete,
                title = "Delete Video",
                subtitle = "Request system confirmation to delete file",
                tint = Color(0xFFFF3B30),
                tag = "menu_delete_video",
                onClick = { onDismiss(); onDeleteVideo() }
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
