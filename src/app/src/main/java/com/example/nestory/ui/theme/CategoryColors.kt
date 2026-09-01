package com.example.nestory.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.nestory.domain.model.DocumentCategory

val CategoryFallbackColor: Color = Color(0xFF717171)

val CategoryIdentityColor: Color = Color(0xFFFCA5A5)
val CategoryEducationColor: Color = Color(0xFFFDBA74)
val CategoryFinanceColor: Color = Color(0xFFFDE68A)
val CategoryPropertyColor: Color = Color(0xFFBEF264)
val CategoryVehicleColor: Color = Color(0xFF86EFAC)
val CategoryHealthColor: Color = Color(0xFF6EE7B7)

val DocumentCategory.categoryColor: Color
    get() = when (this) {
        DocumentCategory.IDENTITY -> CategoryIdentityColor
        DocumentCategory.EDUCATION -> CategoryEducationColor
        DocumentCategory.FINANCE -> CategoryFinanceColor
        DocumentCategory.PROPERTY -> CategoryPropertyColor
        DocumentCategory.VEHICLE -> CategoryVehicleColor
        DocumentCategory.HEALTH -> CategoryHealthColor
    }

fun predefinedCategoryColor(name: String?): Color? {
    val trimmed = name?.trim()
    if (trimmed.isNullOrEmpty()) return null
    return DocumentCategory.entries
        .firstOrNull { it.toVietnameseLabel() == trimmed }
        ?.categoryColor
}

/**
 * A Category is predefined (system-defined) when its name is one of the six
 * default Categories. This is the single source of truth used to lock those
 * Categories against Edit/Delete, regardless of whether the row is rendered as a
 * transient preset or a materialized row in the database.
 */
fun isPredefinedCategoryName(name: String?): Boolean = predefinedCategoryColor(name) != null