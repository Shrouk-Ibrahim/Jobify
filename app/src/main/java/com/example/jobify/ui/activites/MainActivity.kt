package com.example.jobify.ui.activites

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.jobify.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply the saved locale only if the user has explicitly chosen a language
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

        // Handle Bottom Navigation Item Selection
        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }

                R.id.jobsFragment -> {
                    navController.navigate(R.id.jobsFragment)
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
        }}}