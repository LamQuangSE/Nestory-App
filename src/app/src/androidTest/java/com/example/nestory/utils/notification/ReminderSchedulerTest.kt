package com.example.nestory.utils.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.entity.ReminderEntity
import com.example.nestory.receiver.ReminderReceiver
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ReminderSchedulerTest {
    private lateinit var context: Context
    private lateinit var scheduler: ReminderScheduler

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        scheduler = ReminderScheduler(context)
    }

    private fun hasPendingAlarm(reminderId: Long): Boolean =
        PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) != null

    @Test
    fun schedule_enabledFutureReminder_setsAlarm() {
        val reminder = ReminderEntity(
            id = 1,
            documentId = 100L,
            isEnabled = true,
            reminderDate = "01/01/2099",
            reminderTime = "09:00",
        )

        scheduler.schedule(reminder)

        assertNotNull(hasPendingAlarm(reminder.id))
    }

    @Test
    fun schedule_disabledReminder_doesNotSetAlarm() {
        val reminder = ReminderEntity(
            id = 2,
            documentId = 100L,
            isEnabled = false,
            reminderDate = "01/01/2099",
            reminderTime = "09:00",
        )

        scheduler.schedule(reminder)

        assertNull(hasPendingAlarm(reminder.id))
    }

    @Test
    fun schedule_missingDate_doesNotSetAlarm() {
        val reminder = ReminderEntity(
            id = 3,
            documentId = 100L,
            isEnabled = true,
            reminderDate = null,
            reminderTime = "09:00",
        )

        scheduler.schedule(reminder)

        assertNull(hasPendingAlarm(reminder.id))
    }

    @Test
    fun schedule_missingTime_doesNotSetAlarm() {
        val reminder = ReminderEntity(
            id = 4,
            documentId = 100L,
            isEnabled = true,
            reminderDate = "01/01/2099",
            reminderTime = null,
        )

        scheduler.schedule(reminder)

        assertNull(hasPendingAlarm(reminder.id))
    }

    @Test
    fun schedule_pastReminder_doesNotSetAlarm() {
        val reminder = ReminderEntity(
            id = 5,
            documentId = 100L,
            isEnabled = true,
            reminderDate = "01/01/2020",
            reminderTime = "09:00",
        )

        scheduler.schedule(reminder)

        assertNull(hasPendingAlarm(reminder.id))
    }

    @Test
    fun cancel_afterSchedule_removesAlarm() {
        val reminder = ReminderEntity(
            id = 6,
            documentId = 100L,
            isEnabled = true,
            reminderDate = "01/01/2099",
            reminderTime = "09:00",
        )

        scheduler.schedule(reminder)
        assertNotNull(hasPendingAlarm(reminder.id))

        scheduler.cancel(reminder)

        assertNull(hasPendingAlarm(reminder.id))
    }
}
