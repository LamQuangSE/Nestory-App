package com.example.nestory.data.local.database.converter

import androidx.room.TypeConverter
import com.example.nestory.domain.model.DocumentCategory

class Converters {

    @TypeConverter
    fun fromCategory(category: DocumentCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): DocumentCategory {
        return DocumentCategory.valueOf(value)
    }
}