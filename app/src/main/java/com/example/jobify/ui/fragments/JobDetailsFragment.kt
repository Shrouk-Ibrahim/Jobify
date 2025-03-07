package com.example.jobify.ui.fragments

import JobViewModel
import JobViewModelFactory
import Project
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jobify.databinding.FragmentJobDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobDetailsFragment : Fragment() {

    private lateinit var binding: FragmentJobDetailsBinding
    private lateinit var viewModel: JobViewModel
    private lateinit var db: FirebaseFirestore
    private var projectId: Int = -1
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJobDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Retrieve projectId from arguments
        projectId = arguments?.getInt("projectId") ?: -1

        // Initialize ViewModel
        val apiService = FreelancerRetrofitClient.instance
        val viewModelFactory = JobViewModelFactory(apiService)
        viewModel = ViewModelProvider(this, viewModelFactory).get(JobViewModel::class.java)

        // Fetch and display project details
        if (projectId != -1) {
            fetchProjectDetailsFromFirestore()
            viewModel.fetchProjectDetails(projectId)
            observeProjectDetails()
        } else {
            binding.errorMessage.text = "Invalid project ID"
            binding.errorMessage.visibility = View.VISIBLE
        }
    }

    private fun fetchProjectDetailsFromFirestore() {
        if (projectId == -1) {
            Log.e("JobDetailsFragment", "Invalid project ID")
            return
        }

        Log.d("JobDetailsFragment", "Fetching project details for projectId: $projectId")

        // Fetch project details from the savedJobs subcollection
        db.collection("users")
            .document(userId)
            .collection("savedJobs")
            .document(projectId.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("JobDetailsFragment", "Project found in Firestore")
                    val project = document.toObject(Project::class.java)
                    project?.let {
                        displayProjectDetails(it)
                    }
                } else {
                    Log.d("JobDetailsFragment", "Project not found in Firestore, fetching from API")
                    // If the project is not found in Firestore, fetch it from the API
                    viewModel.fetchProjectDetails(projectId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("JobDetailsFragment", "Error fetching project details", e)
                // If there's an error, fetch the project from the API
                viewModel.fetchProjectDetails(projectId)
            }
    }
    private fun observeProjectDetails() {
        viewModel.projectDetails.observe(viewLifecycleOwner) { project ->
            Log.d("JobDetailsFragment", "Project details observed: $project")
            project?.let {
                displayProjectDetails(it)
            }
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
    private fun displayProjectDetails(project: Project) {
        Log.d("JobDetailsFragment", "Displaying project details: $project")
        binding.projectTitle.text = project.title ?: "No Title"
        binding.previewDescription.text = project.previewDescription ?: "No description available"

        if (project.submitdate > 0) {
            val date = Date(project.submitdate * 1000) // Convert seconds to milliseconds
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.submitDate.text = dateFormat.format(date)
        } else {
            binding.submitDate.text = "Unknown Date"
            Log.e("JobDetailsFragment", "Invalid submit date: ${project.submitdate}")
        }
        binding.projectStatus.text = "Status: ${project.status ?: "Unknown"}"
        binding.projectBudget.text = "Budget: ${project.budget?.minimum ?: 0} - ${project.budget?.maximum ?: 0} ${project.currency?.code ?: ""}"
        binding.projectBidStats.text = "Bids count: ${project.bid_stats?.bidCount ?: 0} "
    }


}