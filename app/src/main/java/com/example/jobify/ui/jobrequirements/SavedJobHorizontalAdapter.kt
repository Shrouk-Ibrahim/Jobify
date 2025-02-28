package com.example.jobify.ui.jobrequirements

import Project
import UnsplashResponse
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobify.R
import com.example.jobify.ui.fragments.JobDetailsFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SavedJobHorizontalAdapter(private var projects: List<Project>, private val navController: NavController) :
    RecyclerView.Adapter<SavedJobHorizontalAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_horizontal, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val project = projects[position]
        holder.title.text = project.title
        holder.description.text = project.description


        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("projectId", project.id)
            }
            navController.navigate(R.id.action_to_jobDetailsFragment, bundle)
        }

        UnsplashRetrofitClient.instance.searchPhotos(project.title).enqueue(object :
            Callback<UnsplashResponse> {
            override fun onResponse(
                call: Call<UnsplashResponse>,
                response: Response<UnsplashResponse>
            ) {
                if (response.isSuccessful) {
                    val photoUrl = response.body()?.results?.firstOrNull()?.urls?.regular
                    photoUrl?.let {
                        Glide.with(holder.itemView.context)
                            .load(it)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_dialog_alert)
                            .into(holder.image)
                    }
                }
            }

            override fun onFailure(call: Call<UnsplashResponse>, t: Throwable) {
                Log.e("JobAdapter", "Failed to load image: ${t.message}")
            }
        })
    }


        override fun getItemCount(): Int = projects.size

    fun updateJobs(newJobs: List<Project>) {
        projects = newJobs
        notifyDataSetChanged()
    }

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.job_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
    }
}