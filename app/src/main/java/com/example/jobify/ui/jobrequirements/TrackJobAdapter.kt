// TrackJobAdapter.kt
package com.example.jobify.ui.jobrequirements

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.jobify.R
import com.example.jobify.ui.fragments.AppliedJob
import java.text.NumberFormat
import java.util.Locale

class TrackJobAdapter(
    private var jobs: List<AppliedJob>,
    private val onItemClick: (AppliedJob) -> Unit
) : RecyclerView.Adapter<TrackJobAdapter.TrackJobViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    fun updateJobs(newJobs: List<AppliedJob>) {
        jobs = newJobs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackJobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track_job, parent, false)
        return TrackJobViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackJobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount() = jobs.size

    inner class TrackJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.jobTitle)
        private val bidAmount: TextView = itemView.findViewById(R.id.bidAmount)
        private val deliveryTime: TextView = itemView.findViewById(R.id.deliveryTime)
        private val description: TextView = itemView.findViewById(R.id.jobDescription)
        private val status: TextView = itemView.findViewById(R.id.jobStatus)

        fun bind(job: AppliedJob) {
            title.text = job.jobTitle
            bidAmount.text = "Bid: ${currencyFormat.format(job.bidAmount)}"
            deliveryTime.text = "Delivery: ${job.deliveryTime}"
            description.text = job.description

            // Set status with color coding
            status.text = job.status
            status.setTextColor(
                when (job.status.lowercase()) {
                    "accepted" -> ContextCompat.getColor(itemView.context, R.color.green)
                    "rejected" -> ContextCompat.getColor(itemView.context, R.color.red)
                    "pending" -> ContextCompat.getColor(itemView.context, R.color.orange)
                    else -> Color.BLACK
                }
            )

            itemView.setOnClickListener {
                onItemClick(job)
            }
        }
    }
}