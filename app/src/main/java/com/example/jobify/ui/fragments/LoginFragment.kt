package com.example.jobify.ui.fragments

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jobify.R
import com.example.jobify.databinding.FragmentLoginBinding
import com.example.jobify.viewmodel.AuthViewModel
import com.example.jobify.ui.Resource
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {

    // ViewBinding for the fragment
    private lateinit var binding: FragmentLoginBinding

    // ViewModel for handling authentication logic
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using ViewBinding
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Set up password visibility toggle for the start icon
        binding.passwordInputLayout.setStartIconOnClickListener {
            togglePasswordVisibility(binding.passwordLoginField, binding.passwordInputLayout)
        }

        // Set up login button click listener
        binding.loginButton.setOnClickListener {
            // Get user input from email and password fields
            val email = binding.emailLoginField.text.toString().trim()
            val password = binding.passwordLoginField.text.toString().trim()

            // Clear previous errors
            binding.emailLoginField.error = null
            binding.passwordLoginField.error = null

            // Test Case 1: Check if email field is empty
            if (email.isEmpty()) {
                binding.emailLoginField.error = "Email is required"
                return@setOnClickListener
            }

            // Test Case 2: Check if password field is empty
            if (password.isEmpty()) {
                binding.passwordLoginField.error = "Password is required"
                return@setOnClickListener
            }

            // Test Case 3: Validate email format
            if (!isValidEmail(email)) {
                binding.emailLoginField.error = "Invalid email format"
                return@setOnClickListener
            }

            // Test Case 4: Validate password format
            if (!isValidPassword(password)) {
                binding.passwordLoginField.error = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, and one number"
                return@setOnClickListener
            }

            // Call ViewModel to perform login with the provided email and password
            viewModel.login(email, password)
        }

        // Set up sign-up link click listener
        binding.signUpLink.setOnClickListener {
            // Navigate to the SignupFragment using the navigation graph
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        // Observe authentication state changes from the ViewModel
        viewModel.authState.observe(viewLifecycleOwner, { state ->
            when (state) {
                is Resource.Loading -> {
                    // Show loading indicator
                    Toast.makeText(requireContext(), "Logging in...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    // Login successful
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                }
                is Resource.Error -> {
                    // Login failed
                    when (state.message) {
                        "No account found with this email. Please sign up." -> {
                            binding.emailLoginField.error = state.message
                        }
                        "Invalid password. Please try again." -> {
                            binding.passwordLoginField.error = state.message
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Login failed: ${state.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    // Function to toggle password visibility
    private fun togglePasswordVisibility(editText: TextInputEditText, textInputLayout: TextInputLayout) {
        val isPasswordVisible = editText.transformationMethod == null
        if (isPasswordVisible) {
            // Hide password
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            textInputLayout.startIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility_off)
        } else {
            // Show password
            editText.transformationMethod = null
            textInputLayout.startIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility)
        }
        // Move cursor to the end of the text
        editText.setSelection(editText.text?.length ?: 0)
    }

    // Function to validate email format
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to validate password format
    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"
        return password.matches(passwordRegex.toRegex())
    }
}