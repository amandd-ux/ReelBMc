package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object FormatUtils {

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0:00"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        return if (hours > 0) {
            val mins = minutes % 60
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, mins, seconds)
        } else {
            String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }

    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.getDefault(), "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.getDefault(), "%.0f KB", kb)
            else -> "$sizeBytes B"
        }
    }

    fun formatDate(timestampSecOrMs: Long): String {
        val ms = if (timestampSecOrMs < 100000000000L) timestampSecOrMs * 1000 else timestampSecOrMs
        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        return sdf.format(Date(ms))
    }
}
