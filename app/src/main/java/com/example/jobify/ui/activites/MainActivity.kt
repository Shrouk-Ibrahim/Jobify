package com.example.jobify.ui.activites

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.jobify.R
import com.example.jobify.ui.fragments.JobDetailsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide the action bar
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.hide()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_menu)
        bottomNav.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
        // In your MainActivity's onCreate()
        // Update the notification handling in MainActivity.kt
        if (intent?.getBooleanExtra("from_notification", false) == true) {
            val jobId = intent.getStringExtra("job_id")
            jobId?.let {
                // Navigate to job details using NavController
                val bundle = Bundle().apply {
                    putString("job_id", it)
                }
                navController.navigate(R.id.jobDetailsFragment, bundle)
            }
        }
        // Handle Bottom Navigation Item Selection
        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.savedJobsFragment -> {
                    navController.navigate(R.id.savedJobsFragment)
                    true
                }
                R.id.notificationsFragment -> {
                    navController.navigate(R.id.notificationsFragment)
                    true
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }
    }


}