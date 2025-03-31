import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jobify.R
import com.example.jobify.ui.fragments.AppliedJob
import com.google.android.material.button.MaterialButton

class AdminApplicationsAdapter(
    private val onStatusUpdate: (String, String) -> Unit
) : ListAdapter<AppliedJob, AdminApplicationsAdapter.ApplicationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_application, parent, false)
        return ApplicationViewHolder(view, onStatusUpdate)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ApplicationViewHolder(
        itemView: View,
        private val onStatusUpdate: (String, String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val jobTitleTextView: TextView = itemView.findViewById(R.id.jobTitle)
        private val bidAmountTextView: TextView = itemView.findViewById(R.id.bidAmount)
        private val deliveryTimeTextView: TextView = itemView.findViewById(R.id.deliveryTime)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description)
        private val statusTextView: TextView = itemView.findViewById(R.id.status)
        private val acceptButton: MaterialButton = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: MaterialButton = itemView.findViewById(R.id.rejectButton)

        fun bind(appliedJob: AppliedJob) {
            jobTitleTextView.text = "Job: ${appliedJob.jobTitle}"
            bidAmountTextView.text = "Bid Amount: $${appliedJob.bidAmount}"
            deliveryTimeTextView.text = "Delivery Time: ${appliedJob.deliveryTime} days"
            descriptionTextView.text = "Description: ${appliedJob.description}"
            statusTextView.text = "Status: ${appliedJob.status}"

            updateStatusUI(appliedJob.status)

            acceptButton.setOnClickListener {
                onStatusUpdate(appliedJob.id, "Accepted")
            }

            rejectButton.setOnClickListener {
                onStatusUpdate(appliedJob.id, "Rejected")
            }
        }

        private fun updateStatusUI(status: String) {
            when (status.lowercase()) {
                "accepted" -> {
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                    acceptButton.visibility = View.GONE
                    rejectButton.visibility = View.GONE
                }
                "rejected" -> {
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                    acceptButton.visibility = View.GONE
                    rejectButton.visibility = View.GONE
                }
                else -> {
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.orange))
                    acceptButton.visibility = View.VISIBLE
                    rejectButton.visibility = View.VISIBLE
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AppliedJob>() {
        override fun areItemsTheSame(oldItem: AppliedJob, newItem: AppliedJob): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppliedJob, newItem: AppliedJob): Boolean {
            return oldItem == newItem
        }
    }
}