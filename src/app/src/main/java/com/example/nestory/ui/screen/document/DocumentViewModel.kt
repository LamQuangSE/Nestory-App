package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.model.ExpiryReminderSettings
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentRepository
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
    containerRepository: ContainerRepository,
    expiryReminderSettings: Flow<ExpiryReminderSettings>,
    private val todayProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val localState = MutableStateFlow(DocumentLocalState())

    val uiState: StateFlow<DocumentUiState> = combine(
        documentRepository.observeAllDocuments(),
        containerRepository.observeAllContainers(),
        expiryReminderSettings,
        localState,
    ) { documents, containers, settings, state ->
        val visibleDocuments = documents
            .filter { document ->
                state.searchQuery.isBlank() ||
                    document.title.contains(state.searchQuery, ignoreCase = true) ||
                    document.notes.orEmpty().contains(state.searchQuery, ignoreCase = true) ||
                    categoryLabel(document.category).contains(state.searchQuery, ignoreCase = true)
            }
            .map { document ->
                document.toUiModel(
                    containers = containers,
                    settings = settings,
                    today = todayProvider(),
                )
            }

        state.toUiState(
            documents = visibleDocuments,
            selectedDocument = visibleDocuments.firstOrNull { it.id == state.selectedDocumentId },
        )
    }
        .catch { error ->
            emit(
                localState.value.toUiState(
                    documents = emptyList(),
                    selectedDocument = null,
                    errorMessage = error.localizedMessage ?: "Không thể tải danh sách giấy tờ",
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

    fun deleteSelectedDocument(onDeleted: () -> Unit) {
        val selected = uiState.value.selectedDocument ?: return
        val documentId = selected.id.toLongOrNull() ?: return

        viewModelScope.launch {
            documentRepository.getDocumentById(documentId).fold(
                onSuccess = { document ->
                    if (document != null) {
                        documentRepository.deleteDocument(document).fold(
                            onSuccess = {
                                clearSelection()
                                onDeleted()
                            },
                            onFailure = { error ->
                                setError(error.localizedMessage ?: "Không thể xóa giấy tờ")
                            },
                        )
                    } else {
                        clearSelection()
                        onDeleted()
                    }
                },
                onFailure = { error ->
                    setError(error.localizedMessage ?: "Không thể tải giấy tờ")
                },
            )
        }
    }

    fun clearError() {
        localState.update { it.copy(errorMessage = null) }
    }

    private fun setError(message: String) {
        localState.update { it.copy(errorMessage = message) }
    }

    private data class DocumentLocalState(
        val selectedDocumentId: String? = null,
        val searchQuery: String = "",
        val errorMessage: String? = null,
    ) {
        fun toUiState(
            documents: List<DocumentUiModel>,
            selectedDocument: DocumentUiModel?,
            errorMessage: String? = this.errorMessage,
        ) = DocumentUiState(
            documents = documents,
            selectedDocument = selectedDocument,
            searchQuery = searchQuery,
            errorMessage = errorMessage,
        )
    }
}

private fun DocumentEntity.toUiModel(
    containers: List<ContainerEntity>,
    settings: ExpiryReminderSettings,
    today: LocalDate,
): DocumentUiModel =
    DocumentUiModel(
        id = id.toString(),
        name = title,
        category = categoryLabel(category),
        containerPath = buildContainerPath(containerId, containers),
        status = calculateDocumentStatus(expirationDate, settings, today),
        expiryDate = expirationDate ?: "Chưa có hạn",
        categoryColor = categoryColor(category),
    )

private fun buildContainerPath(
    containerId: Long,
    containers: List<ContainerEntity>,
): String {
    val path = mutableListOf<String>()
    var currentId: Long? = containerId
    while (currentId != null) {
        val container = containers.firstOrNull { it.id == currentId } ?: break
        path.add(0, container.name)
        currentId = container.parentId
    }
    return path.takeIf { it.isNotEmpty() }?.joinToString(" > ") ?: "Chưa chọn container"
}

private fun categoryLabel(category: DocumentCategory): String =
    when (category) {
        DocumentCategory.IDENTITY -> "Nhân thân"
        DocumentCategory.EDUCATION -> "Học vấn"
        DocumentCategory.FINANCE -> "Tài chính"
        DocumentCategory.PROPERTY -> "Bất động sản"
        DocumentCategory.VEHICLE -> "Phương tiện"
        DocumentCategory.HEALTH -> "Sức khỏe"
        DocumentCategory.OTHER -> "Khác"
    }

private fun categoryColor(category: DocumentCategory): Color =
    when (category) {
        DocumentCategory.IDENTITY -> Color(0xFF1855EE)
        DocumentCategory.EDUCATION -> Color(0xFF6D28D9)
        DocumentCategory.FINANCE -> Color(0xFF07BC67)
        DocumentCategory.PROPERTY -> Color(0xFFEB6E00)
        DocumentCategory.VEHICLE -> Color(0xFF0F766E)
        DocumentCategory.HEALTH -> Color(0xFFCF1111)
        DocumentCategory.OTHER -> Color(0xFF717171)
    }

class DocumentViewModelFactory(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val expiryReminderSettings: Flow<ExpiryReminderSettings>,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java)) {
            return DocumentViewModel(
                documentRepository = documentRepository,
                containerRepository = containerRepository,
                expiryReminderSettings = expiryReminderSettings,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
