package com.example.jobify.ui.fragments

import Category
import Job
import JobAdapter
import JobViewModel
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jobify.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobify.R

import com.google.firebase.auth.FirebaseAuth
import android.util.Base64
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: JobViewModel
    private lateinit var jobAdapter: JobAdapter
    private val categories = mutableListOf<Category>()
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

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupSearch()
        setupFilter()
        viewModel.fetchJobs()

        // Fetch user data to load profile photo
        fetchUserProfilePhoto()

        // Set up the click listener for the profile photo
        binding.profilePhoto.setOnClickListener {
            // Navigate to ProfileFragment
            findNavController().navigate(R.id.profileFragment)
        }
    }

    private fun fetchUserProfilePhoto() {
        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (document != null && document.exists()) {
                    // Load profile image from Base64 string
                    val profileImageBase64 = document.getString("profileImageBase64")
                    if (!profileImageBase64.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        Glide.with(requireContext())
                            .load(imageBytes)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.profilePhoto)
                    } else {
                        // Set a default image if no Base64 string is found
                        Glide.with(requireContext())
                            .load(R.drawable.baseline_account_circle_24) // Default profile icon
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.profilePhoto)
                    }
                }
            }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(JobViewModel::class.java)
    }

    private fun setupRecyclerView() {
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnCount = (screenWidthDp / 180).toInt() // Adjust 180dp to your preferred item width
        jobAdapter = JobAdapter(emptyList())
        binding.recyclerView.apply {
            // Use GridLayoutManager with 2 columns
            layoutManager = GridLayoutManager(requireContext(), columnCount)
            adapter = jobAdapter
        }
    }

    private fun setupObservers() {
        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            jobs?.let {
                jobAdapter.updateJobs(jobs)
                updateCategories(jobs)
            }
        }

        viewModel.savedJobs.observe(viewLifecycleOwner) { savedJobs ->
            savedJobs?.let {
                jobAdapter.updateJobs(savedJobs) // Use savedJobs instead of jobs
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            private val handler = Handler(Looper.getMainLooper())
            private var runnable: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                runnable?.let { handler.removeCallbacks(it) }
                runnable = Runnable {
                    viewModel.fetchJobs(query = s?.toString())
                }
                handler.postDelayed(runnable!!, 500)
            }
        })
    }

    private fun setupFilter() {
        binding.filterIcon.setOnClickListener {
            if (categories.isNotEmpty()) {
                showCategoryFilterDialog()
            } else {
                Snackbar.make(binding.root, "Loading categories...", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCategoryFilterDialog() {
        val categoryNames = categories.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(categoryNames.size)

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Category")
            .setMultiChoiceItems(categoryNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }

                .setPositiveButton("Apply") { _, _ ->
                val selectedCategories = categories.filterIndexed { index, _ ->
                    checkedItems[index]
                }.map { it.id } // Already returns String IDs
                viewModel.fetchJobs(categories = selectedCategories)
            }
            .setNegativeButton("Clear") { _, _ ->
                viewModel.fetchJobs(categories = emptyList())
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun updateCategories(jobs: List<Job>) {
        categories.clear()
        categories.addAll(jobs.map { it.category }.distinctBy { it.id })
    }


}
