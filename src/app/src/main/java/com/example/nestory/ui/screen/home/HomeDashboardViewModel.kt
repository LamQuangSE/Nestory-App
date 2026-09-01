package com.example.nestory.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.example.nestory.data.local.entity.CategoryEntity
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.repository.AttachmentRepository
import com.example.nestory.domain.repository.CategoryRepository
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentKitRepository
import com.example.nestory.domain.repository.DocumentRepository
import com.example.nestory.ui.screen.document.DocumentStatus
import com.example.nestory.ui.screen.document.DocumentStatusCalculator
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
            combine(
                documentRepository.observeAllDocuments(),
                containerRepository.observeAllContainers(),
                categoryRepository.getAllCategories(),
                attachmentRepository.observeAllAttachments(),
                documentKitRepository.observeAllKits(),
            ) { documents, containers, categories, attachments, kits ->
                val attachmentsByDocId = attachments.groupBy { it.documentId }
                val rootContainers = containers.filter { it.parentId == null }
                val today = LocalDate.now()

                HomeDashboardUiState(
                    recentDocuments = documents
                        .sortedByDescending { it.lastOpenedAt ?: Long.MIN_VALUE }
                        .take(recentCount)
                        .map { doc ->
                            val catName = resolveCategoryName(doc.categoryId, categories)
                            RecentDocumentUi(
                                id = doc.id,
                                title = doc.title,
                                categoryLabel = catName,
                                expiryDate = doc.expirationDate ?: "Chưa có hạn",
                                attachmentUris = attachmentsByDocId[doc.id].orEmpty().map { it.fileUri }
                            )
                        },
                    attentionDocuments = documents
                        .mapNotNull { doc ->
                            val expiry = DocumentStatusCalculator.parseExpirationDate(doc.expirationDate)
                                ?: return@mapNotNull null
                            val status = DocumentStatusCalculator.calculate(doc.expirationDate, today)
                            if (status == DocumentStatus.Expired) return@mapNotNull null
                            val daysRemaining = ChronoUnit.DAYS.between(today, expiry)
                            Triple(doc, expiry, daysRemaining)
                        }
                        .sortedBy { it.third }
                        .take(DEFAULT_ATTENTION_COUNT)
                        .map { (doc, _, daysRemaining) ->
                            val catName = resolveCategoryName(doc.categoryId, categories)
                            RecentDocumentUi(
                                id = doc.id,
                                title = doc.title,
                                categoryLabel = catName,
                                expiryDate = doc.expirationDate ?: "Chưa có hạn",
                                attachmentUris = attachmentsByDocId[doc.id].orEmpty().map { it.fileUri },
                                daysRemaining = daysRemaining,
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
                    containers = rootContainers
                        .sortedBy { it.name }
                        .map { container ->
                            HomeContainerUi(
                                id = container.id,
                                name = container.name,
                                documentCount = documents.count { it.containerId == container.id },
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
        private const val DEFAULT_ATTENTION_COUNT = 3
    }
}

private fun resolveCategoryName(
    categoryId: String,
    categories: List<CategoryEntity>,
): String {
    categories.find { it.id == categoryId }?.let { return it.name }

    val presetKey = categoryId.removePrefix("preset_")
    return DocumentCategory.entries
        .firstOrNull { it.name.equals(presetKey, ignoreCase = true) }
        ?.toVietnameseLabel()
        ?: "Khác"
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
