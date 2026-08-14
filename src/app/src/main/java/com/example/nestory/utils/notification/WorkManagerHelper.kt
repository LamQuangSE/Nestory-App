package com.example.nestory.utils.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nestory.domain.model.ExpiryReminderSettings
import com.example.nestory.worker.ExpiryReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    private const val PERIODIC_WORK_NAME = "expiry_reminder_periodic"
    private const val IMMEDIATE_WORK_NAME = "expiry_reminder_immediate"

    /**
     * Lập lịch chạy định kỳ hàng ngày vào giờ đã chọn.
     */
    fun schedulePeriodicReminder(context: Context, settings: ExpiryReminderSettings) {
        val workManager = WorkManager.getInstance(context)
        
        if (!settings.enabled) {
            workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
            return
        }

        val currentTime = Calendar.getInstance()
        val scheduleTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.hour)
            set(Calendar.MINUTE, settings.minute)
            set(Calendar.SECOND, 0)
        }

        if (scheduleTime.before(currentTime)) {
            scheduleTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = scheduleTime.timeInMillis - currentTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<ExpiryReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Chạy Worker ngay lập tức (thường dùng khi có thay đổi dữ liệu/ngày tháng).
     */
    fun runImmediateCheck(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<ExpiryReminderWorker>()
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
