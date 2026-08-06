package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.AttachmentEntity
import com.example.nestory.domain.repository.AttachmentRepository
import com.example.nestory.domain.repository.CategoryRepository
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentViewModel(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val categoryRepository: CategoryRepository,
    private val attachmentRepository: AttachmentRepository,
    private val imageStorageManager: ImageStorageManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedDocumentId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DocumentUiState> = combine(
        documentRepository.observeAllDocuments(),
        _searchQuery,
        _selectedDocumentId,
        containerRepository.observeAllContainers(),
        categoryRepository.getAllCategories()
    ) { documents, query, selectedId, containers, categories ->
        val filtered = if (query.isBlank()) documents else {
            documents.filter { it.title.contains(query, ignoreCase = true) }
        }

        val mappedDocuments = filtered.map { entity ->
            mapToUiModel(entity, containers, categories, emptyList())
        }

        DocumentUiState(
            documents = mappedDocuments,
            selectedDocument = mappedDocuments.find { it.id == selectedId },
            searchQuery = query
        )
    }.flatMapLatest { state ->
        if (state.documents.isEmpty()) flowOf(state)
        else {
            flow {
                val enhancedDocs = state.documents.map { doc ->
                    val attachments = attachmentRepository.getAttachmentsByDocumentId(doc.id.toLong()).getOrDefault(emptyList())
                    doc.copy(attachmentUris = attachments.map { it.fileUri })
                }
                emit(state.copy(
                    documents = enhancedDocs,
                    selectedDocument = enhancedDocs.find { it.id == state.selectedDocument?.id }
                ))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DocumentUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectDocument(documentId: String) {
        _selectedDocumentId.value = documentId
    }

    fun clearSelection() {
        _selectedDocumentId.value = null
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            val idLong = documentId.toLongOrNull() ?: return@launch
            val entity = documentRepository.getDocumentById(idLong).getOrNull()
            if (entity != null) {
                // Delete physical files first
                val attachments = attachmentRepository.getAttachmentsByDocumentId(idLong).getOrDefault(emptyList())
                attachments.forEach { attachment ->
                    imageStorageManager.deleteFile(attachment.fileUri)
                }
                documentRepository.deleteDocument(entity)
            }
        }
    }

    fun deleteSelectedDocument(onDeleted: () -> Unit) {
        val selectedId = _selectedDocumentId.value ?: return
        viewModelScope.launch {
            val idLong = selectedId.toLongOrNull() ?: return@launch
            val entity = documentRepository.getDocumentById(idLong).getOrNull()
            if (entity != null) {
                // Delete physical files first
                val attachments = attachmentRepository.getAttachmentsByDocumentId(idLong).getOrDefault(emptyList())
                attachments.forEach { attachment ->
                    imageStorageManager.deleteFile(attachment.fileUri)
                }
                documentRepository.deleteDocument(entity)
                _selectedDocumentId.value = null
                onDeleted()
            }
        }
    }

    fun updateDocument(
        id: String,
        title: String,
        categoryName: String,
        expiryDate: String,
        containerId: Long?
    ) {
        viewModelScope.launch {
            val idLong = id.toLongOrNull() ?: return@launch
            val existing = documentRepository.getDocumentById(idLong).getOrNull() ?: return@launch
            
            val updated = existing.copy(
                title = title,
                category = categoryName,
                expirationDate = expiryDate,
                containerId = containerId ?: existing.containerId
            )
            documentRepository.updateDocument(updated)
        }
    }

    fun clearError() {
        // Implement if needed
    }

    private fun mapToUiModel(
        entity: DocumentEntity,
        allContainers: List<com.example.nestory.data.local.entity.ContainerEntity>,
        allCategories: List<com.example.nestory.data.local.entity.CategoryEntity>,
        attachments: List<AttachmentEntity>
    ): DocumentUiModel {
        val path = buildContainerPath(entity.containerId, allContainers)
        val pathString = path.joinToString(" > ") { it.name }
        
        val categoryColor = allCategories.find { it.name == entity.category }?.let {
            Color(it.colorValue.toULong())
        } ?: Color(0xFF919191)

        return DocumentUiModel(
            id = entity.id.toString(),
            name = entity.title,
            category = entity.category,
            containerPath = pathString,
            containerId = entity.containerId,
            status = calculateStatus(entity.expirationDate),
            expiryDate = entity.expirationDate ?: "",
            categoryColor = categoryColor,
            attachmentUris = attachments.map { it.fileUri }
        )
    }

    private fun buildContainerPath(
        containerId: Long,
        allContainers: List<com.example.nestory.data.local.entity.ContainerEntity>
    ): List<com.example.nestory.data.local.entity.ContainerEntity> {
        val path = mutableListOf<com.example.nestory.data.local.entity.ContainerEntity>()
        var currentId: Long? = containerId
        while (currentId != null) {
            val container = allContainers.find { it.id == currentId } ?: break
            path.add(0, container)
            currentId = container.parentId
        }
        return path
    }

    private fun calculateStatus(expiryDate: String?): DocumentStatus {
        if (expiryDate.isNullOrBlank()) return DocumentStatus.Active
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(expiryDate) ?: return DocumentStatus.Active
            val now = Calendar.getInstance().time
            val diff = date.time - now.time
            val days = diff / (1000 * 60 * 60 * 24)

            when {
                diff < 0 -> DocumentStatus.Expired
                days <= 30 -> DocumentStatus.ExpiringSoon
                else -> DocumentStatus.Active
            }
        } catch (e: Exception) {
            DocumentStatus.Active
        }
    }
}

class DocumentViewModelFactory(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val categoryRepository: CategoryRepository,
    private val attachmentRepository: AttachmentRepository,
    private val imageStorageManager: ImageStorageManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DocumentViewModel(documentRepository, containerRepository, categoryRepository, attachmentRepository, imageStorageManager) as T
    }
}
