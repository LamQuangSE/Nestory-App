package com.example.nestory.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.settings.ExpiryReminderSettingsRepository
import com.example.nestory.utils.notification.ReminderScheduler
import com.example.nestory.utils.notification.WorkManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling all reminders")
            
            val scheduler = ReminderScheduler(context)
            val db = AppDatabase.getDatabase(context)
            val settingsRepository = ExpiryReminderSettingsRepository(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1. Lập lịch lại các nhắc nhở tùy chỉnh (Custom Reminders)
                    val activeReminders = db.reminderDao().getEnabled()
                    activeReminders.forEach { reminder ->
                        scheduler.schedule(reminder)
                    }

                    // 2. Lập lịch lại thông báo nhắc hạn định kỳ (Expiry Reminder Settings)
                    val settings = settingsRepository.settings.first()
                    WorkManagerHelper.schedulePeriodicReminder(context, settings)

                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling reminders", e)
                }
            }
        }
    }
}
