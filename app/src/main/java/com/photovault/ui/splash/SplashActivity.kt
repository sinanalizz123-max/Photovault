package com.photovault.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.photovault.MainActivity
import com.photovault.R
import com.photovault.ui.permission.PermissionActivity
import com.photovault.util.PermissionHelper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,
                if (PermissionHelper.hasAllPermissions(this)) MainActivity::class.java
                else PermissionActivity::class.java))
            finish()
        }, 1800)
    }
}
