package com.photovault

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.photovault.databinding.ActivityMainBinding
import com.photovault.ui.auth.AddAccountActivity
import com.photovault.ui.settings.SettingsActivity
import com.photovault.ui.upload.UploadPanelActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        setupDrawer()
        binding.btnUpload.setOnClickListener {
            startActivity(Intent(this, UploadPanelActivity::class.java))
        }
    }

    private fun setupNavigation() {
        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                as NavHostFragment).navController
        binding.bottomNav.setupWithNavController(nav)
        nav.addOnDestinationChangedListener { _, dest, _ ->
            binding.tvTitle.text = when (dest.id) {
                R.id.photosFragment -> getString(R.string.tab_photos)
                R.id.albumsFragment -> getString(R.string.tab_albums)
                R.id.facesFragment  -> getString(R.string.tab_faces)
                else -> getString(R.string.app_name)
            }
        }
    }

    private fun setupDrawer() {
        binding.btnDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.START)
        }
        binding.leftDrawer.btnSettings.setOnClickListener {
            binding.drawerLayout.closeDrawers()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.leftDrawer.btnAddAccount.setOnClickListener {
            binding.drawerLayout.closeDrawers()
            startActivity(Intent(this, AddAccountActivity::class.java))
        }
    }

    override fun onBackPressed() {
        // Close drawer on back press if open
        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
