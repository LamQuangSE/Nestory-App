package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color

data class DocumentFilterState(
    val selectedCategoryId: String? = null,
    val selectedContainerId: Long? = null,
    val isFavorite: Boolean? = null,
    val statuses: Set<DocumentStatus> = emptySet()
) {
    val isActive: Boolean
        get() = selectedCategoryId != null || selectedContainerId != null || isFavorite != null || statuses.isNotEmpty()
}

data class ContainerUiModel(
    val id: Long,
    val name: String,
    val fullPath: String,
    val parentId: Long?,
    val hasChildren: Boolean,
    val childFolderCount: Int,
    val documentCount: Int
)

data class DocCategoryUiModel(
    val id: String,
    val name: String,
    val color: Color
)

data class DocumentUiState(
    val documents: List<DocumentUiModel> = emptyList(),
    val availableContainers: List<ContainerUiModel> = emptyList(),
    val availableCategories: List<DocCategoryUiModel> = emptyList(),
    val selectedDocument: DocumentUiModel? = null,
    val searchQuery: String = "",
    val activeFilter: DocumentFilterState = DocumentFilterState(),
    val draftFilter: DocumentFilterState = DocumentFilterState(),
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
    val isFavorite: Boolean = false,
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