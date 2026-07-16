package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nestory.data.entity.DocumentEntity
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
    suspend fun getAllDocuments(): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    suspend fun getById(documentId: Long): DocumentEntity?

    @Query("SELECT * FROM documents ORDER BY title ASC")
    fun observeAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    fun observeById(documentId: Long): Flow<DocumentEntity?>

    @Query(
        """
        SELECT * FROM documents
        WHERE title LIKE '%' || :query || '%'
            OR category LIKE '%' || :query || '%'
            OR notes LIKE '%' || :query || '%'
        ORDER BY title ASC
        """
    )
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Query(
        """
        SELECT * FROM documents
        WHERE (:category IS NULL OR category = :category)
            AND (:isFavorite IS NULL OR is_favorite = :isFavorite)
            AND (:containerId IS NULL OR container_id = :containerId)
            AND (:expiresBefore IS NULL OR expiration_date <= :expiresBefore)
            AND (:expiresAfter IS NULL OR expiration_date >= :expiresAfter)
        ORDER BY title ASC
        """
    )
    fun filterDocuments(
        category: String?,
        isFavorite: Boolean?,
        containerId: Long?,
        expiresBefore: String?,
        expiresAfter: String?,
    ): Flow<List<DocumentEntity>>

    @Query("UPDATE documents SET is_favorite = :isFavorite WHERE id = :documentId")
    suspend fun updateFavoriteStatus(documentId: Long, isFavorite: Boolean)

    @Query("UPDATE documents SET container_id = :containerId WHERE id = :documentId")
    suspend fun updateDocumentLocation(documentId: Long, containerId: Long)

    @Query("UPDATE documents SET expiration_date = :expirationDate WHERE id = :documentId")
    suspend fun updateDocumentExpiryDate(documentId: Long, expirationDate: String?)
}
