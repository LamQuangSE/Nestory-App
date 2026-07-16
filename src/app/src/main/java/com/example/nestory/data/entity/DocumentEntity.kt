package com.example.nestory.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a document stored in the database.
 * Each document belongs to a container (folder) and can have optional metadata.
 */
@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["container_id"],
            onDelete = ForeignKey.CASCADE,   // Delete documents when their container is removed
            onUpdate = ForeignKey.NO_ACTION  // Container ID (PK) never changes; keep as NO_ACTION
        )
    ],
    indices = [Index(value = ["container_id"])] // Speed up lookups by container
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,                     // Title of the document

    @ColumnInfo(name = "category")
    val category: String,                  // User‑defined category (e.g., "Invoice", "Photo")

    @ColumnInfo(name = "expiration_date")
    val expirationDate: String? = null,    // Optional expiration date (ISO‑8601 string)

    @ColumnInfo(name = "notes")
    val notes: String? = null,             // Free‑form notes

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,       // Starred flag

    @ColumnInfo(name = "container_id")
    val containerId: Long                  // FK to ContainerEntity.id
)