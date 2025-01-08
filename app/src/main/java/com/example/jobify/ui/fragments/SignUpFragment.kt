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
import com.example.jobify.databinding.FragmentSignUpBinding
import com.example.jobify.viewmodel.AuthViewModel
import com.example.jobify.ui.Resource
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

// Set up password visibility toggle for the start icon
        binding.passwordInputLayout.setStartIconOnClickListener {
            togglePasswordVisibility(binding.passwordSignupField, binding.passwordInputLayout)
        }

        // Set up confirm password visibility toggle for the start icon
        binding.confirmPasswordInputLayout.setStartIconOnClickListener {
            togglePasswordVisibility(binding.confirmPasswordSignupField, binding.confirmPasswordInputLayout)
        }
        // Set up signup button click listener
        binding.signupButton.setOnClickListener {
            val name = binding.nameSignupField.text.toString().trim()
            val email = binding.emailSignupField.text.toString().trim()
            val password = binding.passwordSignupField.text.toString().trim()
            val confirmPassword = binding.confirmPasswordSignupField.text.toString().trim()

            // Clear previous errors
            binding.nameInputLayout.error = null
            binding.emailInputLayout.error = null
            binding.passwordInputLayout.error = null
            binding.confirmPasswordInputLayout.error = null

            // Test Case 1: Empty fields
            if (name.isEmpty()) {
                binding.nameSignupField.error = "Name is required"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.emailSignupField.error = "Email is required"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordSignupField.error = "Password is required"
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                binding.confirmPasswordSignupField.error = "Confirm Password is required"
                return@setOnClickListener
            }

            // Test Case 2: Invalid email format
            if (!isValidEmail(email)) {
                binding.emailSignupField.error = "Invalid email format"
                return@setOnClickListener
            }

            // Test Case 3: Passwords don't match
            if (password != confirmPassword) {
                binding.confirmPasswordSignupField.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Test Case 4: Invalid password format
            if (!isValidPassword(password)) {
                binding.passwordSignupField.error = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, and one number"
                return@setOnClickListener
            }

            // Call ViewModel to perform signup
            viewModel.signup(name, email, password)
        }

        // Set up sign-in link click listener
        binding.signUpLink.setOnClickListener {
            // Navigate to LoginFragment
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        // Observe authentication state
        viewModel.authState.observe(viewLifecycleOwner, { state ->
            when (state) {
                is Resource.Loading -> {
                    // Test Case 5: Show loading indicator
                    Toast.makeText(requireContext(), "Signing up...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    // Test Case 6: Successful signup
                    Toast.makeText(requireContext(), "Signup successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signupFragment_to_homeFragment)
                }
                is Resource.Error -> {
                    // Test Case 7: Signup failed (Firebase error)
                    if (state.message == "Email already in use") {
                        binding.emailSignupField.error = state.message
                    } else {
                        Toast.makeText(requireContext(), "Signup failed: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
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