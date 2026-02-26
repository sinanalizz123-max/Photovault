package com.photovault

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.photovault.database.AppDatabase
import com.photovault.util.PrefsManager

class PhotoVaultApp : Application() {

    // Lazily created Room database — Java ViewModels access via getDatabase()
    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
        applyThemePreference()
        createNotificationChannels()
    }

    /** Called by Java ViewModels: PhotoVaultApp.getInstance().getDatabase() */
    fun getDatabase(): AppDatabase = appDatabase

    private fun applyThemePreference() {
        val isDark = PrefsManager.get(this).getBoolean(PrefsManager.KEY_DARK_MODE, true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_UPLOAD_ID,
                getString(R.string.channel_upload_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.channel_upload_desc) }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_UPLOAD_ID = "upload_channel"

        private var _instance: PhotoVaultApp? = null

        /** Static accessor for Java code (ViewModels) */
        @JvmStatic
        fun getInstance(): PhotoVaultApp =
            _instance ?: throw IllegalStateException("PhotoVaultApp not initialized")
    }
}
