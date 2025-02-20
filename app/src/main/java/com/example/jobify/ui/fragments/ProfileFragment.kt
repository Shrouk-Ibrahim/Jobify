package com.example.jobify.ui.fragments

import Category
import Job
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobify.R
import com.example.jobify.databinding.FragmentProfileBinding
import com.example.jobify.ui.jobrequirements.SavedJobHorizontalAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class ProfileFragment : Fragment(), EditProfileDialogFragment.EditProfileDialogListener {
    private lateinit var savedJobAdapter: SavedJobHorizontalAdapter
    private lateinit var binding: FragmentProfileBinding
    private lateinit var db: FirebaseFirestore

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("ProfileFragment", "User is not logged in")
            throw IllegalStateException("User not logged in")
        }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri: Uri? ->
        if (imageUri != null) {
            uploadProfileImage(imageUri)
        } else {
            Log.e("ProfileFragment", "Selected image URI is null")
            Toast.makeText(requireContext(), "Error: No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PICK_PDF_REQUEST = 1001
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore and adapter
        db = FirebaseFirestore.getInstance()
        savedJobAdapter = SavedJobHorizontalAdapter(emptyList())

        // Set up RecyclerView
        binding.savedJobsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.savedJobsRecyclerView.adapter = savedJobAdapter

        // Fetch user data
        fetchUserData()
        binding.editIcon.setOnClickListener {
            val dialog = EditProfileDialogFragment()
            dialog.setListener(this)
            dialog.show(parentFragmentManager, "EditProfileDialog")
        }

        binding.cameraIcon.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.contactInfoButton.setOnClickListener {
            showContactInfo()
        }

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                binding.profileScrollView.visibility = View.VISIBLE
                binding.frameContainer.visibility = View.GONE
            }
        }

        binding.seeMoreSavedJobs.setOnClickListener {
            binding.profileScrollView.visibility = View.GONE
            binding.frameContainer.visibility = View.VISIBLE

            val savedJobsFragment = SavedJobsFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, savedJobsFragment)
                .addToBackStack("profile_to_saved")
                .commit()
        }
        // Handle back button press to show ScrollView again
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            binding.profileScrollView.visibility = View.VISIBLE
            binding.frameContainer.visibility = View.GONE
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                requireActivity().finish()
            }
        }

        // Handle "See More" button click
        // In ProfileFragment.kt
        binding.seeMoreSavedJobs.setOnClickListener {
            Log.d("ProfileFragment", "See More button clicked")
            try {
                // Hide ScrollView and show FrameContainer
                binding.profileScrollView.visibility = View.GONE
                binding.frameContainer.visibility = View.VISIBLE

                // Replace the frameContainer with SavedJobsFragment
                val savedJobsFragment = SavedJobsFragment()
                childFragmentManager.beginTransaction()
                    .replace(R.id.frameContainer, savedJobsFragment)
                    .addToBackStack("profile_to_saved")
                    .commit()
                Log.d("ProfileFragment", "FragmentTransaction committed successfully")
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error during FragmentTransaction", e)
            }
        }
    }
    private fun fetchUserData() {
        Log.d("ProfileFragment", "Fetching user data for userId: $userId")

        db.collection("users").document(userId).collection("savedJobs")
            .limit(3)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("ProfileFragment", "Error fetching saved jobs", error)
                    return@addSnapshotListener
                }

                val jobs = value?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: throw IllegalStateException("Document data is null")
                        val id = when (val idValue = data["id"]) {
                            is String -> idValue
                            is Long -> idValue.toString() // Convert Long to String
                            else -> throw IllegalStateException("Invalid type for id: ${idValue?.javaClass}")
                        }
                        Job(
                            id = id,
                            name = data["name"] as? String ?: "",
                            category = (data["category"] as? Map<*, *>)?.let {
                                Category(
                                    id = it["id"] as? String ?: "",
                                    name = it["name"] as? String ?: ""
                                )
                            } ?: Category(),
                            active_project_count = data["active_project_count"] as? Int,
                            seo_url = data["seo_url"] as? String ?: "",
                            seo_info = data["seo_info"] as? String ?: "",
                            local = data["local"] as? Boolean ?: false,
                            questions = data["questions"] as? List<String>,
                            timestamp = data["timestamp"] as? com.google.firebase.Timestamp
                        )
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Error parsing document ${doc.id}: ${doc.data}", e)
                        null
                    }
                } ?: emptyList()

                savedJobAdapter.updateJobs(jobs)
            }

        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("ProfileFragment", "Error fetching user data", error)
                    return@addSnapshotListener
                }

                document?.let {
                    Log.d("ProfileFragment", "User data retrieved: ${it.data}")
                    binding.profileName.text = it.getString("name") ?: "No Name"
                    binding.jobTitle.text = it.getString("jobTitle") ?: "No Job Title"
                    binding.facultyText.text = it.getString("faculty") ?: "No Faculty"
                    binding.addressText.text = it.getString("address") ?: "No Address"

                    val profileImageBase64 = it.getString("profileImageBase64")
                    if (!profileImageBase64.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        Glide.with(requireContext())
                            .load(imageBytes)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.profileImage)
                    } else {
                        Glide.with(requireContext())
                            .load(R.drawable.profile)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.profileImage)
                    }
                }
            }
    }
    private fun convertImageToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error converting image to Base64", e)
            null
        }
    }
    private fun uploadProfileImage(imageUri: Uri) {
        val base64Image = convertImageToBase64(imageUri)
        if (base64Image != null) {
            db.collection("users").document(userId)
                .update("profileImageBase64", base64Image)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show()
                    val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                    Glide.with(requireContext())
                        .load(imageBytes)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(binding.profileImage)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error updating Firestore", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileFragment", "Error updating Firestore", exception)
                }
        }
    }

    override fun onProfileUpdated() {
        fetchUserData()
    }


    private fun showContactInfo() {
        val dialog = ContactInfoDialogFragment()
        dialog.setListener(object : ContactInfoDialogFragment.ContactInfoDialogListener {
            override fun onContactInfoUpdated() {
                // Refresh the UI after updating contact info
                fetchUserData()
            }
        })
        dialog.show(parentFragmentManager, "ContactInfoDialog")


}}