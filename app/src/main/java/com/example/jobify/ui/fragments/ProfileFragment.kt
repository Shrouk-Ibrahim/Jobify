package com.example.jobify.ui.fragments

import BidStats
import Budget
import Country
import Currency
import Location
import Project
import Timezone
import Upgrades
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
import com.google.firebase.firestore.Query
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobify.R
import com.example.jobify.databinding.FragmentProfileBinding
import com.example.jobify.ui.jobrequirements.SavedJobHorizontalAdapter
import com.example.jobify.ui.jobrequirements.TrackJobAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment(), EditProfileDialogFragment.EditProfileDialogListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var savedJobAdapter: SavedJobHorizontalAdapter

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        // Initialize Firestore and adapter
        db = FirebaseFirestore.getInstance()
        savedJobAdapter = SavedJobHorizontalAdapter(emptyList(), navController)

        // Set up RecyclerView
        binding.savedJobsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.savedJobsRecyclerView.adapter = savedJobAdapter
fetchTrackedJobs()
        // Fetch user data
        fetchUserData()

        // Set up click listeners
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
            // Navigate to SavedJobsFragment using the main NavController
            navController.navigate(R.id.savedJobsFragment)
        }

        binding.seeMoreTrackJobs.setOnClickListener {
            // Navigate to TrackJobsFragment using the main NavController
            navController.navigate(R.id.trackJobsFragment)
        }
    }
    // ProfileFragment.kt (partial update)
    private fun fetchTrackedJobs() {
        db.collection("users").document(userId).collection("appliedJobs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                val appliedJobs = documents.documents.mapNotNull { doc ->
                    doc.toObject(AppliedJob::class.java)?.copy(id = doc.id)
                }

                if (appliedJobs.isNotEmpty()) {
                    binding.trackApplicationsRecyclerView.visibility = View.VISIBLE
                    binding.noTrackedJobsText.visibility = View.GONE

                    val adapter = TrackJobAdapter(appliedJobs) { job ->
                        val bundle = Bundle().apply {
                            putInt("projectId", job.jobId.toInt())
                        }
                        findNavController().navigate(R.id.action_profileFragment_to_jobDetailsFragment, bundle)
                    }

                    binding.trackApplicationsRecyclerView.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.trackApplicationsRecyclerView.adapter = adapter
                } else {
                    binding.trackApplicationsRecyclerView.visibility = View.GONE
                    binding.noTrackedJobsText.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error fetching tracked jobs", e)
            }
    }
    private fun fetchUserData() {
        Log.d("ProfileFragment", "Fetching user data for userId: $userId")

        // Fetch saved jobs
        db.collection("users").document(userId).collection("savedJobs")
            .limit(3)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("ProfileFragment", "Error fetching saved jobs", error)
                    return@addSnapshotListener
                }

                val projects = value?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: throw IllegalStateException("Document data is null")
                        Project(
                            id = (data["id"] as? Number)?.toInt() ?: 0,
                            ownerId = (data["owner_id"] as? Number)?.toInt() ?: 0,
                            title = data["title"] as? String ?: "",
                            status = data["status"] as? String ?: "",
                            subStatus = data["sub_status"] as? String ?: "",
                            seoUrl = data["seo_url"] as? String ?: "",
                            currency = Currency(
                                id = (data["currency"] as? Map<*, *>)?.get("id") as? Int ?: 0,
                                code = (data["currency"] as? Map<*, *>)?.get("code") as? String ?: "",
                                sign = (data["currency"] as? Map<*, *>)?.get("sign") as? String ?: "",
                                name = (data["currency"] as? Map<*, *>)?.get("name") as? String ?: "",
                                exchangeRate = (data["currency"] as? Map<*, *>)?.get("exchange_rate") as? Double ?: 0.0,
                                country = (data["currency"] as? Map<*, *>)?.get("country") as? String ?: "",
                                isExternal = (data["currency"] as? Map<*, *>)?.get("is_external") as? Boolean ?: false,
                                isEscrowcomSupported = (data["currency"] as? Map<*, *>)?.get("is_escrowcom_supported") as? Boolean ?: false
                            ),
                            description = data["description"] as? String,
                            jobs = (data["jobs"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            submitdate = (data["submitdate"] as? Number)?.toLong() ?: 0L,
                            previewDescription = data["preview_description"] as? String ?: "",
                            deleted = data["deleted"] as? Boolean ?: false,
                            nonpublic = data["nonpublic"] as? Boolean ?: false,
                            hidebids = data["hidebids"] as? Boolean ?: false,
                            type = data["type"] as? String ?: "",
                            bidperiod = (data["bidperiod"] as? Number)?.toInt() ?: 0,
                            budget = data["budget"]?.let {
                                Budget(
                                    minimum = (it as? Map<*, *>)?.get("minimum") as? Double,
                                    maximum = (it as? Map<*, *>)?.get("maximum") as? Double,
                                    name = (it as? Map<*, *>)?.get("name") as? String,
                                    projectType = (it as? Map<*, *>)?.get("project_type") as? String,
                                    currencyId = (it as? Map<*, *>)?.get("currency_id") as? Int
                                )
                            },
                            bid_stats = BidStats(
                                bidCount = (data["bid_stats"] as? Map<*, *>)?.get("bid_count") as? Int ?: 0,
                                bidAvg = (data["bid_stats"] as? Map<*, *>)?.get("bid_avg") as? Double ?: 0.0
                            ),
                            upgrades = Upgrades(
                                featured = (data["upgrades"] as? Map<*, *>)?.get("featured") as? Boolean ?: false,
                                sealed = (data["upgrades"] as? Map<*, *>)?.get("sealed") as? Boolean ?: false,
                                nonpublic = (data["upgrades"] as? Map<*, *>)?.get("nonpublic") as? Boolean ?: false,
                                fulltime = (data["upgrades"] as? Map<*, *>)?.get("fulltime") as? Boolean ?: false,
                                urgent = (data["upgrades"] as? Map<*, *>)?.get("urgent") as? Boolean ?: false,
                                qualified = (data["upgrades"] as? Map<*, *>)?.get("qualified") as? Boolean ?: false,
                                nda = (data["upgrades"] as? Map<*, *>)?.get("NDA") as? Boolean ?: false,
                                ipContract = (data["upgrades"] as? Map<*, *>)?.get("ip_contract") as? Boolean ?: false,
                                successBundle = (data["upgrades"] as? Map<*, *>)?.get("success_bundle") as? Boolean,
                                nonCompete = (data["upgrades"] as? Map<*, *>)?.get("non_compete") as? Boolean ?: false,
                                projectManagement = (data["upgrades"] as? Map<*, *>)?.get("project_management") as? Boolean ?: false,
                                pfOnly = (data["upgrades"] as? Map<*, *>)?.get("pf_only") as? Boolean ?: false,
                                recruiter = (data["upgrades"] as? Map<*, *>)?.get("recruiter") as? Boolean
                            ),
                            language = data["language"] as? String ?: "en",
                            location = Location(
                                country = (data["location"] as? Map<*, *>)?.get("country")?.let {
                                    Country(
                                        name = (it as? Map<*, *>)?.get("name") as? String,
                                        flagUrl = (it as? Map<*, *>)?.get("flag_url") as? String,
                                        code = (it as? Map<*, *>)?.get("code") as? String,
                                        iso3 = (it as? Map<*, *>)?.get("iso3") as? String
                                    )
                                },
                                city = (data["location"] as? Map<*, *>)?.get("city") as? String,
                                latitude = (data["location"] as? Map<*, *>)?.get("latitude") as? Double,
                                longitude = (data["location"] as? Map<*, *>)?.get("longitude") as? Double,
                                timezone = (data["location"] as? Map<*, *>)?.get("timezone")?.let {
                                    Timezone(
                                        id = (it as? Map<*, *>)?.get("id") as? String,
                                        country = (it as? Map<*, *>)?.get("country") as? String,
                                        timezone = (it as? Map<*, *>)?.get("timezone") as? String,
                                        offset = (it as? Map<*, *>)?.get("offset") as? Int
                                    )
                                }
                            ),
                            local = data["local"] as? Boolean ?: false,
                            pool_ids = (data["pool_ids"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            enterpriseIds = (data["enterprise_ids"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            isEscrowProject = data["is_escrow_project"] as? Boolean ?: false,
                            isSellerKycRequired = data["is_seller_kyc_required"] as? Boolean ?: false,
                            isBuyerKycRequired = data["is_buyer_kyc_required"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Error parsing document ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                savedJobAdapter.updateJobs(projects)
            }

        // Fetch user profile data
        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("ProfileFragment", "Error fetching user data", error)
                    return@addSnapshotListener
                }

                document?.let {
                    binding.profileName.text = it.getString("name") ?: "No Name"
                    binding.jobTitle.text = it.getString("jobTitle") ?: "No Job Title"
                    binding.facultyText.text = it.getString("faculty") ?: "No Faculty"
                    binding.addressText.text = it.getString("address") ?: "No Address"

                    val profileImageBase64 = it.getString("profileImageBase64")
                    Glide.with(requireContext())
                        .load(profileImageBase64?.let { Base64.decode(it, Base64.DEFAULT) } ?: R.drawable.profile)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(binding.profileImage)
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
    }
}