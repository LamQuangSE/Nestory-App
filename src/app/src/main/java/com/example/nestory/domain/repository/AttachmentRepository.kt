package com.example.nestory.domain.repository

import com.example.nestory.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun observeAllAttachments(): Flow<List<AttachmentEntity>>

    fun observeAttachmentsByDocumentId(documentId: Long): Flow<List<AttachmentEntity>>

    suspend fun getAttachmentById(attachmentId: Long): Result<AttachmentEntity?>

    suspend fun getAttachmentsByDocumentId(documentId: Long): Result<List<AttachmentEntity>>

    suspend fun addAttachmentMetadata(attachment: AttachmentEntity): Result<Long>

    suspend fun updateAttachmentMetadata(attachment: AttachmentEntity): Result<Unit>

    suspend fun deleteAttachmentMetadata(attachment: AttachmentEntity): Result<Unit>
}
