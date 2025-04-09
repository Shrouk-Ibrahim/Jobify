package com.example.jobify.ui.activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.jobify.R

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Get NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_auth) as NavHostFragment
        val navController = navHostFragment.navController

    }

    // Handle back button without ActionBar
//    override fun onSupportNavigateUp(): Boolean {
//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment_auth) as? NavHostFragment
//        return navHostFragment?.navController?.navigateUp() ?: false
//    }
}