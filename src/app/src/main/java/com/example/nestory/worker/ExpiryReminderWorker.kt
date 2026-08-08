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
            val status = calculateStatus(doc.expirationDate, settings.leadTimeDays, today)
            
            // 1. Dạng 1: Mỗi trạng thái thông báo 1 lần
            if (status != DocumentStatus.Active) {
                val statusString = status.name
                // Nếu trạng thái hiện tại khác với trạng thái đã thông báo lần cuối
                // (VD: Đang là null -> báo ExpiringSoon; Đang là ExpiringSoon -> báo Expired)
                if (doc.lastNotifiedStatus != statusString) {
                    val label = if (status == DocumentStatus.Expired) "đã hết hạn" else "sắp hết hạn"
                    notificationHelper.showExpiryNotification(
                        doc.id.toInt(),
                        "Cập nhật trạng thái giấy tờ",
                        "Giấy tờ \"${doc.title}\" $label"
                    )
                    // Cập nhật trạng thái hiện tại vào DB để không báo lại cho trạng thái NÀY nữa
                    documentDao.update(doc.copy(lastNotifiedStatus = statusString))
                }
            } else if (doc.lastNotifiedStatus != null) {
                // Nếu quay lại trạng thái Active (gia hạn), xóa flag để sau này nếu sắp hết hạn lại thì vẫn báo
                documentDao.update(doc.copy(lastNotifiedStatus = null))
            }

            // 2. Dạng 2: Thông báo nhắc lại mỗi ngày (Daily Reminder)
            // Nếu người dùng bật repeatDaily, thì thông báo mỗi ngày cho các giấy tờ không phải Active
            if (status != DocumentStatus.Active && settings.repeatDaily) {
                notificationHelper.showExpiryNotification(
                    doc.id.toInt() + 1000000, // Offset ID
                    "Nhắc nhở hạn giấy tờ",
                    "Giấy tờ \"${doc.title}\" đang trong trạng thái ${if (status == DocumentStatus.Expired) "hết hạn" else "sắp hết hạn"}"
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
                        "Nhắc nhở cho giấy tờ \"${doc.title}\""
                    )
                }
            }
        }

        return Result.success()
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
