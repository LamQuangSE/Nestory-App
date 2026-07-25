package com.example.nestory.data.repository

import com.example.nestory.data.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeAllDocuments(): Flow<List<DocumentEntity>>

    suspend fun getAllDocuments(): Result<List<DocumentEntity>>

    fun observeDocumentById(documentId: Long): Flow<DocumentEntity?>

    suspend fun getDocumentById(documentId: Long): Result<DocumentEntity?>

    fun observeDocumentsByContainer(containerId: Long): Flow<List<DocumentEntity>>

    suspend fun getDocumentsByContainer(containerId: Long): Result<List<DocumentEntity>>

    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    fun filterDocuments(filter: DocumentFilter): Flow<List<DocumentEntity>>

    suspend fun createDocument(document: DocumentEntity): Result<Long>

    suspend fun updateDocument(document: DocumentEntity): Result<Unit>

    suspend fun deleteDocument(document: DocumentEntity): Result<Unit>

    suspend fun updateFavoriteStatus(documentId: Long, isFavorite: Boolean): Result<Unit>

    suspend fun updateDocumentLocation(documentId: Long, containerId: Long): Result<Unit>

    suspend fun updateDocumentExpiryDate(documentId: Long, expirationDate: String?): Result<Unit>
}
