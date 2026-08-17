package com.example.nestory.utils.ocr

import com.example.nestory.domain.model.DocumentDraft
import com.example.nestory.domain.model.OcrResult

/**
 * Maps an [OcrResult] into a [DocumentDraft] for the auto-fill form.
 * Pure mapping; does not save.
 */
class DocumentDraftMapper(
    private val categoryDetector: CategoryDetector,
) {

    fun toDraft(result: OcrResult): DocumentDraft {
        val categoryEnum = result.category ?: categoryDetector.detect(result)
        return DocumentDraft(
            title = "",
            category = categoryEnum,
            categoryName = categoryEnum?.toVietnameseLabel(),
            issueDate = result.issueDate,
            expiryDate = result.expiryDate,
            holderName = result.holderName,
            notes = null,
            ocrText = result.rawText,
            containerId = null,
            attachmentId = null,
        )
    }
}

