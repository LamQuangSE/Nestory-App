package com.example.nestory.ui.screen.documentkit

import com.example.nestory.ui.screen.document.DocumentStatusCalculator
import java.time.LocalDate

enum class KitUsageStatus(val label: String) {
    Upcoming("Sắp sử dụng"),
    Used("Đã sử dụng"),
}

data class DocumentKitFilterState(
    val selectedCategory: String? = null,
    val isFavorite: Boolean? = null,
    val usageStatuses: Set<KitUsageStatus> = emptySet(),
) {
    val isActive: Boolean
        get() = selectedCategory != null || isFavorite != null || usageStatuses.isNotEmpty()
}

fun resolveKitUsageStatus(
    usageDate: String?,
    today: LocalDate = LocalDate.now(),
): KitUsageStatus? {
    val date = DocumentStatusCalculator.parseExpirationDate(usageDate)
        ?: return null
    return if (date.isBefore(today)) KitUsageStatus.Used else KitUsageStatus.Upcoming
}
