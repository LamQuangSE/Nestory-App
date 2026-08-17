package com.example.nestory.data.repository

import com.example.nestory.data.local.dao.DocumentDao
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.domain.model.DocumentFilter
import com.example.nestory.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class DocumentRepositoryImpl(
    private val documentDao: DocumentDao,
) : DocumentRepository {
    override fun observeAllDocuments(): Flow<List<DocumentEntity>> =
        documentDao.observeAllDocuments()

    override suspend fun getAllDocuments(): Result<List<DocumentEntity>> =
        runCatching { documentDao.getAllDocuments() }

    override fun observeDocumentById(documentId: Long): Flow<DocumentEntity?> =
        documentDao.observeById(documentId)

    override suspend fun getDocumentById(documentId: Long): Result<DocumentEntity?> =
        runCatching { documentDao.getById(documentId) }

    override fun observeDocumentsByContainer(containerId: Long): Flow<List<DocumentEntity>> =
        documentDao.observeDocumentsByContainer(containerId)

    override suspend fun getDocumentsByContainer(containerId: Long): Result<List<DocumentEntity>> =
        runCatching { documentDao.getDocumentsByContainer(containerId) }

    override fun searchDocuments(query: String): Flow<List<DocumentEntity>> =
        documentDao.searchDocuments(query.trim())

    override fun filterDocuments(filter: DocumentFilter): Flow<List<DocumentEntity>> =
        documentDao.filterDocuments(
            categoryId = filter.categoryId,
            isFavorite = filter.isFavorite,
            containerId = filter.containerId,
        )

    override suspend fun createDocument(document: DocumentEntity): Result<Long> =
        runCatching {
            if (document.title.isNotBlank() && documentDao.countByTitle(document.title, 0) > 0) {
                throw IllegalArgumentException("Tên giấy tờ đã tồn tại")
            }
            documentDao.insert(document)
        }

    override suspend fun updateDocument(document: DocumentEntity): Result<Unit> =
        runCatching {
            if (document.title.isNotBlank() && documentDao.countByTitle(document.title, document.id) > 0) {
                throw IllegalArgumentException("Tên giấy tờ đã tồn tại")
            }
            documentDao.update(document)
        }

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

    override suspend fun updateLastOpenedAt(
        documentId: Long,
        timestamp: Long,
    ): Result<Unit> = runCatching {
        documentDao.updateLastOpenedAt(documentId, timestamp)
    }
}
