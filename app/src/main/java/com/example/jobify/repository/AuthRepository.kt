package com.example.jobify.repository

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.jobify.ui.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LiveData to observe authentication state
    private val _authState = MutableLiveData<Resource<FirebaseUser>>()
    val authState: LiveData<Resource<FirebaseUser>> get() = _authState

    // Login function
    fun login(email: String, password: String) {
        _authState.value = Resource.Loading()

        // Query Firestore for the user with the provided email
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No user found with this email
                    _authState.value = Resource.Error("No account found with this email. Please sign up.")
                    Log.w(TAG, "No user found with email: $email")
                } else {
                    for (document in documents) {
                        val savedPasswordHash = document.getString("passwordHash")
                        if (savedPasswordHash != null && verifyPassword(password, savedPasswordHash)) {
                            // Password matches, proceed with Firebase Auth login
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = firebaseAuth.currentUser
                                        if (user != null) {
                                            _authState.value = Resource.Success(user)
                                        } else {
                                            _authState.value = Resource.Error("User is null")
                                        }
                                    } else {
                                        val errorMessage = when (task.exception) {
                                            is FirebaseAuthInvalidCredentialsException -> {
                                                "Invalid  password. Please try again."
                                            }
                                            else -> task.exception?.message ?: "Authentication failed."
                                        }
                                        _authState.value = Resource.Error(errorMessage)
                                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                                    }
                                }
                        } else {
                            // Password does not match
                            _authState.value = Resource.Error("Invalid  password. Please try again.")
                            Log.w(TAG, "Password mismatch for email: $email")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = Resource.Error("Failed to retrieve user data. Please try again.")
                Log.w(TAG, "Firestore query failed", exception)
            }
    }

    // Signup function
    fun signup(name: String, email: String, password: String) {
        _authState.value = Resource.Loading()

        // Hash the password
        val passwordHash = hashPassword(password)

        // Create a user object
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "passwordHash" to passwordHash
        )

        // Add the user to Firestore
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "User added with ID: ${documentReference.id}")

                // Create a Firebase Auth user
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            if (user != null) {
                                _authState.value = Resource.Success(user)
                            } else {
                                _authState.value = Resource.Error("User is null")
                            }
                        } else {
                            val errorMessage = when (task.exception) {
                                is FirebaseAuthUserCollisionException -> "Email already in use"
                                else -> task.exception?.message ?: "Signup failed"
                            }
                            _authState.value = Resource.Error(errorMessage)
                            Log.w(TAG, "Firebase Auth user creation failed", task.exception)
                        }
                    }
            }
            .addOnFailureListener { e ->
                _authState.value = Resource.Error("Failed to add user to Firestore. Please try again.")
                Log.w(TAG, "Error adding user to Firestore", e)
            }
    }

    // Helper function to hash passwords
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    // Helper function to verify passwords
    private fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }
}