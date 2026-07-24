package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object Reels : Screen("reels", "Reels", Icons.Default.Movie, Icons.Outlined.Movie)
    object Explore : Screen("explore", "Explore", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings, Icons.Outlined.Settings)
}
