package com.example.jobify.ui.fragments

import AdminApplicationsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobify.databinding.FragmentAdminApplicationsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminApplicationsFragment : Fragment() {
    private var _binding: FragmentAdminApplicationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminApplicationsAdapter
    private var selectedUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminApplicationsBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminApplicationsAdapter { applicationId, newStatus ->
            updateApplicationStatus(applicationId, newStatus)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminApplicationsFragment.adapter
            setHasFixedSize(true)
        }

        setupUserSpinner()
    }

    private fun setupUserSpinner() {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val users = documents.map { doc ->
                    User(doc.id, doc.getString("name") ?: "Unknown User")
                }

                if (users.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    users.map { "${it.name} (${it.id})" }
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                binding.userSpinner.adapter = spinnerAdapter
                binding.userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedUserId = users[position].id
                        fetchUserApplications(users[position].id)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserApplications(userId: String) {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("users").document(userId).collection("appliedJobs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val applications = documents.map { doc ->
                    AppliedJob(
                        id = doc.id,
                        jobId = doc.getString("jobId") ?: "",
                        jobTitle = doc.getString("jobTitle") ?: "",
                        bidAmount = doc.getDouble("bidAmount") ?: 0.0,
                        deliveryTime = doc.getString("deliveryTime") ?: "",
                        description = doc.getString("description") ?: "",
                        status = doc.getString("status") ?: "Pending",
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                }
                adapter.submitList(applications)
                binding.emptyState.visibility = if (applications.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load applications: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateApplicationStatus(applicationId: String, newStatus: String) {
        selectedUserId?.let { userId ->
            binding.progressBar.visibility = View.VISIBLE
            db.collection("users").document(userId)
                .collection("appliedJobs").document(applicationId)
                .update("status", newStatus)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    fetchUserApplications(userId) // Refresh the list
                    Toast.makeText(requireContext(), "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "No user selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class User(val id: String, val name: String)

}