package com.photovault.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun hasAllPermissions(context: Context): Boolean {
        return getRequiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPermissionLabel(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_MEDIA_IMAGES -> "📷 Read Photos"
            Manifest.permission.READ_MEDIA_VIDEO -> "🎬 Read Videos"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "💾 Storage Access"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "💾 Storage Write"
            Manifest.permission.POST_NOTIFICATIONS -> "🔔 Notifications"
            Manifest.permission.ACCESS_FINE_LOCATION -> "📍 Location (Optional)"
            else -> permission.substringAfterLast(".")
        }
    }

    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_MEDIA_IMAGES -> "Required to access and upload your photos"
            Manifest.permission.READ_MEDIA_VIDEO -> "Required to access and upload your videos"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Required to access your media files"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Required to save compressed media"
            Manifest.permission.POST_NOTIFICATIONS -> "Required to show upload progress"
            else -> "Required for app functionality"
        }
    }
}
