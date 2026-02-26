package com.photovault.ui.compression

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.photovault.databinding.ActivityCompressionBinding
import com.photovault.util.PrefsManager

class CompressionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompressionBinding
    private val prefs by lazy { PrefsManager.get(this) }

    // State
    private var selectedVideoRes = "1080p"
    private var selectedImageRes = "original"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompressionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnSave.setOnClickListener { saveSettings() }

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val quality = prefs.getInt(PrefsManager.KEY_IMG_QUALITY, 85)
        val bitrate = prefs.getInt(PrefsManager.KEY_VIDEO_BITRATE, 8)
        val noCompImg = prefs.getBoolean(PrefsManager.KEY_NO_COMPRESS_IMAGE, false)
        val noCompVid = prefs.getBoolean(PrefsManager.KEY_NO_COMPRESS_VIDEO, false)
        selectedVideoRes = prefs.getString(PrefsManager.KEY_VIDEO_RESOLUTION, "1080p") ?: "1080p"
        selectedImageRes = prefs.getString(PrefsManager.KEY_IMG_RESOLUTION, "original") ?: "original"

        binding.seekQuality.progress = quality
        binding.tvQualityVal.text = "$quality%"
        binding.seekBitrate.progress = bitrate
        binding.tvBitrateVal.text = "$bitrate Mbps"
        binding.switchNoCompressionImage.isChecked = noCompImg
        binding.switchNoCompressionVideo.isChecked = noCompVid

        setImageResButton(selectedImageRes)
        setVideoResButton(selectedVideoRes)
        updateQualitySectionEnabled(!noCompImg)
        updateVideoSectionEnabled(!noCompVid)
        updateSavingsEstimate()
    }

    private fun setupListeners() {
        // No compression toggles
        binding.switchNoCompressionImage.setOnCheckedChangeListener { _, checked ->
            updateQualitySectionEnabled(!checked)
            updateSavingsEstimate()
        }
        binding.switchNoCompressionVideo.setOnCheckedChangeListener { _, checked ->
            updateVideoSectionEnabled(!checked)
            updateSavingsEstimate()
        }

        // Image quality slider
        binding.seekQuality.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                binding.tvQualityVal.text = "$p%"
                if (fromUser) updateSavingsEstimate()
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        // Bitrate slider
        binding.seekBitrate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                binding.tvBitrateVal.text = "$p Mbps"
                if (fromUser) updateSavingsEstimate()
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        // Image resolution buttons
        binding.btnImgResOriginal.setOnClickListener { setImageResButton("original") }
        binding.btnImgRes4k.setOnClickListener { setImageResButton("4k") }
        binding.btnImgResFhd.setOnClickListener { setImageResButton("fhd") }
        binding.btnImgResHd.setOnClickListener { setImageResButton("hd") }

        // Video resolution buttons
        binding.btn480p.setOnClickListener { setVideoResButton("480p") }
        binding.btn720p.setOnClickListener { setVideoResButton("720p") }
        binding.btn1080p.setOnClickListener { setVideoResButton("1080p") }
        binding.btn4k.setOnClickListener { setVideoResButton("4k") }
    }

    private fun setImageResButton(res: String) {
        selectedImageRes = res
        val buttons = mapOf(
            "original" to binding.btnImgResOriginal,
            "4k" to binding.btnImgRes4k,
            "fhd" to binding.btnImgResFhd,
            "hd" to binding.btnImgResHd
        )
        buttons.forEach { (key, btn) ->
            val active = key == res
            btn.setBackgroundResource(
                if (active) com.photovault.R.drawable.bg_gradient_accent
                else com.photovault.R.drawable.bg_pill
            )
            btn.setTextColor(resources.getColor(
                if (active) android.R.color.white
                else com.photovault.R.color.accent_blue, theme
            ))
        }
        updateSavingsEstimate()
    }

    private fun setVideoResButton(res: String) {
        selectedVideoRes = res
        val buttons = mapOf(
            "480p" to binding.btn480p,
            "720p" to binding.btn720p,
            "1080p" to binding.btn1080p,
            "4k" to binding.btn4k
        )
        buttons.forEach { (key, btn) ->
            val active = key == res
            btn.setBackgroundResource(
                if (active) com.photovault.R.drawable.bg_gradient_accent
                else com.photovault.R.drawable.bg_pill
            )
            btn.setTextColor(resources.getColor(
                if (active) android.R.color.white
                else com.photovault.R.color.accent_blue, theme
            ))
        }
        updateSavingsEstimate()
    }

    private fun updateQualitySectionEnabled(enabled: Boolean) {
        binding.qualitySection.alpha = if (enabled) 1f else 0.4f
        binding.seekQuality.isEnabled = enabled
        binding.btnImgResOriginal.isEnabled = enabled
        binding.btnImgRes4k.isEnabled = enabled
        binding.btnImgResFhd.isEnabled = enabled
        binding.btnImgResHd.isEnabled = enabled
    }

    private fun updateVideoSectionEnabled(enabled: Boolean) {
        binding.videoCompressionSection.alpha = if (enabled) 1f else 0.4f
        binding.seekBitrate.isEnabled = enabled
        binding.btn480p.isEnabled = enabled
        binding.btn720p.isEnabled = enabled
        binding.btn1080p.isEnabled = enabled
        binding.btn4k.isEnabled = enabled
    }

    private fun updateSavingsEstimate() {
        val noCompImg = binding.switchNoCompressionImage.isChecked
        val noCompVid = binding.switchNoCompressionVideo.isChecked
        val quality = binding.seekQuality.progress

        val imgReduction = when {
            noCompImg -> 0
            else -> (100 - quality)
        }
        val vidReduction = when {
            noCompVid -> 0
            selectedVideoRes == "480p" -> 70
            selectedVideoRes == "720p" -> 50
            selectedVideoRes == "1080p" -> 30
            else -> 10
        }
        val avgReduction = (imgReduction + vidReduction) / 2
        binding.tvSavingsPct.text = "~$avgReduction%"
        val gbSaved = avgReduction / 100.0 * 8.0 // mock 8GB library
        binding.tvSavingsSize.text = String.format("%.1f GB", gbSaved)
    }

    private fun saveSettings() {
        prefs.edit().apply {
            putInt(PrefsManager.KEY_IMG_QUALITY, binding.seekQuality.progress)
            putInt(PrefsManager.KEY_VIDEO_BITRATE, binding.seekBitrate.progress)
            putBoolean(PrefsManager.KEY_NO_COMPRESS_IMAGE, binding.switchNoCompressionImage.isChecked)
            putBoolean(PrefsManager.KEY_NO_COMPRESS_VIDEO, binding.switchNoCompressionVideo.isChecked)
            putString(PrefsManager.KEY_VIDEO_RESOLUTION, selectedVideoRes)
            putString(PrefsManager.KEY_IMG_RESOLUTION, selectedImageRes)
            apply()
        }
        Toast.makeText(this, "Compression settings saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
