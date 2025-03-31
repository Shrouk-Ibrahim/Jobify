package com.example.jobify.ui.jobrequirements

import Project
import UnsplashResponse
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobify.R
import com.example.jobify.ui.dialogs.ApplyJobDialog
import com.example.jobify.ui.fragments.JobDetailsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SavedJobHorizontalAdapter(
    private var projects: List<Project>,
    private val navController: NavController
) : RecyclerView.Adapter<SavedJobHorizontalAdapter.JobViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    fun getOriginalList(): List<Project> {
        return projects
    }

    fun updateJobs(newJobs: List<Project>) {
        projects = newJobs
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val project = projects[position]
        holder.title.text = project.title

        // Get current adapter position safely
        val adapterPosition = holder.bindingAdapterPosition
        if (adapterPosition == RecyclerView.NO_POSITION) return

        // Set click listener on the root view
        holder.itemView.findViewById<View>(R.id.root_view).setOnClickListener {
            Log.d("savedjobhorizontal", "Item clicked! Navigating to JobDetailsFragment with projectId: ${project.id}")
            val bundle = Bundle().apply {
                putInt("projectId", project.id)
            }
            navController.navigate(R.id.action_to_jobDetailsFragment, bundle)
        }

        // Fetch image from Unsplash
        UnsplashRetrofitClient.instance.searchPhotos(project.title)
            .enqueue(object : Callback<UnsplashResponse> {
                override fun onResponse(
                    call: Call<UnsplashResponse>,
                    response: Response<UnsplashResponse>
                ) {
                    if (response.isSuccessful) {
                        val photoUrl = response.body()?.results?.firstOrNull()?.urls?.regular
                        photoUrl?.let {
                            Glide.with(holder.itemView.context)
                                .load(photoUrl)
                                .override(300, 200)
                                .centerCrop()
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(holder.image)
                        }
                    }
                }

                override fun onFailure(call: Call<UnsplashResponse>, t: Throwable) {
                    Log.e("SavedJobHorizontalAdapter", "Error fetching image", t)
                }
            })

        // Check if job is saved
        userId?.let { uid ->
            db.collection("users").document(uid).collection("savedJobs")
                .document(project.id.toString())
                .get()
                .addOnSuccessListener { document ->
                    val isSaved = document.exists()
                    holder.isSaved = isSaved
                    holder.saveIcon.setImageResource(if (isSaved) R.drawable.savefilled else R.drawable.save)
                }
        }

        // Save/Unsave job
        holder.saveIcon.setOnClickListener {
            userId?.let { uid ->
                if (holder.isSaved) {
                    // Unsave
                    db.collection("users").document(uid).collection("savedJobs")
                        .document(project.id.toString())
                        .delete()
                        .addOnSuccessListener {
                            holder.saveIcon.setImageResource(R.drawable.save)
                            holder.isSaved = false
                        }
                } else {
                    // Save
                    db.collection("users").document(uid).collection("savedJobs")
                        .document(project.id.toString())
                        .set(project)
                        .addOnSuccessListener {
                            holder.saveIcon.setImageResource(R.drawable.savefilled)
                            holder.isSaved = true
                        }
                }
            }
        }
        // Function to update button appearance
        fun updateButtonStatus(status: String) {
            holder.applyButton.apply {
                isEnabled = false
                text = status.replaceFirstChar { it.uppercase() }
                when (status.lowercase()) {
                    "accepted" -> {
                        backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green))
                    }

                    "rejected" -> {
                        backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                    }

                    "pending" -> {
                        backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.orange))
                    }

                    else -> {
                        backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey))
                    }
                }
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }

        // Check application status
        db.collection("users").document(userId ?: "").collection("appliedJobs")
            .whereEqualTo("jobId", project.id.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Not applied yet
                    holder.applyButton.apply {
                        text = "Apply"
                        isEnabled = true
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.primaryColor
                            )
                        )
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                } else {
                    // Applied - get status
                    val status = documents.documents.firstOrNull()?.getString("status") ?: "pending"
                    updateButtonStatus(status)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SavedJobAdapter", "Error checking application status", e)
            }

        // Apply button click listener
        holder.applyButton.setOnClickListener {
            val dialog = ApplyJobDialog().apply {
                jobId = project.id.toString()
                jobTitle = project.title ?: ""
                setListener(object : ApplyJobDialog.ApplyJobListener {
                    override fun onJobApplied() {
                        // Immediately update UI to show pending status
                        updateButtonStatus("pending")
                    }
                })
            }
            dialog.show(
                (holder.itemView.context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                "ApplyJobDialog"
            )
        }
    }

    override fun getItemCount(): Int = projects.size

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.job_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val saveIcon: ImageView = itemView.findViewById(R.id.save_icon)
        val applyButton: Button = itemView.findViewById(R.id.applyButton)
        var isSaved: Boolean = false
    }
}