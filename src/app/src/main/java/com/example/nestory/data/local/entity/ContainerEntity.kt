package com.example.nestory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a container (folder) that can hold documents.
 * A container can nest inside another container via parent_id (null = top level).
 * Container names are unique among siblings that share the same parent.
 */
@Entity(
    tableName = "containers",
    foreignKeys = [
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["name", "parent_id"]),
        Index(value = ["parent_id"]),
    ],
)
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    @ColumnInfo(name = "parent_id")
    val parentId: Long? = null,
)
