import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobAdapter(private var jobs: List<Job>) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]
        holder.title.text = job.name
        holder.description.text = job.category.name

        // Check if the job is saved by the current user
        userId?.let { uid ->
            db.collection("users").document(uid).collection("savedJobs")
                .document(job.id.toString())
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

        holder.saveIcon.setOnClickListener {
            userId?.let { uid ->
                if (holder.isSaved) {
                    // Unsave the job
                    holder.saveIcon.setImageResource(R.drawable.save)
                    holder.isSaved = false

                    // Remove from savedJobs
                    db.collection("users").document(uid).collection("savedJobs")
                        .document(job.id.toString())
                        .delete()
                        .addOnSuccessListener {
                            notifyItemChanged(position) // Refresh the UI
                        }
                        .addOnFailureListener { e ->
                            // Revert UI on failure
                            holder.saveIcon.setImageResource(R.drawable.savefilled)
                            holder.isSaved = true
                        }
                } else {
                    // Save the job
                    holder.saveIcon.setImageResource(R.drawable.savefilled)
                    holder.isSaved = true

                    // Save to user's savedJobs
                    // In JobAdapter.kt's onBindViewHolder()
                    val jobData = hashMapOf(
                        "id" to job.id,
                        "name" to job.name,
                        "category" to hashMapOf("id" to job.category.id, "name" to job.category.name),
                        "timestamp" to FieldValue.serverTimestamp(),
                        // Add other fields if necessary
                        "active_project_count" to job.active_project_count,
                        "seo_url" to job.seo_url
                    )
                    db.collection("users").document(uid).collection("savedJobs")
                        .document(job.id.toString())
                        .set(jobData)
                        .addOnSuccessListener {
                            notifyItemChanged(position) // Refresh the UI
                        }
                        .addOnFailureListener { e ->
                            // Revert UI on failure
                            holder.saveIcon.setImageResource(R.drawable.save)
                            holder.isSaved = false
                        }
                }
            } ?: run {
                Log.e("JobAdapter", "User ID is null, cannot save/unsave job")
            }
        }

        // Load image from Unsplash
        UnsplashRetrofitClient.instance.searchPhotos(job.category.name).enqueue(object : Callback<UnsplashResponse> {
            override fun onResponse(call: Call<UnsplashResponse>, response: Response<UnsplashResponse>) {
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

    fun updateJobs(newJobs: List<Job>) {
        jobs = newJobs.ifEmpty { emptyList() } // Handle empty lists
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = jobs.size

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val image: ImageView = itemView.findViewById(R.id.job_image)
        val saveIcon: ImageView = itemView.findViewById(R.id.save_icon)
        var isSaved: Boolean = false
    }
}