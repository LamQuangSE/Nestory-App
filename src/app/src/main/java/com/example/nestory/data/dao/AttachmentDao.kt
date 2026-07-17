package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.nestory.data.entity.AttachmentEntity

/**
 * Data Access Object for the `attachments` table.
 * Provides CRUD operations and queries for attachment records linked to documents.
 */
@Dao
interface AttachmentDao {
    /** Insert a new attachment; returns the new row ID. Conflict strategy ABORT prevents duplicates. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(attachment: AttachmentEntity): Long

    /** Update an existing attachment row matching the primary key. */
    @Update
    suspend fun update(attachment: AttachmentEntity)

    /** Delete the attachment row matching the primary key. */
    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    /** Retrieve all attachments ordered by display order (ascending). */
    @Query("SELECT * FROM attachments ORDER BY display_order ASC")
    fun getAllAttachments(): Flow<List<AttachmentEntity>>

    /** Retrieve a single attachment by its primary key. */
    @Query("SELECT * FROM attachments WHERE id = :attachmentId LIMIT 1")
    fun getById(attachmentId: Long): Flow<AttachmentEntity?>

    /** Retrieve all attachments belonging to a specific document, ordered by display order. */
    @Query("SELECT * FROM attachments WHERE document_id = :documentId ORDER BY display_order ASC")
    fun getByDocumentId(documentId: Long): Flow<List<AttachmentEntity>>

    /** Update the display order of an attachment (for drag & drop reordering). */
    @Query("UPDATE attachments SET display_order = :newOrder WHERE id = :attachmentId")
    suspend fun updateDisplayOrder(attachmentId: Long, newOrder: Int)
}
