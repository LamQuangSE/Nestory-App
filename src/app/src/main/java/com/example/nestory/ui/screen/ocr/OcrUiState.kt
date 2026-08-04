package com.example.nestory.ui.screen.ocr

import com.example.nestory.domain.model.DocumentDraft

/**
 * Sealed UI state for the OCR flow. Never exposes raw String state.
 */
sealed interface OcrUiState {
    data object Idle : OcrUiState
    data object Processing : OcrUiState
    data class Success(val draft: DocumentDraft) : OcrUiState
    data class Error(val message: String) : OcrUiState
}


