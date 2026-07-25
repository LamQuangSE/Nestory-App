package com.example.nestory.data.repository

import com.example.nestory.data.dao.AttachmentDao
import com.example.nestory.data.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

class AttachmentRepositoryImpl(
    private val attachmentDao: AttachmentDao,
) : AttachmentRepository {
    override fun observeAttachmentsByDocumentId(documentId: Long): Flow<List<AttachmentEntity>> =
        attachmentDao.observeByDocumentId(documentId)

    override suspend fun getAttachmentById(attachmentId: Long): Result<AttachmentEntity?> =
        runCatching { attachmentDao.getById(attachmentId) }

    override suspend fun getAttachmentsByDocumentId(
        documentId: Long,
    ): Result<List<AttachmentEntity>> =
        runCatching { attachmentDao.getByDocumentId(documentId) }

    override suspend fun addAttachmentMetadata(
        attachment: AttachmentEntity,
    ): Result<Long> = runCatching {
        attachmentDao.insert(attachment)
    }

    override suspend fun updateAttachmentMetadata(
        attachment: AttachmentEntity,
    ): Result<Unit> = runCatching {
        attachmentDao.update(attachment)
    }

    override suspend fun deleteAttachmentMetadata(
        attachment: AttachmentEntity,
    ): Result<Unit> = runCatching {
        attachmentDao.delete(attachment)
    }
}
