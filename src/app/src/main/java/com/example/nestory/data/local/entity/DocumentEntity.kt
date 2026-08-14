package com.example.nestory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["container_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        // Tuỳ chọn: Bạn có thể thêm ForeignKey cho Category nếu muốn chặt chẽ hơn
    ],
    indices = [Index(value = ["container_id"])],
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "category_id")
    val categoryId: String,

    @ColumnInfo(name = "expiration_date")
    val expirationDate: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "issue_date")
    val issueDate: String? = null,

    @ColumnInfo(name = "holder_name")
    val holderName: String? = null,

    @ColumnInfo(name = "document_number")
    val documentNumber: String? = null,

    @ColumnInfo(name = "ocr_text")
    val ocrText: String? = null,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "container_id")
    val containerId: Long,

    @ColumnInfo(name = "last_notified_status")
    val lastNotifiedStatus: String? = null,
)
