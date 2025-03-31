package com.example.jobify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.jobify.repository.AuthRepository
import com.example.jobify.ui.Resource
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    // Repository instance to handle authentication logic
    private val authRepository = AuthRepository()

    // LiveData to observe authentication state changes
    val authState: LiveData<Resource<FirebaseUser>> get() = authRepository.authState

    // Function to handle user signup
    // AuthViewModel.kt
    fun signup(name: String, email: String, password: String) {
        authRepository.signup(name, email, password)
    }

    // In AuthViewModel.kt
    fun checkAdminStatus(userId: String, callback: (Boolean) -> Unit) {
        authRepository.checkAdminStatus(userId, callback)
    }
    // Function to handle user login
    fun login(email: String, password: String) {
        // Call the login function in the AuthRepository
        authRepository.login(email, password)
    }
}