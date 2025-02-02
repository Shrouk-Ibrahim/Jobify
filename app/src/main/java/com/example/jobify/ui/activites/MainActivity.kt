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
        applySavedLocaleIfExists()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide the action bar
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.hide()

        FirebaseApp.initializeApp(this)

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
                R.id.languageSwitch -> {
                    showLanguageSelectionDialog() // Show a dialog to select language
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Applies the saved locale only if the user has explicitly chosen a language.
     * Otherwise, the system's default language is used.
     */
    private fun applySavedLocaleIfExists() {
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val language = sharedPrefs.getString("Language", null)

        // Only apply the saved locale if the user has explicitly chosen a language
        if (language != null) {
            val locale = Locale(language)
            setAppLocale(locale)
        }
    }

    /**
     * Shows a dialog to allow the user to select a language.
     */
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "العربية")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Language")
        builder.setItems(languages) { _, which ->
            when (which) {
                0 -> setLanguage("en") // English
                1 -> setLanguage("ar") // Arabic
            }
        }
        builder.show()
    }

    /**
     * Sets the app's language and restarts the app.
     */
    private fun setLanguage(languageCode: String) {
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("Language", languageCode).apply()

        // Restart the app to apply the new language
        restartApp()
    }

    /**
     * Sets the app's locale and updates the configuration.
     */
    private fun setAppLocale(locale: Locale) {
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Update the context for the entire app
        val context = applicationContext
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Restarts the app to apply the new locale.
     */
    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}