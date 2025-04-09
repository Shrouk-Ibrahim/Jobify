package com.example.jobify.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.jobify.R
import com.example.jobify.ui.activites.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private val db = FirebaseFirestore.getInstance()

    suspend fun sendNotification(
        context: Context,
        userId: String,
        title: String,
        message: String,
        jobTitle: String = "",
        status: String = ""
    ) {
        try {
            // Create notification document in Firestore
            val notificationData = hashMapOf(
                "title" to title,
                "message" to message,
                "jobTitle" to jobTitle,
                "status" to status,
                "timestamp" to System.currentTimeMillis(),
                "read" to false
            )

            // Add notification to Firestore
            val docRef = db.collection("users").document(userId)
                .collection("notifications")
                .add(notificationData)
                .await()

            Log.d(TAG, "Notification stored in Firestore with ID: ${docRef.id}")

            // Show immediate notification
            showLocalNotification(context, title, message)

        } catch (e: Exception) {
            Log.w(TAG, "Error sending notification", e)
        }
    }

    fun showLocalNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = context.getString(R.string.default_notification_channel_id)

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}