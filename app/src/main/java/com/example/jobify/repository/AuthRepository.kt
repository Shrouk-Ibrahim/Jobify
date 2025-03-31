package com.example.jobify.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.jobify.ui.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    // Firebase Authentication instance
    private val firebaseAuth = FirebaseAuth.getInstance()
    // Firestore instance for database operations
    private val db = FirebaseFirestore.getInstance()

    // MutableLiveData to hold the authentication state
    private val _authState = MutableLiveData<Resource<FirebaseUser>>()
    // LiveData exposed to the UI layer to observe authentication state changes
    val authState: LiveData<Resource<FirebaseUser>> get() = _authState

    // Function to handle user login
    fun login(email: String, password: String) {
        // Set the authentication state to Loading
        _authState.value = Resource.Loading()
        // Check for admin credentials first
        if (email == "admin@gmail.com" && password == "admin123Ys") {
            handleAdminLogin(email, password)
            return
        }

        // First, check if the email exists in Firestore
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No user found with the provided email in Firestore
                    _authState.value = Resource.Error("No account found with this email. Please sign up.")
                    Log.w(TAG, "No user found with email: $email")
                } else {
                    // Email exists in Firestore, proceed with Firebase Authentication login
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Login successful, get the current user
                                val user = firebaseAuth.currentUser
                                if (user != null) {
                                    _authState.value = Resource.Success(user)
                                } else {
                                    _authState.value = Resource.Error("User is null")
                                }
                            } else {
                                // Handle login failure
                                val errorMessage = when (task.exception) {
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        "Invalid password. Please try again."
                                    }
                                    is FirebaseAuthInvalidUserException -> {
                                        "No account found with this email. Please sign up."
                                    }
                                    else -> task.exception?.message ?: "Authentication failed."
                                }
                                _authState.value = Resource.Error(errorMessage)
                                Log.w(TAG, "Login failed: ${task.exception}")
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Firestore query failed
                _authState.value = Resource.Error("Failed to retrieve user data. Please try again.")
                Log.w(TAG, "Firestore query failed", exception)
            }
    }
    private fun handleAdminLogin(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        // Ensure admin exists in Firestore with isAdmin flag
                        ensureAdminExists(user, email, password)
                    } else {
                        _authState.value = Resource.Error("Admin user is null")
                    }
                } else {
                    _authState.value = Resource.Error("Admin login failed")
                }
            }
    }

    private fun ensureAdminExists(user: FirebaseUser, email: String, password: String) {
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Create admin user in Firestore
                    val adminUser = hashMapOf(
                        "name" to "Admin",
                        "email" to email,
                        "passwordHash" to hashPassword(password),
                        "userId" to user.uid,
                        "isAdmin" to true
                    )
                    db.collection("users").document(user.uid)
                        .set(adminUser)
                        .addOnSuccessListener {
                            _authState.value = Resource.Success(user)
                        }
                        .addOnFailureListener { e ->
                            _authState.value = Resource.Success(user) // Proceed anyway
                        }
                } else {
                    // Update existing user to ensure isAdmin flag is set
                    db.collection("users").document(user.uid)
                        .update("isAdmin", true)
                        .addOnSuccessListener {
                            _authState.value = Resource.Success(user)
                        }
                        .addOnFailureListener {
                            _authState.value = Resource.Success(user) // Proceed anyway
                        }
                }
            }
            .addOnFailureListener {
                _authState.value = Resource.Success(user) // Proceed anyway
            }
    }
    // In AuthRepository.kt
    fun checkAdminStatus(userId: String, callback: (Boolean) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val isAdmin = document.getBoolean("isAdmin") ?: false
                callback(isAdmin)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
      // Function to handle user signup
    fun signup(name: String, email: String, password: String) {
        // Set the authentication state to Loading
        _authState.value = Resource.Loading()

        // Check if the email is already in use in Firebase Authentication
        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // Email is not in use, proceed with Firebase Authentication signup
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    // Firebase Authentication user created successfully
                                    val firebaseUser = firebaseAuth.currentUser
                                    if (firebaseUser != null) {
                                        // Add user to Firestore
                                        val user = hashMapOf(
                                            "name" to name,
                                            "email" to email,
                                            "passwordHash" to hashPassword(password),
                                            "userId" to firebaseUser.uid
                                        )
                                        db.collection("users")
                                            .document(firebaseUser.uid)
                                            .set(user)
                                            .addOnSuccessListener {
                                                // Firestore update successful
                                                _authState.value = Resource.Success(firebaseUser)
                                            }
                                            .addOnFailureListener { e ->
                                                // Firestore update failed, delete the Firebase user
                                                firebaseUser.delete()
                                                    .addOnCompleteListener {
                                                        _authState.value = Resource.Error("Failed to add user to Firestore. Please try again.")
                                                        Log.w(TAG, "Error adding user to Firestore", e)
                                                    }
                                            }
                                    } else {
                                        // Firebase user is null
                                        _authState.value = Resource.Error("User is null")
                                    }
                                } else {
                                    // Firebase Authentication failed
                                    val errorMessage = when (authTask.exception) {
                                        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                                        else -> authTask.exception?.message ?: "Signup failed"
                                    }
                                    _authState.value = Resource.Error(errorMessage)
                                    Log.w(TAG, "Firebase Auth user creation failed", authTask.exception)
                                }
                            }
                    } else {
                        // Email is already in use in Firebase Authentication
                        _authState.value = Resource.Error("Email already in use")
                        Log.w(TAG, "Email already in use: $email")
                    }
                } else {
                    // Failed to check email availability
                    _authState.value = Resource.Error("Failed to check email availability. Please try again.")
                    Log.w(TAG, "Failed to check email availability", task.exception)
                }
            }
    }

    // Helper function to hash passwords using BCrypt
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    // Helper function to verify passwords using BCrypt
    private fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}