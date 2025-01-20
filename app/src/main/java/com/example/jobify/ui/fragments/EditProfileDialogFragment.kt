package com.example.jobify.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.jobify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileDialogFragment : DialogFragment() {

    private lateinit var db: FirebaseFirestore
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")

    // Define a callback interface
    interface EditProfileDialogListener {
        fun onProfileUpdated()
    }

    private var listener: EditProfileDialogListener? = null

    // Set the listener (ProfileFragment will implement this interface)
    fun setListener(listener: EditProfileDialogListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_profile_dialog, null)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize EditText fields
        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val jobTitleEditText = view.findViewById<EditText>(R.id.jobTitleEditText)
        val facultyEditText = view.findViewById<EditText>(R.id.facultyEditText)
        val addressEditText = view.findViewById<EditText>(R.id.addressEditText)

        // Fetch current data and populate the dialog fields
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    nameEditText.setText(document.getString("name"))
                    jobTitleEditText.setText(document.getString("jobTitle"))
                    facultyEditText.setText(document.getString("faculty"))
                    addressEditText.setText(document.getString("address"))
                } else {
                    Log.e("EditProfileDialog", "Document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileDialog", "Error fetching profile data", exception)
                Toast.makeText(requireContext(), "Error fetching profile data", Toast.LENGTH_SHORT).show()
            }

        // Build the dialog
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Edit Profile")
            .setPositiveButton("Save") { _, _ ->
                // Get updated values from EditText fields
                val name = nameEditText.text.toString()
                val jobTitle = jobTitleEditText.text.toString()
                val faculty = facultyEditText.text.toString()
                val address = addressEditText.text.toString()

                // Save updated data to Firestore
                db.collection("users").document(userId)
                    .update(
                        hashMapOf(
                            "name" to name,
                            "jobTitle" to jobTitle,
                            "faculty" to faculty,
                            "address" to address
                        ) as Map<String, Any> // Explicit cast
                    )
                    .addOnSuccessListener {
                        if (isAdded) {
                            Log.d("EditProfileDialog", "Profile updated successfully")
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                            // Notify the listener (ProfileFragment) that the profile was updated
                            listener?.onProfileUpdated()
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (isAdded) {
                            Log.e("EditProfileDialog", "Error updating profile", exception)
                            Toast.makeText(requireContext(), "Error updating profile", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}