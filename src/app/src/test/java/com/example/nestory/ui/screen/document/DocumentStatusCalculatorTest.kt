package com.example.nestory.ui.screen.document

import com.example.nestory.domain.model.ExpiryReminderSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DocumentStatusCalculatorTest {
    private val today = LocalDate.of(2026, 8, 4)

    @Test
    fun expiredDate_returnsExpired() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "03/08/2026",
            settings = ExpiryReminderSettings(),
            today = today,
        )

        assertEquals(DocumentStatus.Expired, status)
    }

    @Test
    fun dateInsideLeadTime_whenReminderEnabled_returnsExpiringSoon() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "11/08/2026",
            settings = ExpiryReminderSettings(enabled = true, leadTimeDays = 7),
            today = today,
        )

        assertEquals(DocumentStatus.ExpiringSoon, status)
    }

    @Test
    fun dateOutsideLeadTime_returnsActive() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "12/08/2026",
            settings = ExpiryReminderSettings(enabled = true, leadTimeDays = 7),
            today = today,
        )

        assertEquals(DocumentStatus.Active, status)
    }

    @Test
    fun dateInsideLeadTime_whenReminderDisabled_returnsActive() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "11/08/2026",
            settings = ExpiryReminderSettings(enabled = false, leadTimeDays = 7),
            today = today,
        )

        assertEquals(DocumentStatus.Active, status)
    }

    @Test
    fun todayExpiration_whenReminderEnabled_returnsExpiringSoon() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "04/08/2026",
            settings = ExpiryReminderSettings(enabled = true, leadTimeDays = 7),
            today = today,
        )

        assertEquals(DocumentStatus.ExpiringSoon, status)
    }

    @Test
    fun blankOrInvalidDate_returnsActive() {
        assertEquals(
            DocumentStatus.Active,
            DocumentStatusCalculator.calculate(null, ExpiryReminderSettings(), today),
        )
        assertEquals(
            DocumentStatus.Active,
            DocumentStatusCalculator.calculate("not-a-date", ExpiryReminderSettings(), today),
        )
    }

    @Test
    fun parseExpirationDate_supportsIsoDate() {
        assertEquals(
            LocalDate.of(2026, 8, 11),
            DocumentStatusCalculator.parseExpirationDate("2026-08-11"),
        )
        assertNull(DocumentStatusCalculator.parseExpirationDate(""))
    }
}
