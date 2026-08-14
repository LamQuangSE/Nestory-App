package com.example.nestory.ui.screen.home

import com.example.nestory.data.local.entity.ContainerEntity

data class HomeDashboardUiState(
    val recentDocuments: List<RecentDocumentUi> = emptyList(),
    val rootContainers: List<ContainerEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class RecentDocumentUi(
    val id: Long,
    val title: String,
    val categoryLabel: String,
    val expiryDate: String,
    val attachmentUris: List<String> = emptyList(),
)