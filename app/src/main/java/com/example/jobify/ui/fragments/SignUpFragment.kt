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
import com.example.jobify.databinding.FragmentSignUpBinding
import com.example.jobify.viewmodel.AuthViewModel

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using data binding
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Set up signup button click listener
        binding.signupButton.setOnClickListener {
            val name = binding.nameSignupField.text.toString().trim()
            val email = binding.emailSignupField.text.toString().trim()
            val password = binding.passwordSignupField.text.toString().trim()
            val confirmPassword = binding.confirmPasswordSignupField.text.toString().trim()

            // Validate inputs
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
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
                    // Show loading indicator (e.g., ProgressBar)
                    Toast.makeText(requireContext(), "Signing up...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    // Navigate to the next screen (e.g., HomeActivity)
                    Toast.makeText(requireContext(), "Signup successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signupFragment_to_homeFragment) // Replace with your destination
                }
                is Resource.Error -> {
                    // Show error message
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}