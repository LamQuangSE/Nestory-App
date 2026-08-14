package com.example.nestory.domain.model

import com.example.nestory.domain.model.DocumentCategory

/**
 * Draft object used to auto-fill the document form. Not saved yet.
 * Mapped from [OcrResult] and editable by the user before saving.
 */
data class DocumentDraft(
    val title: String = "",
    val category: DocumentCategory? = null,
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val documentNumber: String? = null,
    val holderName: String? = null,
    val notes: String? = null,
    val ocrText: String = "",
    val containerId: Long? = null,
    val attachmentId: Long? = null,
)

