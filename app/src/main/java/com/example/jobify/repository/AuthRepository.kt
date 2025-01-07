package com.example.jobify.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.jobify.ui.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    // LiveData to observe authentication state
    private val _authState = MutableLiveData<Resource<FirebaseUser>>()
    val authState: LiveData<Resource<FirebaseUser>> get() = _authState

    // Login function
    fun login(email: String, password: String) {
        _authState.value = Resource.Loading()
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
                    _authState.value = Resource.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    // Signup function
    fun signup(name: String, email: String, password: String) {
        _authState.value = Resource.Loading()
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
                    _authState.value = Resource.Error(task.exception?.message ?: "Signup failed")
                }
            }
    }
}