package com.example.nestory.ui.screen.documentkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.domain.repository.DocumentKitRepository
import com.example.nestory.domain.repository.KitItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DocumentKitViewModel(
    private val documentKitRepository: DocumentKitRepository,
    private val kitItemRepository: KitItemRepository,
) : ViewModel() {

    private val _selectedKitId = MutableStateFlow<Long?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DocumentKitUiState> = combine(
        documentKitRepository.observeAllKits()
            .catch { e ->
                _error.value = e.message ?: "Không thể tải danh sách bộ hồ sơ"
                emit(emptyList())
            },
        _selectedKitId,
        _isLoading,
        _error,
    ) { kits, selectedId, isLoading, error ->
        val selectedKit = if (selectedId == null) null else kits.find { it.kit.id == selectedId }
        DocumentKitUiState(
            kits = kits,
            selectedKit = selectedKit,
            kitItems = selectedKit?.items ?: emptyList(),
            isLoading = isLoading,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DocumentKitUiState(),
    )

    fun selectKit(kitId: Long?) {
        _selectedKitId.value = kitId
    }

    fun clearError() {
        _error.value = null
    }

    fun createKit(name: String, description: String?, targetCompletionDate: String) {
        _isLoading.value = true
        viewModelScope.launch {
            documentKitRepository.createKit(
                DocumentKitEntity(
                    name = name,
                    description = description,
                    targetCompletionDate = targetCompletionDate,
                ),
            ).fold(
                onSuccess = { _isLoading.value = false },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Không thể tạo bộ hồ sơ"
                },
            )
        }
    }

    fun updateKit(kit: DocumentKitEntity) {
        _isLoading.value = true
        viewModelScope.launch {
            documentKitRepository.updateKit(kit).fold(
                onSuccess = { _isLoading.value = false },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Không thể cập nhật bộ hồ sơ"
                },
            )
        }
    }

    fun deleteKit(kit: DocumentKitEntity) {
        _isLoading.value = true
        viewModelScope.launch {
            documentKitRepository.deleteKit(kit).fold(
                onSuccess = {
                    _isLoading.value = false
                    if (_selectedKitId.value == kit.id) {
                        _selectedKitId.value = null
                    }
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Không thể xoá bộ hồ sơ"
                },
            )
        }
    }

    fun addItem(kitId: Long, status: String = DEFAULT_ITEM_STATUS) {
        _isLoading.value = true
        viewModelScope.launch {
            kitItemRepository.addItem(
                KitItemEntity(
                    status = status,
                    documentKitId = kitId,
                    linkedDocumentId = null,
                ),
            ).fold(
                onSuccess = { _isLoading.value = false },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Không thể thêm mục vào bộ hồ sơ"
                },
            )
        }
    }

    fun updateItem(item: KitItemEntity) {
        _isLoading.value = true
        viewModelScope.launch {
            kitItemRepository.updateItem(item).fold(
                onSuccess = { _isLoading.value = false },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Không thể cập nhật mục"
                },
            )
        }
    }

    fun removeItem(item: KitItemEntity) {
        _isLoading.value = true
        viewModelScope.launch {
            kitItemRepository.deleteItem(item).fold(
                onSuccess = { _isLoading.value = false },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Không thể xoá mục"
                },
            )
        }
    }

    fun linkDocument(itemId: Long, documentId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            kitItemRepository.getItemById(itemId).fold(
                onSuccess = { item ->
                    if (item == null) {
                        _isLoading.value = false
                        _error.value = "Không tìm thấy mục cần liên kết"
                        return@fold
                    }
                    kitItemRepository.updateItem(item.copy(linkedDocumentId = documentId)).fold(
                        onSuccess = { _isLoading.value = false },
                        onFailure = { error ->
                            _isLoading.value = false
                            _error.value = error.message ?: "Không thể liên kết giấy tờ"
                        },
                    )
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Không thể liên kết giấy tờ"
                },
            )
        }
    }

    fun unlinkDocument(itemId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            kitItemRepository.getItemById(itemId).fold(
                onSuccess = { item ->
                    if (item == null) {
                        _isLoading.value = false
                        _error.value = "Không tìm thấy mục cần huỷ liên kết"
                        return@fold
                    }
                    kitItemRepository.updateItem(item.copy(linkedDocumentId = null)).fold(
                        onSuccess = { _isLoading.value = false },
                        onFailure = { error ->
                            _isLoading.value = false
                            _error.value = error.message ?: "Không thể huỷ liên kết giấy tờ"
                        },
                    )
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Không thể huỷ liên kết giấy tờ"
                },
            )
        }
    }

    fun createPlaceholder(kitId: Long, status: String = DEFAULT_ITEM_STATUS) {
        addItem(kitId = kitId, status = status)
    }

    companion object {
        const val DEFAULT_ITEM_STATUS = "PENDING"
    }
}

class DocumentKitViewModelFactory(
    private val documentKitRepository: DocumentKitRepository,
    private val kitItemRepository: KitItemRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentKitViewModel::class.java)) {
            return DocumentKitViewModel(
                documentKitRepository = documentKitRepository,
                kitItemRepository = kitItemRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
