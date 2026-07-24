package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.util.RecommendationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoScanner(
    private val context: Context,
    private val videoDao: VideoDao
) {

    suspend fun scanAndSync(maxDurationMinutes: Int, smartShuffle: Boolean): List<VideoEntity> = withContext(Dispatchers.IO) {
        val maxDurationMs = if (maxDurationMinutes > 0) maxDurationMinutes * 60 * 1000L else Long.MAX_VALUE

        val existingMap = videoDao.getAllVideosOnce().associateBy { it.id }

        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        val scannedEntities = mutableListOf<VideoEntity>()
        val foundIds = mutableSetOf<Long>()

        try {
            val cursor = context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataCol = c.getColumnIndex(MediaStore.Video.Media.DATA)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthCol = c.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = c.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val bucketCol = c.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val duration = c.getLong(durationCol)

                    // Skip videos longer than the max duration setting
                    if (duration > 0 && duration > maxDurationMs) {
                        continue
                    }

                    foundIds.add(id)

                    val name = c.getString(nameCol) ?: "Video_$id"
                    val path = if (dataCol >= 0) c.getString(dataCol) ?: "" else ""
                    val size = c.getLong(sizeCol)
                    val dateAdded = c.getLong(dateCol)
                    val width = if (widthCol >= 0) c.getInt(widthCol) else 0
                    val height = if (heightCol >= 0) c.getInt(heightCol) else 0
                    val bucket = if (bucketCol >= 0) c.getString(bucketCol) ?: "Videos" else "Videos"
                    val contentUri = ContentUris.withAppendedId(collection, id).toString()

                    val existing = existingMap[id]
                    val entity = if (existing != null) {
                        // Preserve user state (likes, dislikes, watch history, last position)
                        val updated = existing.copy(
                            contentUri = contentUri,
                            filePath = path,
                            displayName = name,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            width = width,
                            height = height,
                            bucketName = bucket
                        )
                        val newScore = RecommendationEngine.calculateScore(updated, smartShuffle)
                        updated.copy(recommendationScore = newScore)
                    } else {
                        val newEntity = VideoEntity(
                            id = id,
                            contentUri = contentUri,
                            filePath = path,
                            displayName = name,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            width = width,
                            height = height,
                            bucketName = bucket
                        )
                        val newScore = RecommendationEngine.calculateScore(newEntity, smartShuffle)
                        newEntity.copy(recommendationScore = newScore)
                    }

                    scannedEntities.add(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Save scanned entities into Room
        if (scannedEntities.isNotEmpty()) {
            videoDao.insertVideos(scannedEntities)
            videoDao.updateVideos(scannedEntities)
        }

        // Remove DB records for files no longer on device
        if (foundIds.isNotEmpty()) {
            videoDao.deleteMissingVideos(foundIds.toList())
        }

        scannedEntities
    }
}
