package com.example.jobify.ui.fragments

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobify.R
import com.example.jobify.databinding.FragmentTrackJobsBinding
import com.example.jobify.ui.jobrequirements.TrackJobAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TrackJobsFragment : Fragment() {
    private var _binding: FragmentTrackJobsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var trackJobAdapter: TrackJobAdapter
    private var appliedJobsListener: ListenerRegistration? = null

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackJobsBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        fetchUserProfilePhoto()
        fetchAppliedJobs()
    }

    // TrackJobsFragment.kt (partial update)
    private fun setupUI() {
        trackJobAdapter = TrackJobAdapter(emptyList()) { job ->
            // Navigate to JobDetailsFragment with jobId
            val bundle = Bundle().apply {
                putInt("projectId", job.jobId.toInt())
            }
            findNavController().navigate(R.id.action_trackJobsFragment_to_jobDetailsFragment, bundle)
        }



        binding.apply {
            trackJobsRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = trackJobAdapter
                setHasFixedSize(true)
            }

            setTabSelected(tabSavedJobs, false)
            setTabSelected(tabTrackJobs, true)

            tabSavedJobs.setOnClickListener {
                findNavController().navigate(R.id.action_trackJobsFragment_to_savedJobsFragment)
            }

            profilePhoto.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }
        }
    }

    private fun navigateToJobDetails(job: AppliedJob) {
        // Implement navigation to job details with job data
        Toast.makeText(requireContext(), "Opening job details", Toast.LENGTH_SHORT).show()
    }

    private fun fetchAppliedJobs() {
        appliedJobsListener = db.collection("users").document(userId).collection("appliedJobs")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    showError("Failed to load applied jobs")
                    binding.emptyStateTextView.visibility = View.VISIBLE
                    binding.trackJobsRecyclerView.visibility = View.GONE
                    return@addSnapshotListener
                }

                val appliedJobs = value?.documents?.mapNotNull { doc ->
                    doc.toObject(AppliedJob::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                if (appliedJobs.isEmpty()) {
                    binding.emptyStateTextView.visibility = View.VISIBLE
                    binding.trackJobsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateTextView.visibility = View.GONE
                    binding.trackJobsRecyclerView.visibility = View.VISIBLE
                    trackJobAdapter.updateJobs(appliedJobs)
                }
            }
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

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setTabSelected(button: Button, isSelected: Boolean) {
        button.isSelected = isSelected
        button.setBackgroundResource(
            if (isSelected) R.drawable.button_tab_background_selected
            else R.drawable.button_tab_background
        )
        button.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isSelected) R.color.white else R.color.primaryColor
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appliedJobsListener?.remove()
        _binding = null
    }
}

data class AppliedJob(
    val id: String = "",
    val jobId: String = "",
    val jobTitle: String = "",
    val bidAmount: Double = 0.0,
    val deliveryTime: String = "",
    val description: String = "",
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)