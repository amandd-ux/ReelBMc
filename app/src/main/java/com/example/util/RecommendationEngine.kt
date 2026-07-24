package com.example.util

import com.example.data.local.VideoEntity
import kotlin.random.Random

object RecommendationEngine {

    fun calculateScore(video: VideoEntity, smartShuffle: Boolean = true): Float {
        var score = 50.0f

        // 1. Likes (+40) / Favorites (+30) / Dislikes (-100)
        if (video.isLiked) score += 40.0f
        if (video.isFavorite) score += 30.0f
        if (video.isDisliked) score -= 100.0f

        // 2. Watch Completion Ratio
        val totalPlays = video.watchCount.coerceAtLeast(1)
        val completionRatio = video.completionCount.toFloat() / totalPlays.toFloat()
        score += completionRatio * 25.0f

        // 3. Total Watch Time factor (max +20)
        val watchSeconds = video.totalWatchTimeMs / 1000.0f
        val watchTimeFactor = (watchSeconds / 30.0f).coerceAtMost(20.0f)
        score += watchTimeFactor

        // 4. Recency of added file (if added in last 7 days, +15)
        val nowMs = System.currentTimeMillis()
        val dateAddedMs = if (video.dateAdded < 100000000000L) video.dateAdded * 1000 else video.dateAdded
        val daysOld = ((nowMs - dateAddedMs) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        if (daysOld <= 7) {
            score += (15.0f - daysOld * 2.0f).coerceAtLeast(2.0f)
        }

        // 5. Low view count boost for freshness / variety
        if (video.watchCount == 0) {
            score += 20.0f
        } else if (video.watchCount < 3) {
            score += 10.0f
        }

        // 6. Smart Shuffle Randomization (0 to 10 points)
        if (smartShuffle) {
            score += Random.nextFloat() * 10.0f
        }

        return score.coerceAtLeast(0.0f)
    }
}
