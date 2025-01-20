import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobify.R

class JobAdapter(private var jobs: List<Job>) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    // Create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]
        Log.d("JobAdapter", "Binding job: ${job.name}") // Log the job being bound

        // Bind job data to the views
        holder.title.text = job.name
        holder.description.text = job.category.name
        holder.budget.text = "Projects: ${job.active_project_count ?: 0}" // Handle null active_project_count
    }

    // Return the number of items in the list
    override fun getItemCount(): Int = jobs.size

    // Update the jobs list and notify the adapter
    fun updateJobs(newJobs: List<Job>) {
        jobs = newJobs
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    // ViewHolder class
    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val budget: TextView = itemView.findViewById(R.id.budget)
    }
}