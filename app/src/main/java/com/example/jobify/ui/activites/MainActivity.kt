package com.example.jobify.ui.activites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jobify.R
import com.example.jobify.ui.fragments.HomeFragment
import com.example.jobify.ui.fragments.ProfileFragment
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
    }
}