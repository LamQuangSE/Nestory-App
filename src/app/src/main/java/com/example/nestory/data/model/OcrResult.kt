package com.example.nestory.data.model

import com.example.nestory.data.model.DocumentCategory

/**
 * Business model produced by OCR + parsing. Not a Room entity.
 * Contains structured data extracted from raw OCR text.
 */
data class OcrResult(
    val documentName: String? = null,
    val category: DocumentCategory? = null,
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val documentNumber: String? = null,
    val holderName: String? = null,
    val rawText: String = "",
)
