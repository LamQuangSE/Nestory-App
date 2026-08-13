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
        val title = buildTitle(result)
        val categoryEnum = result.category ?: categoryDetector.detect(result)
        return DocumentDraft(
            title = title,
            category = categoryEnum,
            issueDate = result.issueDate,
            expiryDate = result.expiryDate,
            documentNumber = result.documentNumber,
            holderName = result.holderName,
            notes = null,
            containerId = null,
            attachmentId = null,
        )
    }

    private fun buildTitle(result: OcrResult): String {
        val name = result.documentName?.trim().orEmpty()
        return when {
            name.isNotBlank() -> name
            !result.documentNumber.isNullOrBlank() -> "Giấy tờ số ${result.documentNumber}"
            else -> "Giấy tờ"
        }
    }
}


