package com.photovault.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.photovault.service.worker.UploadWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val request = OneTimeWorkRequestBuilder<UploadWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "resume_uploads",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
