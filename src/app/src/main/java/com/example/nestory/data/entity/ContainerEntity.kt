package com.example.nestory.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a container (folder) that can hold documents.
 * A container can nest inside another container via parent_id.
 */
@Entity(
    tableName = "containers",
    // Define a self-referencing foreign key for hierarchical containers
    foreignKeys = [
        ForeignKey(
            entity = ContainerEntity::class, // points to same table
            parentColumns = ["id"],          // primary key of parent container
            childColumns = ["parent_id"],    // foreign key column in this table
            onDelete = ForeignKey.RESTRICT,   // prevent deletion of container with children
            onUpdate = ForeignKey.NO_ACTION  // primary key never changes; no action needed on update
        )
    ],
    // Index on parent_id to speed up lookups of children
    indices = [Index(value = ["parent_id"])]
)
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true) // Auto‑generated row id
    val id: Long = 0,
    val name: String, // name of the folder/container
    @ColumnInfo(name = "parent_id") // column name in DB
    val parentId: Long? = null // null indicates a top‑level container
)