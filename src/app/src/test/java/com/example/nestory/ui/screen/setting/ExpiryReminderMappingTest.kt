package com.example.nestory.ui.screen.setting

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpiryReminderMappingTest {

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @Test
    fun toReminderEntity_whenAlreadyInsideLeadWindow_schedulesToday() {
        val today = LocalDate.now()
        val entity = ExpiryReminderUiState(
            leadTimeDays = 7,
            hour = 19,
            minute = 15,
        ).toReminderEntity(
            documentId = 1L,
            expiryDate = today.plusDays(1).format(formatter),
        )

        assertEquals(today.format(formatter), entity.reminderDate)
        assertEquals("19:15", entity.reminderTime)
        assertEquals(7, entity.leadTimeDays)
        assertEquals(false, entity.customLeadTimeMode)
    }

    @Test
    fun toReminderEntity_whenLeadDateIsFuture_schedulesConfiguredLeadDate() {
        val today = LocalDate.now()
        val entity = ExpiryReminderUiState(
            leadTimeDays = 7,
            hour = 9,
            minute = 5,
        ).toReminderEntity(
            documentId = 1L,
            expiryDate = today.plusDays(10).format(formatter),
        )

        assertEquals(today.plusDays(3).format(formatter), entity.reminderDate)
        assertEquals("09:05", entity.reminderTime)
    }

    @Test
    fun toExpiryReminderUiState_restoresSavedConfigFields() {
        val state = ExpiryReminderUiState(
            leadTimeDays = 14,
            customLeadTimeMode = true,
            repeatDaily = false,
            inAppEnabled = false,
            pushEnabled = true,
            hour = 21,
            minute = 30,
        )
        val restored = state.toReminderEntity(
            documentId = 1L,
            expiryDate = LocalDate.now().plusDays(1).format(formatter),
        ).toExpiryReminderUiState(LocalDate.now().plusDays(1).format(formatter))

        assertEquals(state, restored)
    }

    @Test
    fun toExpiryReminderUiState_restoresCustomModeForNonStandardLeadTime() {
        val state = ExpiryReminderUiState(
            leadTimeDays = 20,
            customLeadTimeMode = true,
            hour = 19,
            minute = 43,
        )
        val restored = state.toReminderEntity(
            documentId = 1L,
            expiryDate = LocalDate.now().plusDays(30).format(formatter),
        ).toExpiryReminderUiState(LocalDate.now().plusDays(30).format(formatter))

        assertEquals(state, restored)
    }
}
