package com.example.nestory.data.local.database.converter

import androidx.room.TypeConverter
import com.example.nestory.domain.model.DocumentCategory
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCategory(value: String?): DocumentCategory? {
        return value?.let { DocumentCategory.valueOf(it) }
    }

    @TypeConverter
    fun categoryToString(category: DocumentCategory?): String? {
        return category?.name
    }
}
