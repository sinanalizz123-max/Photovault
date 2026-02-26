package com.photovault.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.photovault.PhotoVaultApp
import com.photovault.R
import com.photovault.ui.upload.UploadPanelActivity

class UploadForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Uploading photos…", 0))
        // Upload logic delegated to WorkManager workers
        return START_STICKY
    }

    fun updateProgress(uploading: Int, total: Int) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification("Uploading $uploading of $total…", uploading * 100 / total))
    }

    private fun buildNotification(message: String, progress: Int): Notification {
        val intent = PendingIntent.getActivity(
            this, 0,
            Intent(this, UploadPanelActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, PhotoVaultApp.CHANNEL_UPLOAD_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_upload)
            .setContentIntent(intent)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
