package com.example.nestory.ui.screen.document

import com.example.nestory.data.local.entity.ReminderEntity
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
            today = today,
        )

        assertEquals(DocumentStatus.Expired, status)
    }

    @Test
    fun dateInsideLeadTime_returnsExpiringSoon() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "11/08/2026",
            today = today,
        )

        assertEquals(DocumentStatus.ExpiringSoon, status)
    }

    @Test
    fun dateOutsideLeadTime_returnsActive() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "20/10/2026",
            today = today,
        )

        assertEquals(DocumentStatus.Active, status)
    }

    @Test
    fun dateExactlySevenDaysAhead_returnsExpiringSoonByDefault() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "11/08/2026",
            today = today,
        )

        assertEquals(DocumentStatus.ExpiringSoon, status)
    }

    @Test
    fun enabledReminder_usesConfiguredLeadTime() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "24/08/2026",
            today = today,
            reminder = ReminderEntity(isEnabled = true, leadTimeDays = 20),
        )

        assertEquals(DocumentStatus.ExpiringSoon, status)
    }

    @Test
    fun disabledReminder_usesDefaultSevenDays() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "24/08/2026",
            today = today,
            reminder = ReminderEntity(isEnabled = false, leadTimeDays = 20),
        )

        assertEquals(DocumentStatus.Active, status)
    }

    @Test
    fun todayExpiration_returnsExpiringSoon() {
        val status = DocumentStatusCalculator.calculate(
            expirationDate = "04/08/2026",
            today = today,
        )

        assertEquals(DocumentStatus.ExpiringSoon, status)
    }

    @Test
    fun blankOrInvalidDate_returnsActive() {
        assertEquals(
            DocumentStatus.Active,
            DocumentStatusCalculator.calculate(null, today),
        )
        assertEquals(
            DocumentStatus.Active,
            DocumentStatusCalculator.calculate("not-a-date", today),
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
