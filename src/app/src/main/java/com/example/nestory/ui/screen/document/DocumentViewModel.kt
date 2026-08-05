package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.repository.CategoryRepository
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DocumentViewModel(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val categoryRepository: CategoryRepository
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
            mapToUiModel(entity, containers, categories)
        }

        DocumentUiState(
            documents = mappedDocuments,
            selectedDocument = mappedDocuments.find { it.id == selectedId },
            searchQuery = query
        )
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
            
            val category = DocumentCategory.entries.find { 
                categoryLabel(it) == categoryName 
            } ?: DocumentCategory.OTHER

            val updated = existing.copy(
                title = title,
                category = category,
                expirationDate = expiryDate,
                containerId = containerId ?: existing.containerId
            )
            documentRepository.updateDocument(updated)
        }
    }

    fun clearError() {
        // Implement if DocumentUiState has error field
    }

    private fun mapToUiModel(
        entity: DocumentEntity,
        allContainers: List<com.example.nestory.data.local.entity.ContainerEntity>,
        allCategories: List<com.example.nestory.data.local.entity.CategoryEntity>
    ): DocumentUiModel {
        val path = buildContainerPath(entity.containerId, allContainers)
        val pathString = path.joinToString(" > ") { it.name }
        
        val categoryLabel = categoryLabel(entity.category)
        val categoryColor = allCategories.find { it.name == categoryLabel }?.let {
            Color(it.colorValue.toULong())
        } ?: Color(0xFF919191)

        return DocumentUiModel(
            id = entity.id.toString(),
            name = entity.title,
            category = categoryLabel,
            containerPath = pathString,
            status = calculateStatus(entity.expirationDate),
            expiryDate = entity.expirationDate ?: "",
            categoryColor = categoryColor
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

    private fun categoryLabel(category: DocumentCategory): String = when (category) {
        DocumentCategory.IDENTITY -> "Nhân thân"
        DocumentCategory.EDUCATION -> "Học vấn"
        DocumentCategory.FINANCE -> "Tài chính"
        DocumentCategory.PROPERTY -> "Bất động sản"
        DocumentCategory.VEHICLE -> "Phương tiện"
        DocumentCategory.HEALTH -> "Sức khỏe"
        DocumentCategory.OTHER -> "Khác"
    }
}

class DocumentViewModelFactory(
    private val documentRepository: DocumentRepository,
    private val containerRepository: ContainerRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DocumentViewModel(documentRepository, containerRepository, categoryRepository) as T
    }
}
