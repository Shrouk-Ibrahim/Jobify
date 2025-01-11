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
    fun signup(name: String, email: String, password: String) {
        // Call the signup function in the AuthRepository
        authRepository.signup(name, email, password)
    }

    // Function to handle user login
    fun login(email: String, password: String) {
        // Call the login function in the AuthRepository
        authRepository.login(email, password)
    }
}