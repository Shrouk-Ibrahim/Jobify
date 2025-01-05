package com.example.jobify

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.jobify.ui.activites.fragments.SignUpFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Use activity layout instead of fragment_login
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.hide()
        // Load the LoginFragment dynamically
        if (savedInstanceState == null) {
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, SignUpFragment()) // Ensure fragment_container exists in activity_main.xml
            fragmentTransaction.commit()
        }
    }
}
