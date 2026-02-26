package com.photovault.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PrefsManager {

    private const val PREFS_NAME = "photovault_prefs"
    private const val SECURE_PREFS_NAME = "photovault_secure"

    fun get(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSecure(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Keys
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_AUTO_BACKUP = "auto_backup"
    const val KEY_UPLOAD_BY_RULES = "upload_by_rules"
    const val KEY_WIFI_ONLY = "wifi_only"
    const val KEY_IMG_QUALITY = "image_quality"
    const val KEY_IMG_RESOLUTION = "image_resolution"
    const val KEY_VIDEO_RESOLUTION = "video_resolution"
    const val KEY_VIDEO_BITRATE = "video_bitrate"
    const val KEY_NO_COMPRESS_IMAGE = "no_compress_image"
    const val KEY_NO_COMPRESS_VIDEO = "no_compress_video"
    const val KEY_CONFLICT_BEHAVIOR = "conflict_behavior"
}
