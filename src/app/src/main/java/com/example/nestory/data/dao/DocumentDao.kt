package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.nestory.data.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `documents` table.
 * Provides CRUD operations and queries for document entities.
 */
@Dao
interface DocumentDao {
    /** Insert a new document; returns the new row ID. Conflict strategy ABORT. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(document: DocumentEntity): Long

    /** Update an existing document (matched by primary key). */
    @Update
    suspend fun update(document: DocumentEntity)

    /** Delete a document by its primary key. */
    @Delete
    suspend fun delete(document: DocumentEntity)

    /** Retrieve all documents ordered by title (ascending). */
    @Query("SELECT * FROM documents ORDER BY title ASC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    /** Retrieve a single document by its primary key. */
    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    fun getById(documentId: Long): Flow<DocumentEntity?>

    /** Retrieve all documents belonging to a specific container. */
    @Query("SELECT * FROM documents WHERE container_id = :containerId")
    fun getDocumentsByContainer(containerId: Long): Flow<List<DocumentEntity>>
}
