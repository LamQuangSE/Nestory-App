package com.example.nestory.ui.screen.document

import com.example.nestory.domain.model.ExpiryReminderSettings
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DocumentStatusCalculator {
    private val supportedDateFormats = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ISO_LOCAL_DATE,
    )

    fun calculate(
        expirationDate: String?,
        settings: ExpiryReminderSettings,
        today: LocalDate = LocalDate.now()
    ): DocumentStatus {
        val expiryDate = parseExpirationDate(expirationDate) ?: return DocumentStatus.Active

        if (expiryDate.isBefore(today)) {
            return DocumentStatus.Expired
        }

        val reminderLimit = today.plusDays(settings.leadTimeDays.toLong())
        return if (settings.enabled && !expiryDate.isAfter(reminderLimit)) {
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