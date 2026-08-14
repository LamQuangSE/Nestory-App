package com.example.nestory.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.domain.repository.AttachmentRepository
import com.example.nestory.domain.repository.CategoryRepository
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentKitRepository
import com.example.nestory.domain.repository.DocumentRepository
import com.example.nestory.ui.screen.documentkit.KitItemStatus
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
    categoryRepository: CategoryRepository,
    attachmentRepository: AttachmentRepository,
    documentKitRepository: DocumentKitRepository,
    private val recentCount: Int = DEFAULT_RECENT_COUNT,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Gom thêm luồng dữ liệu của Category để map ID sang Tên danh mục
            combine(
                documentRepository.observeAllDocuments(),
                containerRepository.observeAllContainers(),
                categoryRepository.getAllCategories(),
                attachmentRepository.observeAllAttachments(),
                documentKitRepository.observeAllKits(),
            ) { documents, _, categories, attachments, kits ->
                val attachmentsByDocId = attachments.groupBy { it.documentId }
                
                HomeDashboardUiState(
                    recentDocuments = documents
                        .sortedByDescending { it.id }
                        .take(recentCount)
                        .map { doc ->
                            // Dò tìm tên danh mục từ Database, nếu không thấy thì để "Khác"
                            val catName = categories.find { it.id == doc.categoryId }?.name ?: "Khác"
                            RecentDocumentUi(
                                id = doc.id,
                                title = doc.title,
                                categoryLabel = catName,
                                expiryDate = doc.expirationDate ?: "Chưa có hạn",
                                attachmentUris = attachmentsByDocId[doc.id].orEmpty().map { it.fileUri }
                            )
                        },
                    documentKits = kits
                        .sortedByDescending { it.kit.id }
                        .take(DEFAULT_KIT_COUNT)
                        .map { kitWithItems ->
                            HomeDocumentKitUi(
                                id = kitWithItems.kit.id,
                                name = kitWithItems.kit.name,
                                category = kitWithItems.kit.category,
                                targetCompletionDate = kitWithItems.kit.targetCompletionDate,
                                totalItems = kitWithItems.items.size,
                                readyItems = kitWithItems.items.count { it.status == KitItemStatus.READY },
                            )
                        },
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

    companion object {
        private const val DEFAULT_RECENT_COUNT = 4
        private const val DEFAULT_KIT_COUNT = 3
    }
}

class HomeDashboardViewModelFactory(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val categoryRepository: CategoryRepository,
    private val attachmentRepository: AttachmentRepository,
    private val documentKitRepository: DocumentKitRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeDashboardViewModel::class.java)) {
            return HomeDashboardViewModel(
                documentRepository = documentRepository,
                containerRepository = containerRepository,
                categoryRepository = categoryRepository,
                attachmentRepository = attachmentRepository,
                documentKitRepository = documentKitRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
