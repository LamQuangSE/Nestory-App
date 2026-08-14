package com.example.nestory.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeDashboardViewModel(
    documentRepository: DocumentRepository,
    containerRepository: ContainerRepository,
    private val recentCount: Int = DEFAULT_RECENT_COUNT,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                documentRepository.observeAllDocuments(),
                containerRepository.observeAllContainers(),
            ) { documents, containers ->
                HomeDashboardUiState(
                    recentDocuments = documents
                        .sortedByDescending { it.id }
                        .take(recentCount)
                        .map { it.toRecentUi() },
                    rootContainers = containers.filter { it.parentId == null },
                    isLoading = false,
                )
            }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Không thể tải dữ liệu",
                        )
                    }
                }
                .collect { _uiState.value = it }
        }
    }

    private fun DocumentEntity.toRecentUi(): RecentDocumentUi =
        RecentDocumentUi(
            id = id,
            title = title,
            categoryLabel = categoryLabel(category),
            expiryDate = expirationDate ?: "Chưa có hạn",
        )

    companion object {
        private const val DEFAULT_RECENT_COUNT = 4
    }
}

internal fun categoryLabel(category: DocumentCategory): String =
    when (category) {
        DocumentCategory.IDENTITY -> "Nhân thân"
        DocumentCategory.EDUCATION -> "Học vấn"
        DocumentCategory.FINANCE -> "Tài chính"
        DocumentCategory.PROPERTY -> "Bất động sản"
        DocumentCategory.VEHICLE -> "Phương tiện"
        DocumentCategory.HEALTH -> "Sức khỏe"
        DocumentCategory.OTHER -> "Khác"
    }

class HomeDashboardViewModelFactory(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeDashboardViewModel::class.java)) {
            return HomeDashboardViewModel(
                documentRepository = documentRepository,
                containerRepository = containerRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}