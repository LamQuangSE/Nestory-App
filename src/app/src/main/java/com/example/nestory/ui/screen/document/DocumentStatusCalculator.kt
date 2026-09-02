package com.example.nestory.ui.screen.document

import com.example.nestory.data.local.entity.ReminderEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DocumentStatusCalculator {

    /** Nếu reminder chưa bật cho giấy tờ thì trạng thái sắp hết hạn dùng mốc mặc định 7 ngày. */
    const val DEFAULT_EXPIRING_SOON_LEAD_DAYS = 7L

    private val supportedDateFormats = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ISO_LOCAL_DATE,
    )

    fun calculate(
        expirationDate: String?,
        today: LocalDate = LocalDate.now(),
        reminder: ReminderEntity? = null,
    ): DocumentStatus {
        val expiryDate = parseExpirationDate(expirationDate) ?: return DocumentStatus.Active

        if (expiryDate.isBefore(today)) {
            return DocumentStatus.Expired
        }

        val leadDays = reminder
            ?.takeIf { it.isEnabled }
            ?.leadTimeDays
            ?.coerceAtLeast(1)
            ?.toLong()
            ?: DEFAULT_EXPIRING_SOON_LEAD_DAYS
        val reminderLimit = today.plusDays(leadDays)
        return if (!expiryDate.isAfter(reminderLimit)) {
            DocumentStatus.ExpiringSoon
        } else {
            DocumentStatus.Active
        }
    }

    fun parseExpirationDate(expirationDate: String?): LocalDate? {
        val value = expirationDate?.trim().orEmpty()
        if (value.isBlank()) return null

        return supportedDateFormats.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(value, formatter)
            } catch (e: DateTimeParseException) {
                null
            }
        }
    }
}
