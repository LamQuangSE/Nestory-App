package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    /** Retrieve all attachments ordered by file name (ascending). */
    @Query("SELECT * FROM attachments ORDER BY file_name ASC")
    suspend fun getAllAttachments(): List<AttachmentEntity>

    /** Retrieve a single attachment by its primary key. */
    @Query("SELECT * FROM attachments WHERE id = :attachmentId LIMIT 1")
    suspend fun getById(attachmentId: Long): AttachmentEntity?

    /** Retrieve all attachments belonging to a specific document. */
    @Query("SELECT * FROM attachments WHERE document_id = :documentId")
    suspend fun getByDocumentId(documentId: Long): List<AttachmentEntity>
}