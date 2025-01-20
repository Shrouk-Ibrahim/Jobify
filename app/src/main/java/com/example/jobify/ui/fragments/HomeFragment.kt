package com.example.jobify.ui.fragments

import Category
import Job
import JobAdapter
import JobViewModel
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobify.databinding.FragmentHomeBinding

import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: JobViewModel
    private lateinit var jobAdapter: JobAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupSearch()
        setupFilter()
        viewModel.fetchJobs()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(JobViewModel::class.java)
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(emptyList())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = jobAdapter
        }
    }

    private fun setupObservers() {
        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            jobs?.let {
                jobAdapter.updateJobs(jobs)
                updateCategories(jobs)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            private val handler = Handler(Looper.getMainLooper())
            private var runnable: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                runnable?.let { handler.removeCallbacks(it) }
                runnable = Runnable {
                    viewModel.fetchJobs(query = s?.toString())
                }
                handler.postDelayed(runnable!!, 500)
            }
        })
    }

    private fun setupFilter() {
        binding.filterIcon.setOnClickListener {
            if (categories.isNotEmpty()) {
                showCategoryFilterDialog()
            } else {
                Snackbar.make(binding.root, "Loading categories...", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCategoryFilterDialog() {
        val categoryNames = categories.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(categoryNames.size)

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Category")
            .setMultiChoiceItems(categoryNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Apply") { _, _ ->
                val selectedCategories = categories.filterIndexed { index, _ ->
                    checkedItems[index]
                }.map { it.id }
                viewModel.fetchJobs(categories = selectedCategories)
            }
            .setNegativeButton("Clear") { _, _ ->
                viewModel.fetchJobs(categories = emptyList())
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun updateCategories(jobs: List<Job>) {
        categories.clear()
        categories.addAll(jobs.map { it.category }.distinctBy { it.id })
    }
}