package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HeartRed
import kotlin.math.roundToInt

data class HeartEffect(
    val id: Long = System.currentTimeMillis(),
    val offset: Offset
)

@Composable
fun HeartBurstOverlay(
    heart: HeartEffect?,
    onAnimationEnd: () -> Unit
) {
    if (heart == null) return

    val scale = remember { Animatable(0.2f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(heart.id) {
        scale.animateTo(
            targetValue = 1.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350)
        )
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (heart.offset.x - 50).roundToInt(),
                    y = (heart.offset.y - 50).roundToInt()
                )
            }
            .scale(scale.value)
            .alpha(alpha.value)
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Heart Animation",
            tint = HeartRed,
            modifier = Modifier.size(100.dp)
        )
    }
}
