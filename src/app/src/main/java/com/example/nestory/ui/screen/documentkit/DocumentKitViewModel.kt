package com.example.nestory.ui.screen.documentkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.domain.repository.DocumentKitRepository
import com.example.nestory.domain.repository.KitItemRepository
import com.example.nestory.relation.KitWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class DocumentKitViewModel(
    private val documentKitRepository: DocumentKitRepository,
    private val kitItemRepository: KitItemRepository,
    private val todayProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _successMessage = MutableStateFlow<String?>(null)
    private val localState = MutableStateFlow(DocumentKitLocalState())

    val uiState: StateFlow<DocumentKitUiState> = combine(
        documentKitRepository.observeAllKits()
            .catch { e ->
                _error.value = e.message ?: "Không thể tải danh sách bộ hồ sơ"
                emit(emptyList())
            },
        _isLoading,
        _error,
        _successMessage,
        localState,
    ) { kits: List<KitWithItems>, isLoading: Boolean, error: String?, successMessage: String?, state: DocumentKitLocalState ->
        val selectedKit = if (state.selectedKitId == null) null else kits.find { it.kit.id == state.selectedKitId }
        val activeFilter = state.activeFilter
        val filteredKits = kits.filter { kitWithItems ->
            val kit = kitWithItems.kit
            val matchesCategory = activeFilter.selectedCategory == null || kit.category == activeFilter.selectedCategory
            val matchesFavorite = activeFilter.isFavorite == null || kit.isFavorite == activeFilter.isFavorite
            val usageStatus = resolveKitUsageStatus(kit.targetCompletionDate, todayProvider())
            val matchesUsage = activeFilter.usageStatuses.isEmpty() ||
                (usageStatus != null && activeFilter.usageStatuses.contains(usageStatus))
            matchesCategory && matchesFavorite && matchesUsage
        }
        DocumentKitUiState(
            kits = filteredKits,
            selectedKit = selectedKit,
            kitItems = selectedKit?.items ?: emptyList(),
            isLoading = isLoading,
            error = error,
            successMessage = successMessage,
            activeFilter = activeFilter,
            draftFilter = state.draftFilter,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DocumentKitUiState(),
    )

    fun selectKit(kitId: Long?) {
        localState.update { it.copy(selectedKitId = kitId) }
    }

    fun clearError() {
        _error.value = null
    }

    fun consumeSuccess() {
        _successMessage.value = null
    }

    fun updateDraftCategory(category: String?) {
        localState.update { it.copy(draftFilter = it.draftFilter.copy(selectedCategory = category)) }
    }

    fun updateDraftFavorite(isFavorite: Boolean?) {
        localState.update { it.copy(draftFilter = it.draftFilter.copy(isFavorite = isFavorite)) }
    }

    fun toggleDraftStatus(status: KitUsageStatus) {
        localState.update { state ->
            val currentStatuses = state.draftFilter.usageStatuses.toMutableSet()
            if (currentStatuses.contains(status)) currentStatuses.remove(status) else currentStatuses.add(status)
            state.copy(draftFilter = state.draftFilter.copy(usageStatuses = currentStatuses))
        }
    }

    fun applyFilter() {
        localState.update { it.copy(activeFilter = it.draftFilter) }
    }

    fun resetFilter() {
        val emptyFilter = DocumentKitFilterState()
        localState.update { it.copy(draftFilter = emptyFilter, activeFilter = emptyFilter) }
    }

    fun syncDraftWithActiveFilter() {
        localState.update { it.copy(draftFilter = it.activeFilter) }
    }

    fun createKit(
        name: String,
        description: String? = null,
        targetCompletionDate: String,
        category: String? = null,
        note: String? = null,
    ) {
        if (!beginOperation()) return
        viewModelScope.launch {
            documentKitRepository.createKit(
                DocumentKitEntity(
                    name = name,
                    category = category,
                    description = description,
                    note = note,
                    targetCompletionDate = targetCompletionDate,
                ),
            ).fold(
                onSuccess = { endOperation(successMessage = "Đã tạo bộ hồ sơ") },
                onFailure = { endOperation(error = it, fallback = "Không thể tạo bộ hồ sơ") },
            )
        }
    }

    fun updateKit(kit: DocumentKitEntity) {
        if (!beginOperation()) return
        viewModelScope.launch {
            documentKitRepository.updateKit(kit).fold(
                onSuccess = { endOperation(successMessage = "Đã cập nhật bộ hồ sơ") },
                onFailure = { endOperation(error = it, fallback = "Không thể cập nhật bộ hồ sơ") },
            )
        }
    }

    fun deleteKit(kit: DocumentKitEntity) {
        if (!beginOperation()) return
        viewModelScope.launch {
            documentKitRepository.deleteKit(kit).fold(
                onSuccess = {
                    if (localState.value.selectedKitId == kit.id) {
                        localState.update { it.copy(selectedKitId = null) }
                    }
                    endOperation(successMessage = "Đã xoá bộ hồ sơ")
                },
                onFailure = { endOperation(error = it, fallback = "Không thể xoá bộ hồ sơ") },
            )
        }
    }

    fun toggleFavorite(kitId: Long) {
        val kit = uiState.value.kits.find { it.kit.id == kitId }?.kit ?: return
        viewModelScope.launch {
            documentKitRepository.updateFavoriteStatus(kitId, !kit.isFavorite)
        }
    }

    fun addItem(
        kitId: Long,
        status: String = DEFAULT_ITEM_STATUS,
        name: String? = null,
        description: String? = null,
        note: String? = null,
        requiredDocuments: Int? = null,
    ) {
        if (!beginOperation()) return
        viewModelScope.launch {
            kitItemRepository.addItem(
                KitItemEntity(
                    status = status,
                    documentKitId = kitId,
                    linkedDocumentId = null,
                    name = name,
                    description = description,
                    note = note,
                    requiredDocuments = requiredDocuments,
                ),
            ).fold(
                onSuccess = { endOperation(successMessage = "Đã thêm mục vào bộ hồ sơ") },
                onFailure = { endOperation(error = it, fallback = "Không thể thêm mục vào bộ hồ sơ") },
            )
        }
    }

    fun updateItem(item: KitItemEntity) {
        if (!beginOperation()) return
        viewModelScope.launch {
            kitItemRepository.updateItem(item).fold(
                onSuccess = { endOperation(successMessage = "Đã cập nhật mục") },
                onFailure = { endOperation(error = it, fallback = "Không thể cập nhật mục") },
            )
        }
    }

    fun removeItem(item: KitItemEntity) {
        if (!beginOperation()) return
        viewModelScope.launch {
            kitItemRepository.deleteItem(item).fold(
                onSuccess = { endOperation(successMessage = "Đã xoá mục") },
                onFailure = { endOperation(error = it, fallback = "Không thể xoá mục") },
            )
        }
    }

    fun linkDocument(itemId: Long, documentId: Long) {
        if (!beginOperation()) return
        viewModelScope.launch {
            kitItemRepository.getItemById(itemId).fold(
                onSuccess = { item ->
                    if (item == null) {
                        endOperation(errorMessage = "Không tìm thấy mục cần liên kết")
                        return@fold
                    }
                    kitItemRepository.updateItem(
                        item.copy(
                            linkedDocumentId = documentId,
                            status = completionStatus(item, linkedCount = 1),
                        ),
                    ).fold(
                        onSuccess = { endOperation(successMessage = "Đã liên kết giấy tờ") },
                        onFailure = { endOperation(error = it, fallback = "Không thể liên kết giấy tờ") },
                    )
                },
                onFailure = { endOperation(error = it, fallback = "Không thể liên kết giấy tờ") },
            )
        }
    }

    fun unlinkDocument(itemId: Long) {
        if (!beginOperation()) return
        viewModelScope.launch {
            kitItemRepository.getItemById(itemId).fold(
                onSuccess = { item ->
                    if (item == null) {
                        endOperation(errorMessage = "Không tìm thấy mục cần huỷ liên kết")
                        return@fold
                    }
                    kitItemRepository.updateItem(
                        item.copy(
                            linkedDocumentId = null,
                            status = completionStatus(item, linkedCount = 0),
                        ),
                    ).fold(
                        onSuccess = { endOperation(successMessage = "Đã huỷ liên kết giấy tờ") },
                        onFailure = { endOperation(error = it, fallback = "Không thể huỷ liên kết giấy tờ") },
                    )
                },
                onFailure = { endOperation(error = it, fallback = "Không thể huỷ liên kết giấy tờ") },
            )
        }
    }

    private fun completionStatus(item: KitItemEntity, linkedCount: Int): String {
        val required = item.requiredDocuments
        return when {
            required != null && required > 0 && linkedCount >= required -> KitItemStatus.READY
            required != null && required > 0 -> KitItemStatus.PENDING
            else -> item.status
        }
    }

    fun createPlaceholder(kitId: Long, status: String = DEFAULT_ITEM_STATUS) {
        addItem(kitId = kitId, status = status)
    }

    private fun beginOperation(): Boolean {
        if (_isLoading.value) return false
        _isLoading.value = true
        _error.value = null
        _successMessage.value = null
        return true
    }

    private fun endOperation(successMessage: String? = null, error: Throwable? = null, fallback: String = "", errorMessage: String? = null) {
        _isLoading.value = false
        if (successMessage != null) {
            _successMessage.value = successMessage
        } else if (errorMessage != null) {
            _error.value = errorMessage
        } else if (error != null) {
            _error.value = error.message ?: fallback
        }
    }

    companion object {
        const val DEFAULT_ITEM_STATUS = "PENDING"
    }
}

private data class DocumentKitLocalState(
    val selectedKitId: Long? = null,
    val activeFilter: DocumentKitFilterState = DocumentKitFilterState(),
    val draftFilter: DocumentKitFilterState = DocumentKitFilterState(),
)

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
