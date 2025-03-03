package com.example.jobify.ui.fragments

import JobAdapter
import JobViewModel
import JobViewModelFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobify.R
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

        // Initialize ViewModel
        val apiService = FreelancerRetrofitClient.instance
        val viewModelFactory = JobViewModelFactory(apiService)
        viewModel = ViewModelProvider(this, viewModelFactory).get(JobViewModel::class.java)

        fetchUserProfilePhoto()
        setupRecyclerView()
        setupObservers()
        setupSearchBar()

        binding.profilePhoto.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        binding.filterIcon.setOnClickListener {
            val filterDialog = FilterDialogFragment()
            filterDialog.setListener(object : FilterDialogFragment.FilterDialogListener {
                override fun onFilterApplied(minBudget: Double?, maxBudget: Double?) {
                    applyFilters(minBudget, maxBudget)
                }

                override fun onResetFilters() {
                    resetFilters()
                }
            })
            filterDialog.show(parentFragmentManager, "FilterDialog")
        }

        viewModel.fetchProjects()
        viewModel.fetchSavedJobs(userId)
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(emptyList(), findNavController())
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = jobAdapter
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                filterProjects(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterProjects(query: String) {
        val originalList = viewModel.projects.value ?: emptyList()
        val filteredProjects = if (query.isEmpty()) {
            originalList // Return the original list if the query is empty
        } else {
            originalList.filter { project ->
                // Check if the title or description contains the query (case-insensitive)
                project.title?.contains(query, ignoreCase = true) == true ||
                        project.description?.contains(query, ignoreCase = true) == true
            }
        }
        jobAdapter.updateJobs(filteredProjects)
    }

    private fun applyFilters(minBudget: Double?, maxBudget: Double?) {
        val originalList = viewModel.projects.value ?: emptyList()
        val filteredProjects = originalList.filter { project ->
            // Filter by budget
            when {
                minBudget != null && maxBudget != null -> {
                    // Both min and max are provided
                    (project.budget?.minimum ?: 0.0) >= minBudget && (project.budget?.maximum ?: 0.0) <= maxBudget
                }
                minBudget != null -> {
                    // Only min is provided
                    (project.budget?.minimum ?: 0.0) >= minBudget
                }
                maxBudget != null -> {
                    // Only max is provided
                    (project.budget?.maximum ?: 0.0) <= maxBudget
                }
                else -> true // No budget filter applied
            }
        }
        jobAdapter.updateJobs(filteredProjects)
    }

    private fun resetFilters() {
        val originalList = viewModel.projects.value ?: emptyList()
        jobAdapter.updateJobs(originalList)
    }

    private fun fetchUserProfilePhoto() {
        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (document != null && document.exists()) {
                    val profileImageBase64 = document.getString("profileImageBase64")
                    if (!profileImageBase64.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        Glide.with(requireContext())
                            .load(imageBytes)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.profilePhoto)
                    } else {
                        Glide.with(requireContext())
                            .load(R.drawable.baseline_account_circle_24)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.profilePhoto)
                    }
                }
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