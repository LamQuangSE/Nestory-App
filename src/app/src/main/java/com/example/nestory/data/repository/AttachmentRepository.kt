package com.example.nestory.data.repository

import com.example.nestory.data.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun getAttachmentsByDocumentId(documentId: Long): Flow<List<AttachmentEntity>>

    suspend fun addAttachmentMetadata(attachment: AttachmentEntity): Result<Long>

    suspend fun updateAttachmentMetadata(attachment: AttachmentEntity): Result<Unit>

    suspend fun deleteAttachmentMetadata(attachment: AttachmentEntity): Result<Unit>
}
