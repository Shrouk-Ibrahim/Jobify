import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobify.R

class JobAdapter(private var jobs: List<Job>) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]
        Log.d("JobAdapter", "Binding job: ${job.name}")

        holder.title.text = job.name
        holder.description.text = job.category.name
    }

    override fun getItemCount(): Int = jobs.size

    fun updateJobs(newJobs: List<Job>) {
        jobs = newJobs
        notifyDataSetChanged()
        Log.d("JobAdapter", "Updated with ${newJobs.size} jobs")
    }

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
    }

}