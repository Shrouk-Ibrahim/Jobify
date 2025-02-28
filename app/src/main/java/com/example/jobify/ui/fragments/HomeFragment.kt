package com.example.jobify.ui.fragments

import ApiService
import FreelancerRetrofitClient
import JobAdapter
import JobViewModel
import JobViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jobify.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: JobViewModel
    private lateinit var jobAdapter: JobAdapter
    private lateinit var db: FirebaseFirestore
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        // Initialize ApiService (Retrofit instance)
        val apiService = FreelancerRetrofitClient.instance
        val viewModelFactory = JobViewModelFactory(apiService)
        viewModel = ViewModelProvider(this, viewModelFactory).get(JobViewModel::class.java)

        setupRecyclerView()
        setupObservers()

        // Fetch projects and saved jobs
        viewModel.fetchProjects()
        viewModel.fetchSavedJobs(userId)
    }

    private fun setupRecyclerView() {
        val navController = findNavController()

        jobAdapter = JobAdapter(emptyList(), navController)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = jobAdapter
        }
    }

    private fun setupObservers() {
        viewModel.projects.observe(viewLifecycleOwner) { projects ->
            projects?.let { jobAdapter.updateJobs(it) }
        }

        viewModel.savedJobs.observe(viewLifecycleOwner) { savedJobs ->
            savedJobs?.let { jobAdapter.updateJobs(it) }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                binding.errorMessage.text = error
                binding.errorMessage.visibility = View.VISIBLE
            } else {
                binding.errorMessage.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}