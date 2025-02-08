package com.example.jobify.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobify.R
import com.example.jobify.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class ProfileFragment : Fragment(), EditProfileDialogFragment.EditProfileDialogListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var db: FirebaseFirestore
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")

    // Add a variable to store the upload type
    private var currentUploadType: String? = null

    companion object {
        private const val PICK_PDF_REQUEST = 1001
        private const val PICK_IMAGE_REQUEST = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        // Fetch user data from Firestore
        fetchUserData()

        // Set up click listeners
        binding.editIcon.setOnClickListener {
            val dialog = EditProfileDialogFragment()
            dialog.setListener(this)
            dialog.show(parentFragmentManager, "EditProfileDialog")
        }

        binding.cameraIcon.setOnClickListener {
            openImagePicker()
        }

        binding.contactInfoButton.setOnClickListener {
            showContactInfo()
        }




    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val base64Image = convertImageToBase64(imageUri)
        if (base64Image != null) {
            db.collection("users").document(userId)
                .update("profileImageBase64", base64Image)
                .addOnSuccessListener {
                    if (isAdded && !isDetached) {
                        Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show()
                        // Load the image from the Base64 string
                        val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                        Glide.with(requireContext())
                            .load(imageBytes)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.profileImage)
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded && !isDetached) {
                        Toast.makeText(requireContext(), "Error updating Firestore", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileFragment", "Error updating Firestore", exception)
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Error converting image", Toast.LENGTH_SHORT).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val imageUri = data.data
                    if (imageUri != null) {
                        uploadProfileImage(imageUri)
                    } else {
                        Log.e("ProfileFragment", "Selected image URI is null")
                        Toast.makeText(requireContext(), "Error: No image selected", Toast.LENGTH_SHORT).show()
                    }
                }
                PICK_PDF_REQUEST -> {
                    val fileUri = data.data
                    // Use the class-level variable to get the upload type
                    val uploadType = currentUploadType
                    if (fileUri != null && uploadType != null) {
                        uploadFileToFirestore(fileUri, uploadType)
                    } else {
                        Toast.makeText(requireContext(), "Error: No file selected or upload type not set", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileFragment", "File URI: $fileUri, Upload Type: $uploadType")
                    }
                    // Reset the upload type after use
                    currentUploadType = null
                }
            }
        }
    }

    private fun uploadFileToFirestore(fileUri: Uri, uploadType: String) {
        Log.d("ProfileFragment", "Starting file upload to Firestore: $uploadType")
        val base64File = convertFileToBase64(fileUri)
        if (base64File != null) {
            Log.d("ProfileFragment", "File converted to Base64 successfully")
            db.collection("users").document(userId)
                .update("${uploadType}Base64", base64File)
                .addOnSuccessListener {
                    if (isAdded && !isDetached) {
                        Toast.makeText(requireContext(), "$uploadType uploaded", Toast.LENGTH_SHORT).show()
                        Log.d("ProfileFragment", "$uploadType uploaded to Firestore")
                        // Fetch user data again to update the UI
                        fetchUserData()
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded && !isDetached) {
                        Toast.makeText(requireContext(), "Error updating Firestore", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileFragment", "Error updating Firestore", exception)
                    }
                }
        } else {
            Log.e("ProfileFragment", "Error converting file to Base64")
            Toast.makeText(requireContext(), "Error converting file to Base64", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertFileToBase64(fileUri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(fileUri)
            val bytes = inputStream?.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error converting file to Base64", e)
            null
        }
    }

    override fun onProfileUpdated() {
        Log.d("ProfileFragment", "onProfileUpdated called")
        fetchUserData()
    }

    private fun fetchUserData() {
        Log.d("ProfileFragment", "Fetching user data")
        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (isAdded && !isDetached) {
                    if (document != null && document.exists()) {
                        Log.d("ProfileFragment", "User data retrieved: ${document.data}")
                        // Update UI with Firestore data
                        binding.profileName.text = document.getString("name") ?: "No Name"
                        binding.jobTitle.text = document.getString("jobTitle") ?: "No Job Title"
                        binding.facultyText.text = document.getString("faculty") ?: "No Faculty"
                        binding.addressText.text = document.getString("address") ?: "No Address"

                        // Load profile image from Base64 string
                        val profileImageBase64 = document.getString("profileImageBase64")
                        if (!profileImageBase64.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                            Glide.with(requireContext())
                                .load(imageBytes)
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .into(binding.profileImage)
                        } else {
                            // Set a default image if no Base64 string is found
                            Glide.with(requireContext())
                                .load(R.drawable.profile)
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .into(binding.profileImage)
                        }


                    }
                }
            }
    }

    // In ProfileFragment.kt

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
// In ProfileFragment.kt

    fun onContactInfoClick(view: View) {
        showContactInfo()
    }
    private fun uploadFile(type: String) {
        Log.d("ProfileFragment", "Starting file upload with type: $type")
        // Store the upload type in the class-level variable
        currentUploadType = type
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            this.type = "application/pdf"
        }
        startActivityForResult(intent, PICK_PDF_REQUEST)
    }

    private fun showFile(type: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (isAdded && !isDetached) {
                    if (document != null) {
                        val fileBase64 = document.getString("${type}Base64")
                        if (!fileBase64.isNullOrEmpty()) {
                            // Decode the Base64 string to bytes
                            val fileBytes = Base64.decode(fileBase64, Base64.DEFAULT)

                            // Save the file to a temporary location
                            val file = saveFileToCache(fileBytes, type)

                            // Open the file using an appropriate app
                            openFile(file)
                        } else {
                            Toast.makeText(requireContext(), "No $type file found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded && !isDetached) {
                    Toast.makeText(requireContext(), "Error fetching $type file", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileFragment", "Error fetching $type file", exception)
                }
            }
    }

    private fun saveFileToCache(fileBytes: ByteArray, type: String): File {
        // Create a temporary file in the cache directory
        val fileName = "${type}_${System.currentTimeMillis()}.pdf"
        val file = File(requireContext().cacheDir, fileName)
        file.writeBytes(fileBytes)
        return file
    }

    private fun openFile(file: File) {
        // Create an intent to open the file
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        intent.setDataAndType(fileUri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Start the activity to open the file
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No app found to open the file", Toast.LENGTH_SHORT).show()
        }
    }
}