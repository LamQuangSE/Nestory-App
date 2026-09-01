package com.example.nestory.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.utils.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling all reminders")

            val scheduler = ReminderScheduler(context)
            val db = AppDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = db.reminderDao().getEnabled()
                    activeReminders.forEach { reminder ->
                        scheduler.schedule(reminder)
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling reminders", e)
                }
            }
        }
    }
}
