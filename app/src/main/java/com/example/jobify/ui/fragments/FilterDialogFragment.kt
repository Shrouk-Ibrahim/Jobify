package com.example.jobify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.jobify.R

class FilterDialogFragment : DialogFragment() {

    interface FilterDialogListener {
        fun onFilterApplied(minBudget: Double?, maxBudget: Double?)
        fun onResetFilters()
    }

    private var listener: FilterDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter_dialog, container, false)

        val minBudgetEditText = view.findViewById<EditText>(R.id.minBudget)
        val maxBudgetEditText = view.findViewById<EditText>(R.id.maxBudget)


        view.findViewById<Button>(R.id.applyButton).setOnClickListener {
            val minBudget = minBudgetEditText.text.toString().toDoubleOrNull()
            val maxBudget = maxBudgetEditText.text.toString().toDoubleOrNull()

            listener?.onFilterApplied(minBudget, maxBudget)
            dismiss()
        }

        view.findViewById<Button>(R.id.resetButton).setOnClickListener {
            listener?.onResetFilters()
            dismiss()
        }

        view.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }

        return view
    }

    fun setListener(listener: FilterDialogListener) {
        this.listener = listener
    }
}