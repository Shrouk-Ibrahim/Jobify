package com.example.jobify.ui.fragments

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jobify.R
import com.example.jobify.databinding.FragmentLoginBinding
import com.example.jobify.viewmodel.AuthViewModel
import com.example.jobify.ui.Resource
import com.example.jobify.utils.NotificationHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        binding.passwordInputLayout.setStartIconOnClickListener {
            togglePasswordVisibility(binding.passwordLoginField, binding.passwordInputLayout)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailLoginField.text.toString().trim()
            val password = binding.passwordLoginField.text.toString().trim()

            binding.emailLoginField.error = null
            binding.passwordLoginField.error = null

            if (email.isEmpty()) {
                binding.emailLoginField.error = "Email is required"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordLoginField.error = "Password is required"
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                binding.emailLoginField.error = "Invalid email format"
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                binding.passwordLoginField.error = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, and one number"
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        binding.signUpLink.setOnClickListener {
            safeNavigate(R.id.action_loginFragment_to_signupFragment)
        }

        viewModel.authState.observe(viewLifecycleOwner, { state ->
            when (state) {
                is Resource.Loading -> {
                    showLoading()
                }
                is Resource.Success -> {
                    state.data?.let { user ->
                        viewModel.checkAdminStatus(user.uid) { isAdmin ->
                            if (isAdmin) {
                                navigateToAdminDashboard()
                            } else {
                                navigateToMainActivity()
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    handleLoginError(state.message)
                }
            }
        })
    }

    private fun showLoading() {
        // Loading state handling without Toast
    }

    private fun navigateToAdminDashboard() {
        safeNavigate(R.id.action_loginFragment_to_adminDashboardFragment)
    }

    private fun navigateToMainActivity() {
        safeNavigate(R.id.action_loginFragment_to_mainActivity)
    }

    private fun handleLoginError(message: String?) {
        when (message) {
            "No account found with this email. Please sign up." -> {
                binding.emailLoginField.error = message
            }
            "Invalid password. Please try again." -> {
                binding.passwordLoginField.error = message
            }
            else -> {
                // Error handling without Toast
            }
        }
    }

    private fun safeNavigate(actionId: Int) {
        try {
            if (findNavController().currentDestination?.id == R.id.loginFragment) {
                findNavController().navigate(actionId)
            }
        } catch (e: Exception) {
            // Navigation error handling without Toast
        }
    }

    private fun togglePasswordVisibility(editText: TextInputEditText, textInputLayout: TextInputLayout) {
        val isPasswordVisible = editText.transformationMethod == null
        if (isPasswordVisible) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            textInputLayout.startIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility_off)
        } else {
            editText.transformationMethod = null
            textInputLayout.startIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility)
        }
        editText.setSelection(editText.text?.length ?: 0)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"
        return password.matches(passwordRegex.toRegex())
    }
}