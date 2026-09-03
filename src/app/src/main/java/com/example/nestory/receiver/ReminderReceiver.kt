package com.example.nestory.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.utils.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        if (reminderId == -1L) return

        Log.d("ReminderReceiver", "Received reminder alarm for ID: $reminderId")
        val pendingResult = goAsync()

        // Use Coroutine to handle DB access in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val reminder = db.reminderDao().getById(reminderId)
                
                if (reminder != null && reminder.isEnabled) {
                    val doc = reminder.documentId?.let { db.documentDao().getById(it) }
                    val kit = reminder.documentKitId?.let { db.documentKitDao().getKitById(it) }
                    val title = doc?.title ?: kit?.name
                    if (title != null) {
                        val notificationHelper = NotificationHelper(context)
                        notificationHelper.showExpiryNotification(
                            id = reminderId.toInt(),
                            title = "Nhắc nhở giấy tờ",
                            message = "Đã đến hạn nhắc nhở cho \"$title\"",
                            documentId = doc?.id
                        )
                    } else {
                        Log.d("ReminderReceiver", "Document/Kit not found for reminder $reminderId, skipping notification")
                    }
                } else {
                    Log.d("ReminderReceiver", "Reminder $reminderId is disabled or not found, skipping notification")
                }
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error processing reminder $reminderId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
