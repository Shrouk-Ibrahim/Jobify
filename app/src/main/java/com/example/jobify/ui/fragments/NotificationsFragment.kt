package com.example.jobify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jobify.databinding.FragmentNotificationsBinding
import com.example.jobify.databinding.ItemNotificationBinding
import com.example.jobify.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotificationsAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        adapter = NotificationsAdapter(db, auth)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificationsFragment.adapter
            adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() = checkEmptyState()
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkEmptyState()
                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkEmptyState()
            })
        }

        loadNotifications()
        showUnreadNotifications()
    }

    private fun checkEmptyState() {
        if (adapter.itemCount == 0) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading notifications", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val notifications = snapshots?.map { doc ->
                    Notification(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        jobTitle = doc.getString("jobTitle") ?: "",
                        status = doc.getString("status") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0,
                        read = doc.getBoolean("read") ?: false
                    )
                } ?: emptyList()

                adapter.submitList(notifications)
            }
    }

    private fun showUnreadNotifications() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("notifications")
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val latestUnread = documents.firstOrNull()
                    latestUnread?.let {
                        val title = it.getString("title") ?: ""
                        val message = it.getString("message") ?: ""
                        NotificationHelper.showLocalNotification(
                            requireContext(),
                            title,
                            message
                        )
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class Notification(
        val id: String,
        val title: String,
        val message: String,
        val jobTitle: String,
        val status: String,
        val timestamp: Long,
        val read: Boolean
    )

    class NotificationsAdapter(
        private val db: FirebaseFirestore,
        private val auth: FirebaseAuth
    ) : ListAdapter<Notification, NotificationsAdapter.ViewHolder>(DiffCallback()) {

        companion object {
            private val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        }

        inner class ViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(notification: Notification) {
                binding.notificationTitle.text = notification.title
                binding.notificationMessage.text = notification.message
                binding.notificationTime.text = dateFormat.format(Date(notification.timestamp))

                if (notification.read) {
                    binding.root.setBackgroundColor(binding.root.context.getColor(android.R.color.transparent))
                } else {
                    binding.root.setBackgroundColor(binding.root.context.getColor(android.R.color.holo_blue_light))
                }

                itemView.setOnClickListener {
                    if (!notification.read) {
                        db.collection("users").document(auth.currentUser?.uid ?: "")
                            .collection("notifications")
                            .document(notification.id)
                            .update("read", true)
                            .addOnSuccessListener {
                                val updated = notification.copy(read = true)
                                val currentList = currentList.toMutableList()
                                val index = currentList.indexOfFirst { it.id == updated.id }
                                if (index != -1) {
                                    currentList[index] = updated
                                    submitList(currentList)
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(binding.root.context, "Failed to mark as read", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class DiffCallback : DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem == newItem
            }
        }
    }
}
