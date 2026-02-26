package com.photovault.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.photovault.databinding.ActivitySettingsBinding
import com.photovault.ui.compression.CompressionActivity
import com.photovault.ui.rules.RulesActivity
import com.photovault.util.PrefsManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val prefs by lazy { PrefsManager.get(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loadPreferences()
        setupListeners()
    }

    private fun loadPreferences() {
        binding.switchDarkMode.isChecked = prefs.getBoolean(PrefsManager.KEY_DARK_MODE, true)
        binding.switchAutoBackup.isChecked = prefs.getBoolean(PrefsManager.KEY_AUTO_BACKUP, false)
        binding.switchRules.isChecked = prefs.getBoolean(PrefsManager.KEY_UPLOAD_BY_RULES, false)
        binding.switchWifiOnly.isChecked = prefs.getBoolean(PrefsManager.KEY_WIFI_ONLY, true)
        updateThemeDesc()
        updateCompressionSummary()
    }

    private fun setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PrefsManager.KEY_DARK_MODE, isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            updateThemeDesc()
        }

        binding.switchAutoBackup.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PrefsManager.KEY_AUTO_BACKUP, isChecked).apply()
        }

        binding.switchRules.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PrefsManager.KEY_UPLOAD_BY_RULES, isChecked).apply()
        }

        binding.switchWifiOnly.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PrefsManager.KEY_WIFI_ONLY, isChecked).apply()
        }

        binding.itemCompression.setOnClickListener {
            startActivity(Intent(this, CompressionActivity::class.java))
        }

        binding.itemRules.setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }

        binding.itemTakeout.setOnClickListener {
            // TODO: File picker for Takeout ZIP
        }

        binding.itemClearCache.setOnClickListener {
            showClearCacheConfirmation()
        }
    }

    private fun updateThemeDesc() {
        val isDark = prefs.getBoolean(PrefsManager.KEY_DARK_MODE, true)
        binding.tvThemeDesc.text = if (isDark) "Currently: Dark" else "Currently: Light"
    }

    private fun updateCompressionSummary() {
        val noCompressImg = prefs.getBoolean(PrefsManager.KEY_NO_COMPRESS_IMAGE, false)
        val noCompressVid = prefs.getBoolean(PrefsManager.KEY_NO_COMPRESS_VIDEO, false)
        val quality = prefs.getInt(PrefsManager.KEY_IMG_QUALITY, 85)
        val videoRes = prefs.getString(PrefsManager.KEY_VIDEO_RESOLUTION, "1080p") ?: "1080p"

        binding.tvCompressionSummary.text = when {
            noCompressImg && noCompressVid -> "No compression · Original quality"
            noCompressImg -> "Images: Original · Videos: $videoRes"
            else -> "Images: $quality% · Videos: $videoRes"
        }
    }

    private fun showClearCacheConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Clear Local Cache?")
            .setMessage("This will remove all thumbnails and locally cached metadata. Upload records will be preserved.")
            .setPositiveButton("Clear") { _, _ ->
                // TODO: Clear Glide cache and Room media_items thumbnails
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateCompressionSummary()
    }
}
