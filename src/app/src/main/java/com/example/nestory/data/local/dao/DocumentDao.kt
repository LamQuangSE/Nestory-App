package com.example.nestory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.domain.model.DocumentCategory
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
    fun observeAllDocuments(): Flow<List<DocumentEntity>>

    /** Retrieve all documents once, ordered by title (ascending). */
    @Query("SELECT * FROM documents ORDER BY title ASC")
    suspend fun getAllDocuments(): List<DocumentEntity>

    /** Retrieve a single document by its primary key. */
    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    fun observeById(documentId: Long): Flow<DocumentEntity?>

    /** Retrieve a single document once by its primary key. */
    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    suspend fun getById(documentId: Long): DocumentEntity?

    /** Retrieve all documents belonging to a specific container. */
    @Query("SELECT * FROM documents WHERE container_id = :containerId")
    fun observeDocumentsByContainer(containerId: Long): Flow<List<DocumentEntity>>

    /** Retrieve all documents belonging to a specific container once. */
    @Query("SELECT * FROM documents WHERE container_id = :containerId")
    suspend fun getDocumentsByContainer(containerId: Long): List<DocumentEntity>

    @Query("UPDATE documents SET is_favorite = :isFavorite WHERE id = :documentId")
    suspend fun updateFavoriteStatus(documentId: Long, isFavorite: Boolean)

    @Query("UPDATE documents SET container_id = :containerId WHERE id = :documentId")
    suspend fun updateDocumentLocation(documentId: Long, containerId: Long)

    @Query("UPDATE documents SET expiration_date = :expirationDate WHERE id = :documentId")
    suspend fun updateDocumentExpiryDate(documentId: Long, expirationDate: String?)

    @Query(
        """
        SELECT * FROM documents
        WHERE title LIKE '%' || :keyword || '%' COLLATE NOCASE
        ORDER BY title
        """
    )
    fun searchDocuments(keyword: String): Flow<List<DocumentEntity>>

    @Query(
        """
        SELECT * FROM documents
        WHERE (:category IS NULL OR category = :category)
            AND (:isFavorite IS NULL OR is_favorite = :isFavorite)
            AND (:containerId IS NULL OR container_id = :containerId)
        ORDER BY title COLLATE NOCASE
        """
    )
    fun filterDocuments(
        category: String?,
        isFavorite: Boolean?,
        containerId: Long?,
    ): Flow<List<DocumentEntity>>
}
