package com.example.nestory.data.repository

import com.example.nestory.data.dao.DocumentDao
import com.example.nestory.data.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

class DocumentRepositoryImpl(
    private val documentDao: DocumentDao,
) : DocumentRepository {
    override fun observeAllDocuments(): Flow<List<DocumentEntity>> =
        documentDao.observeAllDocuments()

    override fun observeDocumentById(documentId: Long): Flow<DocumentEntity?> =
        documentDao.observeById(documentId)

    override fun searchDocuments(query: String): Flow<List<DocumentEntity>> =
        documentDao.searchDocuments(query.trim())

    override fun filterDocuments(filter: DocumentFilter): Flow<List<DocumentEntity>> =
        documentDao.filterDocuments(
            category = filter.category,
            isFavorite = filter.isFavorite,
            containerId = filter.containerId
        )

    override suspend fun createDocument(document: DocumentEntity): Result<Long> =
        runCatching { documentDao.insert(document) }

    override suspend fun updateDocument(document: DocumentEntity): Result<Unit> =
        runCatching { documentDao.update(document) }

    override suspend fun deleteDocument(document: DocumentEntity): Result<Unit> =
        runCatching { documentDao.delete(document) }

    override suspend fun updateFavoriteStatus(
        documentId: Long,
        isFavorite: Boolean,
    ): Result<Unit> = runCatching {
        documentDao.updateFavoriteStatus(documentId, isFavorite)
    }

    override suspend fun updateDocumentLocation(
        documentId: Long,
        containerId: Long,
    ): Result<Unit> = runCatching {
        documentDao.updateDocumentLocation(documentId, containerId)
    }

    override suspend fun updateDocumentExpiryDate(
        documentId: Long,
        expirationDate: String?,
    ): Result<Unit> = runCatching {
        documentDao.updateDocumentExpiryDate(documentId, expirationDate)
    }
}
