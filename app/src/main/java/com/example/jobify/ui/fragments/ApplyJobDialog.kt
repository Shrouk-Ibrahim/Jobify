// ApplyJobDialog.kt
package com.example.jobify.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.jobify.R
import com.example.jobify.databinding.DialogApplyJobBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class ApplyJobDialog : DialogFragment() {
    private lateinit var binding: DialogApplyJobBinding
    private lateinit var db: FirebaseFirestore
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")

    var jobId: String = ""
    var jobTitle: String = ""

    interface ApplyJobListener {
        fun onJobApplied()
    }

    private var listener: ApplyJobListener? = null

    fun setListener(listener: ApplyJobListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogApplyJobBinding.inflate(LayoutInflater.from(requireContext()))
        db = FirebaseFirestore.getInstance()

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Apply for Job")

        binding.jobTitle.text = jobTitle

        // Set up input validation
        setupInputValidation()

        checkIfAlreadyApplied()

        binding.applyButton.setOnClickListener {
            clearErrorMessages()

            val bidAmountText = binding.bidAmountEditText.text.toString()
            val deliveryTime = binding.deliveryTimeEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            if (jobTitle.isEmpty()) {
                showErrorMessage("Job title not available")
                return@setOnClickListener
            }
            if (bidAmountText.isEmpty()) {
                showErrorMessage("Bid amount is required", binding.bidAmountEditText)
                return@setOnClickListener
            }
            if (deliveryTime.isEmpty()) {
                showErrorMessage("Delivery time is required", binding.deliveryTimeEditText)
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                showErrorMessage("Description is required", binding.descriptionEditText)
                return@setOnClickListener
            }

            val bidAmount = bidAmountText.toDoubleOrNull() ?: run {
                showErrorMessage("Invalid bid amount", binding.bidAmountEditText)
                return@setOnClickListener
            }

            if (bidAmount <= 0) {
                showErrorMessage("Bid amount must be positive", binding.bidAmountEditText)
                return@setOnClickListener
            }

            if (deliveryTime.toIntOrNull() == null || deliveryTime.toInt() <= 0) {
                showErrorMessage("Delivery time must be a positive number", binding.deliveryTimeEditText)
                return@setOnClickListener
            }

            if (description.length < 20) {
                showErrorMessage("Description must be at least 20 characters", binding.descriptionEditText)
                return@setOnClickListener
            }

            applyForJob(bidAmount, deliveryTime, description)
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    private fun clearErrorMessages() {

        binding.bidAmountEditText.error = null
        binding.deliveryTimeEditText.error = null
        binding.descriptionEditText.error = null
    }

    private fun showErrorMessage(message: String, view: View? = null) {


        view?.let {
            when (it) {
                binding.bidAmountEditText -> binding.bidAmountEditText.error = message
                binding.deliveryTimeEditText -> binding.deliveryTimeEditText.error = message
                binding.descriptionEditText -> binding.descriptionEditText.error = message
            }
        }
    }

    private fun setupInputValidation() {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

        binding.bidAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.toDoubleOrNull()?.let {
                    binding.bidAmountText.text = "Bid Amount: ${currencyFormat.format(it)}"
                } ?: run {
                    binding.bidAmountText.text = "Bid Amount:"
                }
            }
        })

        binding.deliveryTimeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.toIntOrNull()?.let {
                    binding.deliveryTimeText.text = "Delivery Time: $it days"
                } ?: run {
                    binding.deliveryTimeText.text = "Delivery Time:"
                }
            }
        })
    }

    private fun checkIfAlreadyApplied() {
        db.collection("users").document(userId).collection("appliedJobs")
            .whereEqualTo("jobId", jobId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    binding.applyButton.isEnabled = false
                    binding.applyButton.text = "Already Applied"
                    binding.applyButton.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
                    binding.bidAmountEditText.isEnabled = false
                    binding.deliveryTimeEditText.isEnabled = false
                    binding.descriptionEditText.isEnabled = false
                    showErrorMessage("You've already applied to this job")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ApplyJobDialog", "Error checking applications", e)
                showErrorMessage("Error checking application status")
            }
    }

    private fun applyForJob(bidAmount: Double, deliveryTime: String, description: String) {
        val appliedJob = hashMapOf(
            "jobId" to jobId,
            "jobTitle" to jobTitle,
            "bidAmount" to bidAmount,
            "deliveryTime" to deliveryTime,
            "description" to description,
            "status" to "Pending",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("appliedJobs")
            .add(appliedJob)
            .addOnSuccessListener {
                listener?.onJobApplied()
                dismiss()
            }
            .addOnFailureListener { e ->
                Log.e("ApplyJobDialog", "Error applying for job", e)
                showErrorMessage("Failed to submit application. Please try again.")
            }
    }
}