package com.example.nestory.utils.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.nestory.data.local.entity.ReminderEntity
import com.example.nestory.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: ReminderEntity) {
        if (!reminder.isEnabled || reminder.reminderDate.isNullOrBlank() || reminder.reminderTime.isNullOrBlank()) {
            cancel(reminder)
            return
        }

        val calendar = parseDateTime(reminder.reminderDate, reminder.reminderTime) ?: return
        
        // Don't schedule if time is in the past
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Log.d("ReminderScheduler", "Reminder time is in the past, not scheduling: ${reminder.id}")
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("ReminderScheduler", "Scheduled reminder ${reminder.id} for ${calendar.time}")
        } catch (e: SecurityException) {
            // Handle cases where SCHEDULE_EXACT_ALARM is not granted on Android 13+
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.e("ReminderScheduler", "SecurityException while scheduling exact alarm, using inexact fallback", e)
        }
    }

    fun cancel(reminder: ReminderEntity) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("ReminderScheduler", "Cancelled reminder ${reminder.id}")
        }
    }

    private fun parseDateTime(date: String, time: String): Calendar? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val parsedDate = sdf.parse("$date $time") ?: return null
            Calendar.getInstance().apply {
                this.time = parsedDate
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Error parsing date/time: $date $time", e)
            null
        }
    }
}
