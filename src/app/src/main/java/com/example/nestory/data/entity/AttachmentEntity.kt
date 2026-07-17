package com.example.nestory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["document_id"]),
        Index(value = ["document_id", "display_order"], unique = true)
    ]
)
data class AttachmentEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "file_uri")
    val fileUri: String,

    @ColumnInfo(name = "document_id")
    val documentId: Long,

    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0
)