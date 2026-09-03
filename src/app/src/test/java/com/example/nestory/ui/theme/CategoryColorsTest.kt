package com.example.nestory.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.ui.screen.category.defaultCategoryColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryColorsTest {

    @Test
    fun predefinedCategoryColor_usesExactFixedColorPerCategory() {
        assertEquals(Color(0xFFFCA5A5), predefinedCategoryColor("Danh tính"))
        assertEquals(Color(0xFFFDBA74), predefinedCategoryColor("Học vấn"))
        assertEquals(Color(0xFFFDE68A), predefinedCategoryColor("Tài chính"))
        assertEquals(Color(0xFFBEF264), predefinedCategoryColor("Tài sản"))
        assertEquals(Color(0xFF86EFAC), predefinedCategoryColor("Phương tiện"))
        assertEquals(Color(0xFF6EE7B7), predefinedCategoryColor("Sức khỏe"))
    }

    @Test
    fun predefinedCategoryColor_isCaseInsensitiveOnWhitespace() {
        assertEquals(Color(0xFFFCA5A5), predefinedCategoryColor("  Danh tính "))
    }

    @Test
    fun predefinedCategoryColor_returnsNullForUnknownCategory() {
        assertNull(predefinedCategoryColor("Bảo hiểm"))
        assertNull(predefinedCategoryColor(null))
        assertNull(predefinedCategoryColor(""))
    }

    @Test
    fun documentCategory_enumColor_matchesSpecifiedPalette() {
        assertEquals(Color(0xFFFCA5A5), DocumentCategory.IDENTITY.categoryColor)
        assertEquals(Color(0xFFFDBA74), DocumentCategory.EDUCATION.categoryColor)
        assertEquals(Color(0xFFFDE68A), DocumentCategory.FINANCE.categoryColor)
        assertEquals(Color(0xFFBEF264), DocumentCategory.PROPERTY.categoryColor)
        assertEquals(Color(0xFF86EFAC), DocumentCategory.VEHICLE.categoryColor)
        assertEquals(Color(0xFF6EE7B7), DocumentCategory.HEALTH.categoryColor)
    }

    @Test
    fun defaultCategoryColors_keepPredefinedColorsAsSourceOfTruth() {
        val palette = defaultCategoryColors()
        assertEquals(CategoryIdentityColor, palette[0])
        assertEquals(CategoryEducationColor, palette[1])
        assertEquals(CategoryFinanceColor, palette[2])
        assertEquals(CategoryPropertyColor, palette[3])
        assertEquals(CategoryVehicleColor, palette[4])
        assertEquals(CategoryHealthColor, palette[5])
    }

    @Test
    fun isPredefinedCategoryName_protectsAllSixDefaultCategories() {
        assertTrue(isPredefinedCategoryName("Danh tính"))
        assertTrue(isPredefinedCategoryName("Học vấn"))
        assertTrue(isPredefinedCategoryName("Tài chính"))
        assertTrue(isPredefinedCategoryName("Tài sản"))
        assertTrue(isPredefinedCategoryName("Phương tiện"))
        assertTrue(isPredefinedCategoryName("Sức khỏe"))
        assertTrue(isPredefinedCategoryName("  Danh tính "))
    }

    @Test
    fun isPredefinedCategoryName_doesNotProtectUserCategories() {
        assertFalse(isPredefinedCategoryName("Bảo hiểm"))
        assertFalse(isPredefinedCategoryName("Hợp đồng"))
        assertFalse(isPredefinedCategoryName(null))
        assertFalse(isPredefinedCategoryName(""))
    }
}
