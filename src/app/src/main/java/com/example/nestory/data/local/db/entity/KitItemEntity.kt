package com.example.nestory.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kit_items",
    foreignKeys = [
        ForeignKey(
            entity = DocumentKitEntity::class,
            parentColumns = ["id"],
            childColumns = ["document_kit_id"],
            onDelete = ForeignKey.CASCADE
        )
        // Khi tích hợp với bảng documents của SCRUM-92, hãy mở comment đoạn này:
        /*
        ,ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["linked_document_id"],
            onDelete = ForeignKey.SET_NULL
        )
        */
    ],
    indices = [Index(value = ["document_kit_id"]), Index(value = ["linked_document_id"])]
)
data class KitItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val status: String, // "READY", "MISSING", "EXPIRED", "NEEDS_REVIEW"
    
    @ColumnInfo(name = "document_kit_id")
    val documentKitId: Long,
    
    @ColumnInfo(name = "linked_document_id")
    val linkedDocumentId: Long? 
)