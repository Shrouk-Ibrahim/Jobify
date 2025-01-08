package com.example.jobify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.jobify.repository.AuthRepository
import com.example.jobify.ui.Resource
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // LiveData to observe authentication state
    val authState: LiveData<Resource<FirebaseUser>> get() = authRepository.authState

    // Signup function
    fun signup(name: String, email: String, password: String) {
        authRepository.signup(name, email, password)
    }

    // Login function
    fun login(email: String, password: String) {
        authRepository.login(email, password)
    }
}