package com.example.nestory.ui.screen.home

data class HomeDashboardUiState(
    val recentDocuments: List<RecentDocumentUi> = emptyList(),
    val attentionDocuments: List<RecentDocumentUi> = emptyList(),
    val documentKits: List<HomeDocumentKitUi> = emptyList(),
    val containers: List<HomeContainerUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class RecentDocumentUi(
    val id: Long,
    val title: String,
    val categoryLabel: String,
    val expiryDate: String,
    val attachmentUris: List<String> = emptyList(),
    val daysRemaining: Long? = null,
)

data class HomeDocumentKitUi(
    val id: Long,
    val name: String,
    val category: String?,
    val targetCompletionDate: String,
    val totalItems: Int,
    val readyItems: Int,
) {
    val progressPercent: Int
        get() = if (totalItems == 0) 0 else (readyItems * 100) / totalItems
}

data class HomeContainerUi(
    val id: Long,
    val name: String,
    val documentCount: Int,
)