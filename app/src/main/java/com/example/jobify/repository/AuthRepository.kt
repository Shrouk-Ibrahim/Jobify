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

        // Query Firestore to find a user with the provided email
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No user found with the provided email
                    _authState.value = Resource.Error("No account found with this email. Please sign up.")
                    Log.w(TAG, "No user found with email: $email")
                } else {
                    for (document in documents) {
                        // Retrieve the hashed password from Firestore
                        val savedPasswordHash = document.getString("passwordHash")
                        if (savedPasswordHash != null && verifyPassword(password, savedPasswordHash)) {
                            // Password matches, proceed with Firebase Authentication login
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
                                            else -> task.exception?.message ?: "Authentication failed."
                                        }
                                        _authState.value = Resource.Error(errorMessage)
                                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                                    }
                                }
                        } else {
                            // Password does not match
                            _authState.value = Resource.Error("Invalid password. Please try again.")
                            Log.w(TAG, "Password mismatch for email: $email")
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

    // Function to handle user signup
    fun signup(name: String, email: String, password: String) {
        // Set the authentication state to Loading
        _authState.value = Resource.Loading()


        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // Email is not in use in Firebase Authentication
                        // Proceed to create Firebase Authentication user
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
                                                // Firestore update failed
                                                _authState.value = Resource.Error("Failed to add user to Firestore. Please try again.")
                                                Log.w(TAG, "Error adding user to Firestore", e)
                                            }
                                    } else {
                                        // Firebase user is null
                                        _authState.value = Resource.Error("User is null")
                                    }
                                } else {
                                    // Firebase Authentication failed
                                    val errorMessage = when (authTask.exception) {
                                        is FirebaseAuthUserCollisionException -> "Email already in use"
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
}