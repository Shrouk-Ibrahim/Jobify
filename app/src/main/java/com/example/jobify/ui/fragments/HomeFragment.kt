package com.example.jobify.ui.fragments

import JobAdapter
import JobViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobify.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: JobViewModel
    private lateinit var jobAdapter: JobAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(JobViewModel::class.java)

        // Initialize RecyclerView and Adapter
        jobAdapter = JobAdapter(emptyList())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = jobAdapter
        }

        // Observe jobs LiveData
        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            jobAdapter.updateJobs(jobs ?: emptyList())
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                // Show error message (e.g., using Snackbar)
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }

        // Fetch jobs
        viewModel.fetchJobs()
    }
}