package com.example.nestory.ui.screen.documentkit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class KitUsageStatusTest {
    private val today = LocalDate.of(2026, 8, 17)

    @Test
    fun futureUsageDate_returnsUpcoming() {
        assertEquals(
            KitUsageStatus.Upcoming,
            resolveKitUsageStatus("18/08/2026", today),
        )
    }

    @Test
    fun pastUsageDate_returnsUsed() {
        assertEquals(
            KitUsageStatus.Used,
            resolveKitUsageStatus("16/08/2026", today),
        )
    }

    @Test
    fun todayUsageDate_returnsUpcoming() {
        assertEquals(
            KitUsageStatus.Upcoming,
            resolveKitUsageStatus("17/08/2026", today),
        )
    }

    @Test
    fun blankUsageDate_returnsNull() {
        assertNull(resolveKitUsageStatus(null, today))
        assertNull(resolveKitUsageStatus("", today))
        assertNull(resolveKitUsageStatus("   ", today))
    }

    @Test
    fun invalidUsageDate_returnsNull() {
        assertNull(resolveKitUsageStatus("not-a-date", today))
    }
}