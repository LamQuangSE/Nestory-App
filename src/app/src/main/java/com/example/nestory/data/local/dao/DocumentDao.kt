package com.example.nestory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nestory.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(document: DocumentEntity): Long

    @Update
    suspend fun update(document: DocumentEntity)

    @Delete
    suspend fun delete(document: DocumentEntity)

    @Query("SELECT * FROM documents ORDER BY title ASC")
    fun observeAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY title ASC")
    suspend fun getAllDocuments(): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    fun observeById(documentId: Long): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    suspend fun getById(documentId: Long): DocumentEntity?

    @Query("SELECT * FROM documents WHERE container_id = :containerId")
    fun observeDocumentsByContainer(containerId: Long): Flow<List<DocumentEntity>>

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
        WHERE (:categoryId IS NULL OR category_id = :categoryId)
            AND (:isFavorite IS NULL OR is_favorite = :isFavorite)
            AND (:containerId IS NULL OR container_id = :containerId)
        ORDER BY title COLLATE NOCASE
        """
    )
    fun filterDocuments(
        categoryId: String?,
        isFavorite: Boolean?,
        containerId: Long?,
    ): Flow<List<DocumentEntity>>

    @Query("UPDATE documents SET last_opened_at = :timestamp WHERE id = :documentId")
    suspend fun updateLastOpenedAt(documentId: Long, timestamp: Long)

    @Query(
        """
        SELECT COUNT(*) FROM documents
        WHERE title = :title COLLATE NOCASE AND id != :excludeId
        """
    )
    suspend fun countByTitle(title: String, excludeId: Long): Int
}