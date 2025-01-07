package com.example.jobify.ui.activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.jobify.R

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Set up Navigation Component
        val navController = findNavController(R.id.nav_host_fragment_auth)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    // Handle back button in fragments
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_auth).navigateUp() || super.onSupportNavigateUp()
    }
}