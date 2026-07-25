package com.example.nestory.data.database.converter

import androidx.room.TypeConverter
import com.example.nestory.data.model.DocumentCategory

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