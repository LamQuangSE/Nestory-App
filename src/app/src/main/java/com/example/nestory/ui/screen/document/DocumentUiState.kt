package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color

data class DocumentUiState(
    val documents: List<DocumentUiModel> = emptyList(),
    val searchQuery: String = "",
    val isFilterActive: Boolean = false
)

data class DocumentUiModel(
    val id: String,
    val name: String,
    val category: String,
    val containerPath: String,
    val status: DocumentStatus,
    val expiryDate: String,
    val categoryColor: Color
)

enum class DocumentStatus {
    Active,
    ExpiringSoon,
    Expired
}
