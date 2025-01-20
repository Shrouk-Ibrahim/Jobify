package com.example.jobify.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.jobify.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class ContactInfoDialogFragment : DialogFragment() {

    private lateinit var db: FirebaseFirestore
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")

    interface ContactInfoDialogListener {
        fun onContactInfoUpdated()
    }

    private var listener: ContactInfoDialogListener? = null

    fun setListener(listener: ContactInfoDialogListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        db = FirebaseFirestore.getInstance()

        // Inflate the custom dialog layout
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_contact_info, null)

        // Initialize views
        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val linkedInEditText = view.findViewById<EditText>(R.id.linkedInEditText)
        val githubEditText = view.findViewById<EditText>(R.id.githubEditText)
        val phoneEditText = view.findViewById<EditText>(R.id.phoneEditText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)

        // Fetch current contact info from Firestore
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    emailEditText.setText(document.getString("email") ?: "")
                    linkedInEditText.setText(document.getString("linkedIn") ?: "")
                    githubEditText.setText(document.getString("github") ?: "")
                    phoneEditText.setText(document.getString("phone") ?: "")
                }
            }

        // Enable/Disable Save button based on field input
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = emailEditText.text.toString().trim()
                val linkedIn = linkedInEditText.text.toString().trim()
                val github = githubEditText.text.toString().trim()
                val phone = phoneEditText.text.toString().trim()

                // Enable Save button only if all fields are filled
                saveButton.isEnabled = email.isNotEmpty() && linkedIn.isNotEmpty() && github.isNotEmpty() && phone.isNotEmpty()
            }
        }

        // Add text change listeners to all fields
        emailEditText.addTextChangedListener(textWatcher)
        linkedInEditText.addTextChangedListener(textWatcher)
        githubEditText.addTextChangedListener(textWatcher)
        phoneEditText.addTextChangedListener(textWatcher)

        // Set up the Save button
        saveButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val linkedIn = linkedInEditText.text.toString()
            val github = githubEditText.text.toString()
            val phone = phoneEditText.text.toString()

            // Update Firestore with the new contact info
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "email" to email,
                        "linkedIn" to linkedIn,
                        "github" to github,
                        "phone" to phone
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Contact info updated", Toast.LENGTH_SHORT).show()
                    listener?.onContactInfoUpdated() // Notify the listener
                    dismiss() // Close the dialog
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error updating contact info", Toast.LENGTH_SHORT).show()
                    Log.e("ContactInfoDialog", "Error updating Firestore", exception)
                }
        }

        // Set up the Cancel button
        cancelButton.setOnClickListener {
            dismiss() // Close the dialog
        }

        // Make LinkedIn and GitHub links clickable
        linkedInEditText.setOnClickListener {
            val linkedInUrl = linkedInEditText.text.toString()
            if (linkedInUrl.isNotEmpty()) {
                openUrl(linkedInUrl)
            }
        }

        githubEditText.setOnClickListener {
            val githubUrl = githubEditText.text.toString()
            if (githubUrl.isNotEmpty()) {
                openUrl(githubUrl)
            }
        }

        // Build the dialog
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}