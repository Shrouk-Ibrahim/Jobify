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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobAdapter(private var projects: List<Project>,private val navController: NavController) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val project = projects[position]
        holder.title.text = project.title

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("projectId", project.id)
            }
            navController.navigate(R.id.action_to_jobDetailsFragment, bundle)
        }



        // Fetch an image from Unsplash based on the project title
        UnsplashRetrofitClient.instance.searchPhotos(project.title).enqueue(object : Callback<UnsplashResponse> {
            override fun onResponse(call: Call<UnsplashResponse>, response: Response<UnsplashResponse>) {
                if (response.isSuccessful) {
                    val photoUrl = response.body()?.results?.firstOrNull()?.urls?.regular
                    photoUrl?.let {
                        Glide.with(holder.itemView.context)
                            .load(it)
                            .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder image
                            .error(android.R.drawable.ic_dialog_alert) // Error image
                            .into(holder.image)
                    }
                } else {
                    Log.e("JobAdapter", "Failed to fetch image from Unsplash: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UnsplashResponse>, t: Throwable) {
                Log.e("JobAdapter", "Error fetching image from Unsplash", t)
            }
        })

        // Check if the job is saved by the current user
        userId?.let { uid ->
            db.collection("users").document(uid).collection("savedJobs")
                .document(project.id.toString())
                .get()
                .addOnSuccessListener { document ->
                    val isSaved = document.exists()
                    holder.isSaved = isSaved
                    holder.saveIcon.setImageResource(if (isSaved) R.drawable.savefilled else R.drawable.save)
                }
                .addOnFailureListener { e ->
                    Log.e("JobAdapter", "Error checking if job is saved", e)
                }
        } ?: run {
            Log.e("JobAdapter", "User ID is null, cannot check saved jobs")
        }

        // Handle save/unsave button click
        holder.saveIcon.setOnClickListener {
            userId?.let { uid ->
                if (holder.isSaved) {
                    // Unsave the job
                    holder.saveIcon.setImageResource(R.drawable.save)
                    holder.isSaved = false

                    db.collection("users").document(uid).collection("savedJobs")
                        .document(project.id.toString())
                        .delete()
                        .addOnSuccessListener {
                            notifyItemChanged(position)
                        }
                        .addOnFailureListener { e ->
                            holder.saveIcon.setImageResource(R.drawable.savefilled)
                            holder.isSaved = true
                        }
                } else {
                    // Save the job
                    holder.saveIcon.setImageResource(R.drawable.savefilled)
                    holder.isSaved = true


                    db.collection("users").document(uid).collection("savedJobs")
                        .document(project.id.toString())
                        .set(project)
                        .addOnSuccessListener {
                            notifyItemChanged(position)
                        }
                        .addOnFailureListener { e ->
                            holder.saveIcon.setImageResource(R.drawable.save)
                            holder.isSaved = false
                        }
                }
            } ?: run {
                Log.e("JobAdapter", "User ID is null, cannot save/unsave job")
            }
        }
    }


    fun updateJobs(newJobs: List<Project>) {
        projects = newJobs.ifEmpty { emptyList() }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = projects.size

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val saveIcon: ImageView = itemView.findViewById(R.id.save_icon)
        val image: ImageView = itemView.findViewById(R.id.job_image) // Ensure this ID exists in item_job.xml
        var isSaved: Boolean = false
    }
}