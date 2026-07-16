package com.example.nestory.data.repository

import com.example.nestory.data.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeAllDocuments(): Flow<List<DocumentEntity>>

    fun getDocumentById(documentId: Long): Flow<DocumentEntity?>

    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    fun filterDocuments(filter: DocumentFilter): Flow<List<DocumentEntity>>

    suspend fun createDocument(document: DocumentEntity): Result<Long>

    suspend fun updateDocument(document: DocumentEntity): Result<Unit>

    suspend fun deleteDocument(document: DocumentEntity): Result<Unit>

    suspend fun updateFavoriteStatus(documentId: Long, isFavorite: Boolean): Result<Unit>

    suspend fun updateDocumentLocation(documentId: Long, containerId: Long): Result<Unit>

    suspend fun updateDocumentExpiryDate(documentId: Long, expirationDate: String?): Result<Unit>
}
