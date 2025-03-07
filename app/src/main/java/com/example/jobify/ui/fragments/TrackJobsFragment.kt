package com.example.jobify.ui.fragments

import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobify.R
import com.example.jobify.databinding.FragmentTrackJobsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrackJobsFragment : Fragment() {
    private lateinit var binding: FragmentTrackJobsBinding
    private lateinit var db: FirebaseFirestore // Declare db as lateinit
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackJobsBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance() // Initialize db here
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUserProfilePhoto()

        // Set the initial state for the tabs
        setTabSelected(binding.tabSavedJobs, false)
        setTabSelected(binding.tabTrackJobs, true)

        binding.profilePhoto.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        // Set up click listeners for the tabs
        binding.tabSavedJobs.setOnClickListener {
            setTabSelected(binding.tabSavedJobs, true)
            setTabSelected(binding.tabTrackJobs, false)
            findNavController().navigate(R.id.action_trackJobsFragment_to_savedJobsFragment)
        }

        binding.tabTrackJobs.setOnClickListener {
            setTabSelected(binding.tabSavedJobs, false)
            setTabSelected(binding.tabTrackJobs, true)
            // No need to navigate here since we are already in TrackJobsFragment
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

    private fun setTabSelected(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundResource(R.drawable.button_tab_background_selected) // Create this drawable
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Change to your desired selected text color
        } else {
            button.setBackgroundResource(R.drawable.button_tab_background) // Default background
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor)) // Change to your desired default text color
        }
    }
}