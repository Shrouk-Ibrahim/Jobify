package com.example.jobify.ui.fragments

import BidStats
import Budget
import Country
import Currency
import Location
import Project
import Timezone
import Upgrades
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobify.R
import com.example.jobify.databinding.FragmentSavedJobsBinding
import com.example.jobify.ui.jobrequirements.SavedJobHorizontalAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedJobsFragment : Fragment() {

    private lateinit var binding: FragmentSavedJobsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var savedJobAdapter: SavedJobHorizontalAdapter

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("SavedJobsFragment", "User is not logged in")
            throw IllegalStateException("User not logged in")
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore and adapter
        db = FirebaseFirestore.getInstance()

        // Calculate the number of columns based on screen width
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidthDp: Float = displayMetrics.widthPixels / displayMetrics.density
        val columnCount: Int = (screenWidthDp / 180).toInt() // Adjust 180dp to your preferred item width
        val navController = findNavController()
        // Initialize the adapter
        savedJobAdapter = SavedJobHorizontalAdapter(emptyList(),navController)

        // Set up RecyclerView with GridLayoutManager
        binding.savedJobsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), columnCount).apply {
                isSmoothScrollbarEnabled = true // Enables smooth scrolling
            }
            setHasFixedSize(true) // Improves performance
            adapter = savedJobAdapter
            addItemDecoration(SpacingItemDecoration(16)) // Adds spacing dynamically
        }

        // Fetch user data to load profile photo
        fetchUserProfilePhoto()

        // Set up the click listener for the profile photo
        binding.profilePhoto.setOnClickListener {
            // Navigate to ProfileFragment
            findNavController().navigate(R.id.profileFragment)
        }

        // Fetch saved jobs
        fetchSavedJobs()
    }

    private fun fetchSavedJobs() {
        db.collection("users").document(userId).collection("savedJobs")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("SavedJobsFragment", "Error fetching saved jobs", error)
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
                        Log.e("SavedJobsFragment", "Error parsing document ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                savedJobAdapter.updateJobs(projects)
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
    class SpacingItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.left = space
            outRect.right = space
            outRect.top = space
            outRect.bottom = space
        }
}}