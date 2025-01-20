package com.example.jobify.ui.activites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.jobify.R
import com.example.jobify.ui.fragments.HomeFragment
import com.example.jobify.ui.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.hide()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Firestore
        val db = FirebaseFirestore.getInstance()

        // Get NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment // Correct ID

        // Get NavController
        val navController = navHostFragment.navController

        // Link BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_menu) // Correct ID
        bottomNav.setupWithNavController(navController)
    }
}