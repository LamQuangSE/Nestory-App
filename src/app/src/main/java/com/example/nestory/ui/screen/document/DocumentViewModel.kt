package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.CategoryEntity
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.domain.model.ExpiryReminderSettings
import com.example.nestory.domain.repository.CategoryRepository
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentRepository
import com.example.nestory.ui.assets.AppIcons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class DocumentViewModel(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val categoryRepository: CategoryRepository,
    expiryReminderSettings: Flow<ExpiryReminderSettings>,
    private val todayProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val localState = MutableStateFlow(DocumentLocalState())

    val uiState: StateFlow<DocumentUiState> = combine(
        documentRepository.observeAllDocuments(),
        containerRepository.observeAllContainers(),
        categoryRepository.getAllCategories(),
        expiryReminderSettings,
        localState,
    ) { documents, containers, categories, settings, state ->
        
        val containerUiModels = containers.map { container ->
            ContainerUiModel(
                id = container.id,
                name = container.name,
                fullPath = buildContainerPath(container.id, containers),
                parentId = container.parentId,
                hasChildren = containers.any { it.parentId == container.id },
                childFolderCount = containers.count { it.parentId == container.id },
                documentCount = documents.count { it.containerId == container.id }
            )
        }

        val categoryUiModels = categories.map { category ->
            DocCategoryUiModel(
                id = category.id,
                name = category.name,
                color = Color(category.colorValue.toULong())
            )
        }

        val filteredDocuments = documents.filter { document ->
            val catName = categories.find { it.id == document.categoryId }?.name ?: "Khác"
            val matchesSearch = state.searchQuery.isBlank() ||
                    document.title.contains(state.searchQuery, ignoreCase = true) ||
                    document.notes.orEmpty().contains(state.searchQuery, ignoreCase = true) ||
                    catName.contains(state.searchQuery, ignoreCase = true)

            val matchesCategory = state.activeFilter.selectedCategoryId == null || document.categoryId == state.activeFilter.selectedCategoryId
            val matchesContainer = state.activeFilter.selectedContainerId == null || document.containerId == state.activeFilter.selectedContainerId
            val matchesFav = state.activeFilter.isFavorite == null || document.isFavorite == state.activeFilter.isFavorite

            matchesSearch && matchesCategory && matchesContainer && matchesFav
        }.map { document ->
            document.toUiModel(containers, categories, settings, todayProvider())
        }.filter { uiModel ->
            state.activeFilter.statuses.isEmpty() || state.activeFilter.statuses.contains(uiModel.status)
        }

        state.toUiState(
            documents = filteredDocuments,
            availableContainers = containerUiModels,
            availableCategories = categoryUiModels,
            selectedDocument = filteredDocuments.firstOrNull { it.id == state.selectedDocumentId },
        )
    }
        .catch { error ->
            emit(
                localState.value.toUiState(
                    documents = emptyList(),
                    availableContainers = emptyList(),
                    availableCategories = emptyList(),
                    selectedDocument = null,
                    errorMessage = error.localizedMessage ?: "Lỗi",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DocumentUiState(isLoading = true),
        )

    fun onSearchQueryChange(query: String) {
        localState.update { it.copy(searchQuery = query) }
    }

    fun selectDocument(documentId: String) {
        localState.update { it.copy(selectedDocumentId = documentId) }
    }

    fun clearSelection() {
        localState.update { it.copy(selectedDocumentId = null) }
    }

    fun updateDraftCategory(categoryId: String?) {
        localState.update { it.copy(draftFilter = it.draftFilter.copy(selectedCategoryId = categoryId)) }
    }

    fun updateDraftContainer(containerId: Long?) {
        localState.update { it.copy(draftFilter = it.draftFilter.copy(selectedContainerId = containerId)) }
    }

    fun updateDraftFavorite(isFavorite: Boolean?) {
        localState.update { it.copy(draftFilter = it.draftFilter.copy(isFavorite = isFavorite)) }
    }

    fun toggleDraftStatus(status: DocumentStatus) {
        localState.update { state ->
            val currentStatuses = state.draftFilter.statuses.toMutableSet()
            if (currentStatuses.contains(status)) currentStatuses.remove(status) else currentStatuses.add(status)
            state.copy(draftFilter = state.draftFilter.copy(statuses = currentStatuses))
        }
    }

    fun applyFilter() {
        localState.update { it.copy(activeFilter = it.draftFilter) }
    }

    fun resetFilter() {
        val emptyFilter = DocumentFilterState()
        localState.update { it.copy(draftFilter = emptyFilter, activeFilter = emptyFilter) }
    }

    fun syncDraftWithActiveFilter() {
        localState.update { it.copy(draftFilter = it.activeFilter) }
    }

    fun deleteSelectedDocument(onDeleted: () -> Unit) {
        val selected = uiState.value.selectedDocument ?: return
        val documentId = selected.id.toLongOrNull() ?: return
        viewModelScope.launch {
            documentRepository.getDocumentById(documentId).fold(
                onSuccess = { document ->
                    if (document != null) {
                        documentRepository.deleteDocument(document).fold(
                            onSuccess = { clearSelection(); onDeleted() },
                            onFailure = { error -> setError(error.localizedMessage ?: "Lỗi xóa") },
                        )
                    } else { clearSelection(); onDeleted() }
                },
                onFailure = { error -> setError(error.localizedMessage ?: "Lỗi tải") },
            )
        }
    }

    fun clearError() { localState.update { it.copy(errorMessage = null) } }
    private fun setError(message: String) { localState.update { it.copy(errorMessage = message) } }

    private data class DocumentLocalState(
        val selectedDocumentId: String? = null,
        val searchQuery: String = "",
        val activeFilter: DocumentFilterState = DocumentFilterState(),
        val draftFilter: DocumentFilterState = DocumentFilterState(),
        val errorMessage: String? = null,
    ) {
        fun toUiState(
            documents: List<DocumentUiModel>,
            availableContainers: List<ContainerUiModel>,
            availableCategories: List<DocCategoryUiModel>,
            selectedDocument: DocumentUiModel?,
            errorMessage: String? = this.errorMessage,
        ) = DocumentUiState(
            documents = documents,
            availableContainers = availableContainers,
            availableCategories = availableCategories,
            selectedDocument = selectedDocument,
            searchQuery = searchQuery,
            activeFilter = activeFilter,
            draftFilter = draftFilter,
            errorMessage = errorMessage,
        )
    }
}

private fun DocumentEntity.toUiModel(
    containers: List<ContainerEntity>,
    categories: List<CategoryEntity>,
    settings: ExpiryReminderSettings,
    today: LocalDate,
): DocumentUiModel {
    val categoryEntity = categories.find { it.id == this.categoryId }
    return DocumentUiModel(
        id = id.toString(),
        name = title,
        category = categoryEntity?.name ?: "Khác",
        containerPath = buildContainerPath(containerId, containers),
        // Gọi Object Calculator vừa được tách ra
        status = DocumentStatusCalculator.calculate(expirationDate, settings, today),
        expiryDate = expirationDate ?: "Chưa có hạn",
        categoryColor = categoryEntity?.let { Color(it.colorValue.toULong()) } ?: Color(0xFF717171),
        isFavorite = isFavorite
    )
}

fun buildContainerPath(containerId: Long, containers: List<ContainerEntity>): String {
    val path = mutableListOf<String>()
    var currentId: Long? = containerId
    while (currentId != null) {
        val container = containers.firstOrNull { it.id == currentId } ?: break
        path.add(0, container.name)
        currentId = container.parentId
    }
    return path.takeIf { it.isNotEmpty() }?.joinToString(" > ") ?: "Chưa chọn container"
}

class DocumentViewModelFactory(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val categoryRepository: CategoryRepository,
    private val expiryReminderSettings: Flow<ExpiryReminderSettings>,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java)) {
            return DocumentViewModel(
                documentRepository = documentRepository,
                containerRepository = containerRepository,
                categoryRepository = categoryRepository,
                expiryReminderSettings = expiryReminderSettings,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}