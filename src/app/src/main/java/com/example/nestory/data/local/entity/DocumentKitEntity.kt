package com.example.nestory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_kits")
data class DocumentKitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val category: String?,

    val description: String?,

    val note: String?,

    @ColumnInfo(name = "target_completion_date")
    val targetCompletionDate: String,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
)
