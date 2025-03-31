package com.example.jobify.ui.fragments

import JobViewModel
import JobViewModelFactory
import Project
import android.content.res.ColorStateList
import com.example.jobify.ui.dialogs.ApplyJobDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jobify.R
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
setupViews()
        // Fetch and display project details
        if (projectId != -1) {
            fetchProjectDetailsFromFirestore()
            viewModel.fetchProjectDetails(projectId)
            observeProjectDetails()
            checkIfProjectIsSaved()
            checkApplicationStatus()
        } else {
            binding.errorMessage.text = "Invalid project ID"
            binding.errorMessage.visibility = View.VISIBLE
        }

        // Set up save icon click listener
        binding.saveIcon.setOnClickListener {
            toggleSaveProject()
        }
    }

    private fun checkIfProjectIsSaved() {
        db.collection("users").document(userId).collection("savedJobs")
            .document(projectId.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Project is saved, set icon to savefilled
                    binding.saveIcon.setImageResource(R.drawable.savefilled)
                } else {
                    // Project is not saved, set icon to save
                    binding.saveIcon.setImageResource(R.drawable.save)
                }
            }
            .addOnFailureListener { e ->
                Log.e("JobDetailsFragment", "Error checking if project is saved", e)
            }
    }

    private fun toggleSaveProject() {
        db.collection("users").document(userId).collection("savedJobs")
            .document(projectId.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Project is saved, unsave it
                    db.collection("users").document(userId).collection("savedJobs")
                        .document(projectId.toString())
                        .delete()
                        .addOnSuccessListener {
                            binding.saveIcon.setImageResource(R.drawable.save)
                        }
                        .addOnFailureListener { e ->
                            Log.e("JobDetailsFragment", "Error unsaving project", e)
                        }
                } else {
                    // Project is not saved, save it
                    viewModel.projectDetails.value?.let { project ->
                        db.collection("users").document(userId).collection("savedJobs")
                            .document(projectId.toString())
                            .set(project)
                            .addOnSuccessListener {
                                binding.saveIcon.setImageResource(R.drawable.savefilled)
                            }
                            .addOnFailureListener { e ->
                                Log.e("JobDetailsFragment", "Error saving project", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("JobDetailsFragment", "Error checking if project is saved", e)
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
    // JobDetailsFragment.kt (partial update)
// JobDetailsFragment.kt (partial update)
    private fun setupViews() {
        binding.applyButton.setOnClickListener {
            showApplyDialog()
        }
    }

    private fun showApplyDialog() {
        val dialog = ApplyJobDialog().apply {
            jobId = projectId.toString()
            jobTitle = viewModel.projectDetails.value?.title ?: ""
            setListener(object : ApplyJobDialog.ApplyJobListener {
                override fun onJobApplied() {
                    checkApplicationStatus()
                }
            })
        }
        dialog.show(parentFragmentManager, "ApplyJobDialog")
    }

    private fun checkApplicationStatus() {
        db.collection("users").document(userId).collection("appliedJobs")
            .whereEqualTo("jobId", projectId.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val status = documents.documents.firstOrNull()?.getString("status") ?: "pending"
                    updateApplyButton(status)
                }
            }
    }

    private fun updateApplyButton(status: String) {
        binding.applyButton.apply {
            isEnabled = false
            text = status.replaceFirstChar { it.uppercase() }
            when (status.lowercase()) {
                "accepted" -> {
                    backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
                }
                "rejected" -> {
                    backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
                }
                "pending" -> {
                    backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange))
                }
                else -> {
                    backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
                }
            }
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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