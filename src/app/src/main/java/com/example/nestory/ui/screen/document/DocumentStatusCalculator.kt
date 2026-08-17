package com.example.nestory.ui.screen.document

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DocumentStatusCalculator {

    /** Logic ngầm: giấy tờ tự chuyển sang "sắp hết hạn" 60 ngày trước hạn. */
    const val EXPIRING_SOON_LEAD_DAYS = 60L

    private val supportedDateFormats = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ISO_LOCAL_DATE,
    )

    fun calculate(
        expirationDate: String?,
        today: LocalDate = LocalDate.now()
    ): DocumentStatus {
        val expiryDate = parseExpirationDate(expirationDate) ?: return DocumentStatus.Active

        if (expiryDate.isBefore(today)) {
            return DocumentStatus.Expired
        }

        val reminderLimit = today.plusDays(EXPIRING_SOON_LEAD_DAYS)
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