package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.entity.CategoryEntity
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.ReminderEntity
import com.example.nestory.domain.model.DocumentCategory // Đã import thêm để lấy 6 mục mặc định
import com.example.nestory.domain.repository.AttachmentRepository
import com.example.nestory.domain.repository.CategoryRepository
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentRepository
import com.example.nestory.domain.repository.ReminderRepository
import com.example.nestory.ui.theme.CategoryFallbackColor
import com.example.nestory.ui.theme.categoryColor // Đã import
import com.example.nestory.ui.theme.isPredefinedCategoryName // Đã import
import com.example.nestory.ui.theme.predefinedCategoryColor
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
    private val attachmentRepository: AttachmentRepository,
    private val reminderRepository: ReminderRepository,
    private val imageStorageManager: ImageStorageManager,
    private val todayProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val localState = MutableStateFlow(DocumentLocalState())

    private val documentsWithReminders = combine(
        documentRepository.observeAllDocuments(),
        reminderRepository.observeAllReminders(),
    ) { documents, reminders ->
        documents to reminders
    }

    val uiState: StateFlow<DocumentUiState> = combine(
        documentsWithReminders,
        containerRepository.observeAllContainers(),
        categoryRepository.getAllCategories(),
        attachmentRepository.observeAllAttachments(),
        localState,
    ) { documentsAndReminders, containers, categories, attachments, state ->

        val documents = documentsAndReminders.first
        val reminders = documentsAndReminders.second
        val attachmentsByDocumentId = attachments.groupBy { it.documentId }
        val remindersByDocumentId = reminders
            .filter { it.documentId != null }
            .associateBy { it.documentId }

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

        // ==========================================
        // CẬP NHẬT LOGIC: THÊM 6 MỤC MẶC ĐỊNH & SORT A-Z 2 TẦNG
        // ==========================================
        val dbCategoryUiModels = categories.map { category ->
            DocCategoryUiModel(
                id = category.id,
                name = category.name,
                color = predefinedCategoryColor(category.name) ?: Color(category.colorValue.toULong())
            )
        }

        val existingCategoryNames = dbCategoryUiModels.map { it.name.trim().lowercase() }.toSet()
        val missingDefaultCategories = DocumentCategory.entries.mapNotNull { preset ->
            val label = preset.toVietnameseLabel()
            if (label.lowercase() in existingCategoryNames) null
            else DocCategoryUiModel(
                id = "preset_${preset.name}",
                name = label,
                color = preset.categoryColor
            )
        }

        val allAvailableCategories = dbCategoryUiModels + missingDefaultCategories
        
        // Chia 2 nhóm và Sort A-Z
        val defaultGroup = allAvailableCategories.filter { isPredefinedCategoryName(it.name) }.sortedBy { it.name }
        val customGroup = allAvailableCategories.filterNot { isPredefinedCategoryName(it.name) }.sortedBy { it.name }
        
        val sortedCategoryUiModels = defaultGroup + customGroup
        // ==========================================

        val filteredDocuments = documents.filter { document ->
            val catName = resolveDocumentCategory(document.categoryId, categories).name
            val cleanQuery = state.searchQuery.replace("""[^\p{L}\p{N}]+$""".toRegex(), "")
            
            val matchesSearch = cleanQuery.isBlank() ||
                    document.title.contains(cleanQuery, ignoreCase = true) ||
                    document.notes.orEmpty().contains(cleanQuery, ignoreCase = true) ||
                    catName.contains(cleanQuery, ignoreCase = true) ||
                    document.ocrText.orEmpty().contains(cleanQuery, ignoreCase = true) ||
                    document.holderName.orEmpty().contains(cleanQuery, ignoreCase = true) ||
                    document.documentNumber.orEmpty().contains(cleanQuery, ignoreCase = true)

            // TRÍCH XUẤT LỖI TÌM ẨN: Khi Document lưu categoryId dưới dạng ID của Database, 
            // nhưng Filter lại truyền vào "preset_IDENTITY", việc lọc sẽ bị rỗng.
            // Giải pháp: Tìm tên của Category đang được chọn trong filter, rồi so sánh với tên Category của Document.
            val selectedFilterCatName = state.activeFilter.selectedCategoryId?.let { selectedCategoryId ->
                sortedCategoryUiModels.find { it.id == selectedCategoryId }?.name
                    ?: resolvePresetCategory(selectedCategoryId)?.toVietnameseLabel()
            }
            val docCatName = sortedCategoryUiModels.find { it.id == document.categoryId }?.name
                ?: resolveDocumentCategory(document.categoryId, categories).name
            
            val matchesCategory = state.activeFilter.selectedCategoryId == null || docCatName == selectedFilterCatName
            
            val matchesContainer = state.activeFilter.selectedContainerId == null || document.containerId == state.activeFilter.selectedContainerId
            val matchesFav = state.activeFilter.isFavorite == null || document.isFavorite == state.activeFilter.isFavorite

            matchesSearch && matchesCategory && matchesContainer && matchesFav
        }.map { document ->
            document.toUiModel(
                containers = containers,
                categories = categories,
                today = todayProvider(),
                reminder = remindersByDocumentId[document.id],
                attachmentUris = attachmentsByDocumentId[document.id].orEmpty().map { it.fileUri }
            )
        }.filter { uiModel ->
            state.activeFilter.statuses.isEmpty() || state.activeFilter.statuses.contains(uiModel.status)
        }

        state.toUiState(
            documents = filteredDocuments,
            availableContainers = containerUiModels,
            availableCategories = sortedCategoryUiModels, // Truyền danh sách đã sort xuống UI
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
        documentId.toLongOrNull()?.let { id ->
            viewModelScope.launch {
                documentRepository.updateLastOpenedAt(id, System.currentTimeMillis())
            }
        }
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

    fun updateDocumentDetails(
        title: String,
        categoryLabelValue: String,
        expirationDate: String,
        containerId: Long? = null,
        pdfFileName: String? = null,
    ) {
        if (isSaving) return
        val selected = uiState.value.selectedDocument ?: return
        val documentId = selected.id.toLongOrNull() ?: return

        setSaving(true)
        viewModelScope.launch {
            documentRepository.getDocumentById(documentId).fold(
                onSuccess = { document ->
                    if (document != null) {
                        if (pdfFileName != null && pdfFileName.isNotBlank()) {
                            attachmentRepository.getAttachmentsByDocumentId(documentId).fold(
                                onSuccess = { attachments ->
                                    val pdfAttachment = attachments.firstOrNull {
                                        it.fileUri.endsWith(".pdf", ignoreCase = true)
                                    }
                                    if (pdfAttachment != null) {
                                        val newPath = imageStorageManager
                                            .renamePdf(pdfAttachment.fileUri, pdfFileName)
                                            .getOrNull()
                                        if (newPath != null && newPath != pdfAttachment.fileUri) {
                                            attachmentRepository.updateAttachmentMetadata(
                                                pdfAttachment.copy(fileUri = newPath),
                                            )
                                        }
                                    }
                                },
                                onFailure = { },
                            )
                        }
                        val selectedCategory = uiState.value.availableCategories.firstOrNull { it.name == categoryLabelValue.trim() }
                        val newCategoryId = selectedCategory?.id ?: document.categoryId

                        documentRepository.updateDocument(
                            document.copy(
                                title = title.ifBlank { document.title },
                                categoryId = newCategoryId,
                                containerId = containerId ?: document.containerId,
                                expirationDate = expirationDate.takeUnless {
                                    it.isBlank() || it == "Chưa có hạn"
                                },
                            ),
                        ).fold(
                            onSuccess = { setSaving(false) },
                            onFailure = { error ->
                                setSaving(false)
                                setError(error.localizedMessage ?: "Không thể lưu thay đổi")
                            },
                        )
                    } else {
                        setSaving(false)
                    }
                },
                onFailure = { error ->
                    setSaving(false)
                    setError(error.localizedMessage ?: "Không thể tải giấy tờ")
                },
            )
        }
    }

    fun toggleFavorite() {
        val selected = uiState.value.selectedDocument ?: return
        toggleFavorite(selected.id)
    }

    fun toggleFavorite(documentId: String) {
        val documentIdLong = documentId.toLongOrNull() ?: return

        viewModelScope.launch {
            documentRepository.getDocumentById(documentIdLong).fold(
                onSuccess = { document ->
                    if (document != null) {
                        documentRepository.updateFavoriteStatus(
                            documentId = document.id,
                            isFavorite = !document.isFavorite,
                        ).onFailure { error ->
                            setError(error.localizedMessage ?: "Không thể cập nhật yêu thích")
                        }
                    }
                },
                onFailure = { error ->
                    setError(error.localizedMessage ?: "Không thể tải giấy tờ")
                },
            )
        }
    }

    fun deleteSelectedDocument(onDeleted: () -> Unit) {
        if (isSaving) return
        val selected = uiState.value.selectedDocument ?: return
        val documentId = selected.id.toLongOrNull() ?: return
        setSaving(true)
        viewModelScope.launch {
            documentRepository.getDocumentById(documentId).fold(
                onSuccess = { document ->
                    if (document != null) {
                        documentRepository.deleteDocument(document).fold(
                            onSuccess = { clearSelection(); setSaving(false); onDeleted() },
                            onFailure = { error ->
                                setSaving(false)
                                setError(error.localizedMessage ?: "Lỗi xóa")
                            },
                        )
                    } else { clearSelection(); setSaving(false); onDeleted() }
                },
                onFailure = { error ->
                    setSaving(false)
                    setError(error.localizedMessage ?: "Lỗi tải")
                },
            )
        }
    }

    fun clearError() { localState.update { it.copy(errorMessage = null) } }
    private fun setError(message: String) { localState.update { it.copy(errorMessage = message) } }
    private fun setSaving(saving: Boolean) { localState.update { it.copy(isSaving = saving) } }
    private val isSaving: Boolean get() = localState.value.isSaving

    private data class DocumentLocalState(
        val selectedDocumentId: String? = null,
        val searchQuery: String = "",
        val activeFilter: DocumentFilterState = DocumentFilterState(),
        val draftFilter: DocumentFilterState = DocumentFilterState(),
        val errorMessage: String? = null,
        val isSaving: Boolean = false,
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
            isSaving = isSaving,
        )
    }
}

private fun DocumentEntity.toUiModel(
    containers: List<ContainerEntity>,
    categories: List<CategoryEntity>,
    today: LocalDate,
    reminder: ReminderEntity? = null,
    attachmentUris: List<String> = emptyList(),
): DocumentUiModel {
    val category = resolveDocumentCategory(categoryId, categories)
    return DocumentUiModel(
        id = id.toString(),
        name = title,
        category = category.name,
        containerPath = buildContainerPath(containerId, containers),
        containerId = containerId,
        status = DocumentStatusCalculator.calculate(expirationDate, today, reminder),
        expiryDate = expirationDate ?: "Chưa có hạn",
        categoryColor = category.color,
        isFavorite = isFavorite,
        attachmentUris = attachmentUris,
    )
}

private data class ResolvedCategory(
    val name: String,
    val color: Color,
)

private fun resolveDocumentCategory(
    categoryId: String,
    categories: List<CategoryEntity>,
): ResolvedCategory {
    val categoryEntity = categories.find { it.id == categoryId }
    if (categoryEntity != null) {
        return ResolvedCategory(
            name = categoryEntity.name,
            color = predefinedCategoryColor(categoryEntity.name) ?: Color(categoryEntity.colorValue.toULong()),
        )
    }

    val preset = resolvePresetCategory(categoryId)
    if (preset != null) {
        return ResolvedCategory(
            name = preset.toVietnameseLabel(),
            color = preset.categoryColor,
        )
    }

    return ResolvedCategory(name = "Khác", color = CategoryFallbackColor)
}

private fun resolvePresetCategory(categoryId: String): DocumentCategory? {
    val key = categoryId.removePrefix("preset_")
    return DocumentCategory.entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
}

internal fun buildContainerPath(
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

class DocumentViewModelFactory(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val categoryRepository: CategoryRepository,
    private val attachmentRepository: AttachmentRepository,
    private val reminderRepository: ReminderRepository,
    private val imageStorageManager: ImageStorageManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java)) {
            return DocumentViewModel(
                documentRepository = documentRepository,
                containerRepository = containerRepository,
                categoryRepository = categoryRepository,
                attachmentRepository = attachmentRepository,
                reminderRepository = reminderRepository,
                imageStorageManager = imageStorageManager,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
