package com.example.nestory.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.settings.ExpiryReminderSettingsRepository
import com.example.nestory.ui.screen.document.DocumentStatus
import com.example.nestory.utils.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpiryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val settingsRepository = ExpiryReminderSettingsRepository(context)
        val settings = settingsRepository.settings.first()

        if (!settings.enabled) return Result.success()

        val database = AppDatabase.getDatabase(context)
        val documentDao = database.documentDao()
        val reminderDao = database.reminderDao()
        val documents = documentDao.getAllDocuments()
        val customReminders = reminderDao.getEnabled()
        val notificationHelper = NotificationHelper(context)
        
        val now = Calendar.getInstance()
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val dateToday = sdfDate.format(now.time)
        val timeNow = sdfTime.format(now.time)
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        for (doc in documents) {
            val expiryDate = parseDate(doc.expirationDate)
            val daysDiff = if (expiryDate != null) {
                ((expiryDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
            } else null

            val status = calculateStatus(doc.expirationDate, settings.leadTimeDays, today)
            
            // 1. Dạng 1: Thông báo CHUYỂN TRẠNG THÁI (Báo 1 lần khi đổi sang sắp hết hạn/hết hạn)
            if (status != DocumentStatus.Active) {
                val statusString = status.name
                if (doc.lastNotifiedStatus != statusString) {
                    val label = if (status == DocumentStatus.Expired) "đã hết hạn" else "sắp hết hạn"
                    notificationHelper.showExpiryNotification(
                        doc.id.toInt(),
                        "Cập nhật trạng thái",
                        "Giấy tờ \"${doc.title}\" $label",
                        doc.id
                    )
                    documentDao.update(doc.copy(lastNotifiedStatus = statusString))
                }
            } else if (doc.lastNotifiedStatus != null) {
                documentDao.update(doc.copy(lastNotifiedStatus = null))
            }

            // 2. Dạng 2: Thông báo NHẮC NHỞ HÀNG NGÀY (Nội dung chi tiết số ngày)
            if (status != DocumentStatus.Active && settings.repeatDaily) {
                val message = when {
                    status == DocumentStatus.Expired -> "Giấy tờ \"${doc.title}\" đã hết hạn"
                    daysDiff != null && daysDiff > 0 -> "Giấy tờ \"${doc.title}\" còn $daysDiff ngày nữa là hết hạn"
                    daysDiff == 0 -> "Giấy tờ \"${doc.title}\" sẽ hết hạn vào hôm nay"
                    else -> "Giấy tờ \"${doc.title}\" sắp hết hạn"
                }

                notificationHelper.showExpiryNotification(
                    doc.id.toInt() + 1000000,
                    "Nhắc nhở hạn giấy tờ",
                    message,
                    doc.id
                )
            }
        }

        // 3. Dạng 3: Thông báo nhắc hạn tùy chỉnh theo ngày/giờ người dùng đặt
        for (reminder in customReminders) {
            if (reminder.reminderDate == dateToday && (reminder.reminderTime == null || reminder.reminderTime == timeNow)) {
                val doc = reminder.documentId?.let { documentDao.getById(it) }
                if (doc != null) {
                    notificationHelper.showExpiryNotification(
                        reminder.id.toInt() + 2000000, // Offset ID khác
                        "Nhắc nhở tùy chỉnh",
                        "Nhắc nhở cho giấy tờ \"${doc.title}\"",
                        doc.id
                    )
                }
            }
        }

        return Result.success()
    }

    private fun parseDate(dateStr: String?): Date? {
        if (dateStr == null) return null
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateStatus(expiryDateStr: String?, leadTimeDays: Int, today: Date): DocumentStatus {
        if (expiryDateStr == null) return DocumentStatus.Active
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val expiryDate = sdf.parse(expiryDateStr) ?: return DocumentStatus.Active
            
            if (expiryDate.before(today)) {
                DocumentStatus.Expired
            } else {
                val calendar = Calendar.getInstance()
                calendar.time = today
                calendar.add(Calendar.DAY_OF_YEAR, leadTimeDays)
                val thresholdDate = calendar.time
                
                if (!expiryDate.after(thresholdDate)) {
                    DocumentStatus.ExpiringSoon
                } else {
                    DocumentStatus.Active
                }
            }
        } catch (e: Exception) {
            DocumentStatus.Active
        }
    }
}
