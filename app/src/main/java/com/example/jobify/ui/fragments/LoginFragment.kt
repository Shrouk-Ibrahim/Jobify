package com.example.jobify.ui.fragments
import com.example.jobify.ui.Resource
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jobify.R
import com.example.jobify.databinding.FragmentLoginBinding
import com.example.jobify.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Set up login button click listener
        binding.loginButton.setOnClickListener {
            val email = binding.emailLoginField.text.toString().trim()
            val password = binding.passwordLoginField.text.toString().trim()

            // Validate inputs
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call ViewModel to perform login
            viewModel.login(email, password)
        }

        // Set up sign-up link click listener
        binding.signUpLink.setOnClickListener {
            // Navigate to SignupFragment
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        // Observe authentication state
        viewModel.authState.observe(viewLifecycleOwner, { state ->
            when (state) {
                is Resource.Loading -> {
                    // Show loading indicator (e.g., ProgressBar)
                    Toast.makeText(requireContext(), "Logging in...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    // Navigate to HomeFragment
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is Resource.Error -> {
                    // Show error message
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}