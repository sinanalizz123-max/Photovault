package com.photovault.ui.upload

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.photovault.databinding.ActivityUploadPanelBinding

class UploadPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        binding.btnClose.setOnClickListener { finish() }
        binding.rvQueue.layoutManager = LinearLayoutManager(this)
        binding.btnPauseAll.setOnClickListener { togglePauseAll() }

        // TODO: Observe WorkManager upload tasks and update UI
        // TODO: Observe Room upload_targets for queue items
        updateStats(uploading = 3, waiting = 7, done = 234, failed = 2)
    }

    private fun togglePauseAll() {
        // TODO: pause/resume all WorkManager workers
    }

    private fun updateStats(uploading: Int, waiting: Int, done: Int, failed: Int) {
        binding.tvStatUploading.text = uploading.toString()
        binding.tvStatWaiting.text = waiting.toString()
        binding.tvStatDone.text = done.toString()
        binding.tvStatFailed.text = failed.toString()
        binding.tvQueueSubtitle.text = "$uploading uploading · $waiting waiting"
    }
}
