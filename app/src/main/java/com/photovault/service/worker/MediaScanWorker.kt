package com.photovault.service.worker

import android.content.Context
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.photovault.database.AppDatabase
import com.photovault.database.entity.MediaItem

class MediaScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DATE_TAKEN
        )

        applicationContext.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            "${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%'",
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val pathCol   = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val mimeCol   = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val folderCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val sizeCol   = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val wCol      = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val hCol      = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val dateCol   = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
            val idCol     = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)

            while (cursor.moveToNext()) {
                val mime = cursor.getString(mimeCol) ?: continue
                val path = cursor.getString(pathCol)  ?: continue

                // Skip if already scanned
                if (db.mediaItemDao().getByPath(path) != null) continue

                val mediaType = when {
                    mime.startsWith("video/") -> MediaItem.TYPE_VIDEO
                    mime == "image/gif"       -> MediaItem.TYPE_GIF
                    else                     -> MediaItem.TYPE_IMAGE
                }

                val item = MediaItem().apply {
                    mediaStoreId = cursor.getLong(idCol)
                    localPath    = path
                    this.mediaType   = mediaType
                    folderName   = cursor.getString(folderCol) ?: "Unknown"
                    fileSizeBytes = cursor.getLong(sizeCol)
                    width        = cursor.getInt(wCol)
                    height       = cursor.getInt(hCol)
                    createdAt    = cursor.getLong(dateCol)
                    insertedAt   = System.currentTimeMillis()
                }
                db.mediaItemDao().insert(item)
            }
        }
        return Result.success()
    }
}
