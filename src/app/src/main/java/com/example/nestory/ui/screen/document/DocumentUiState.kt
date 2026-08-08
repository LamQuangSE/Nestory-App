package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color

data class DocumentUiState(
    val documents: List<DocumentUiModel> = emptyList(),
    val selectedDocument: DocumentUiModel? = null,
    val searchQuery: String = "",
    val isFilterActive: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class DocumentUiModel(
    val id: String,
    val name: String,
    val category: String,
    val containerPath: String,
    val containerId: Long,
    val status: DocumentStatus,
    val expiryDate: String,
    val categoryColor: Color,
    val attachmentUris: List<String> = emptyList(),
    val customReminder: CustomReminderUiModel? = null
)

data class CustomReminderUiModel(
    val id: Long = 0,
    val date: String,
    val time: String,
    val isEnabled: Boolean = true
)

enum class DocumentStatus {
    Active,
    ExpiringSoon,
    Expired
}
