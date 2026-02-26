package com.photovault.ui.permission

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.photovault.MainActivity
import com.photovault.databinding.ActivityPermissionBinding
import com.photovault.util.PermissionHelper

class PermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionBinding

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) goToMain()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnGrant.setOnClickListener {
            launcher.launch(PermissionHelper.getRequiredPermissions())
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
