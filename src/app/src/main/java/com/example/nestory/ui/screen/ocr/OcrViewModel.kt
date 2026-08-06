package com.example.nestory.ui.screen.ocr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.AttachmentEntity
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.repository.AttachmentRepository
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentRepository
import com.example.nestory.domain.repository.KitItemRepository
import com.example.nestory.ui.screen.document.parseExpirationDate
import com.example.nestory.ui.screen.documentkit.KitItemStatus
import com.example.nestory.utils.ocr.CategoryDetector
import com.example.nestory.utils.ocr.DocumentDraftMapper
import com.example.nestory.domain.model.DocumentDraft
import com.example.nestory.utils.ocr.OcrTextParser
import com.example.nestory.domain.repository.OcrRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Orchestrates the full flow:
 * Image -> OcrRepository -> Parser -> CategoryDetector -> DocumentDraftMapper -> UiState.
 */
class OcrViewModel(
    private val ocrRepository: OcrRepository,
    private val parser: OcrTextParser,
    private val categoryDetector: CategoryDetector,
    private val draftMapper: DocumentDraftMapper,
    private val documentRepository: DocumentRepository,
    private val attachmentRepository: AttachmentRepository,
    private val containerRepository: ContainerRepository,
    private val imageStorageManager: ImageStorageManager,
    private val kitItemRepository: KitItemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OcrUiState>(OcrUiState.Idle)
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    private val _containers = MutableStateFlow<List<ContainerEntity>>(emptyList())
    val containers: StateFlow<List<ContainerEntity>> = _containers.asStateFlow()

    private val _fieldErrors = MutableStateFlow(OcrFieldErrors())
    val fieldErrors: StateFlow<OcrFieldErrors> = _fieldErrors.asStateFlow()

    private var capturedBitmaps: List<Bitmap> = emptyList()
    private var pendingKitLinkItemId: Long? = null

    fun setPendingKitLinkItemId(itemId: Long?) {
        pendingKitLinkItemId = itemId
    }

    init {
        observeContainers()
    }

    private fun observeContainers() {
        viewModelScope.launch {
            containerRepository.observeAllContainers().collect { list ->
                _containers.value = list
            }
        }
    }

    fun processImage(bitmap: Bitmap) {
        processImages(listOf(bitmap))
    }

    fun processImages(bitmaps: List<Bitmap>) {
        val firstBitmap = bitmaps.firstOrNull() ?: return
        capturedBitmaps = bitmaps
        _uiState.value = OcrUiState.Processing

        viewModelScope.launch {
            ocrRepository.recognizeText(firstBitmap)
                .map { rawText -> parser.parse(rawText) }
                .onSuccess { result ->
                    val withCategory = result.copy(category = categoryDetector.detect(result))
                    _uiState.value = OcrUiState.Success(draftMapper.toDraft(withCategory))
                }
                .onFailure { error ->
                    _uiState.value = OcrUiState.Error(
                        error.message ?: "Không thể nhận dạng văn bản từ ảnh",
                    )
                }
        }
    }

    fun updateDraft(draft: DocumentDraft) {
        _fieldErrors.value = OcrFieldErrors()
        val current = _uiState.value as? OcrUiState.Success ?: return
        _uiState.value = OcrUiState.Success(draft)
    }

    fun saveDocument(onSaved: (Long) -> Unit) {
        val draft = (_uiState.value as? OcrUiState.Success)?.draft ?: return
        val bitmaps = capturedBitmaps
        if (bitmaps.isEmpty()) return

        val fieldErrors = validateDraft(draft)
        _fieldErrors.value = fieldErrors
        if (fieldErrors.hasErrors) return

        viewModelScope.launch {
            val containerId = draft.containerId ?: return@launch
            val document = DocumentEntity(
                title = draft.title,
                category = draft.category ?: DocumentCategory.OTHER,
                expirationDate = draft.expiryDate,
                notes = draft.notes,
                containerId = containerId,
            )

            documentRepository.createDocument(document).fold(
                onSuccess = { documentId ->
                    var saveError: Throwable? = null
                    bitmaps.forEachIndexed { index, bitmap ->
                        imageStorageManager.saveBitmap(bitmap).fold(
                            onSuccess = { filePath ->
                                attachmentRepository.addAttachmentMetadata(
                                    AttachmentEntity(
                                        fileUri = filePath,
                                        documentId = documentId,
                                        displayOrder = index,
                                    ),
                                ).onFailure { error -> saveError = error }
                            },
                            onFailure = { error -> saveError = error },
                        )
                    }

                    if (saveError == null) {
                        pendingKitLinkItemId?.let { itemId ->
                            kitItemRepository.getItemById(itemId).onSuccess { item ->
                                if (item != null) {
                                    kitItemRepository.updateItem(
                                        item.copy(
                                            linkedDocumentId = documentId,
                                            status = completionStatus(item, linkedCount = 1),
                                        ),
                                    )
                                }
                            }
                        }
                        pendingKitLinkItemId = null
                        onSaved(documentId)
                    } else {
                        saveError?.let { error ->
                            _uiState.value = OcrUiState.Error(
                                error.message ?: "Không thể lưu ảnh đính kèm",
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.value = OcrUiState.Error(
                        error.message ?: "Không thể lưu giấy tờ",
                    )
                },
            )
        }
    }

    fun selectContainer(containerId: Long?) {
        val current = _uiState.value as? OcrUiState.Success ?: return
        updateDraft(current.draft.copy(containerId = containerId))
    }

    fun cancelOcr() {
        _fieldErrors.value = OcrFieldErrors()
        _uiState.value = OcrUiState.Idle
        capturedBitmaps = emptyList()
    }

    private fun completionStatus(item: KitItemEntity, linkedCount: Int): String {
        val required = item.requiredDocuments
        return when {
            required != null && required > 0 && linkedCount >= required -> KitItemStatus.READY
            required != null && required > 0 -> KitItemStatus.PENDING
            else -> item.status
        }
    }

    private fun validateDraft(draft: DocumentDraft): OcrFieldErrors {        val issueDate = draft.issueDate.orEmpty()
        val expiryDate = draft.expiryDate.orEmpty()
        return OcrFieldErrors(
            title = draft.title.isBlank(),
            category = draft.category == null,
            issueDate = issueDate.isNotBlank() && parseExpirationDate(issueDate) == null,
            expiryDate = expiryDate.isNotBlank() && parseExpirationDate(expiryDate) == null,
            container = draft.containerId == null,
        )
    }
}

data class OcrFieldErrors(
    val title: Boolean = false,
    val category: Boolean = false,
    val issueDate: Boolean = false,
    val expiryDate: Boolean = false,
    val container: Boolean = false,
) {
    val hasErrors: Boolean get() = title || category || issueDate || expiryDate || container
}

class OcrViewModelFactory(
    private val ocrRepository: OcrRepository,
    private val parser: OcrTextParser,
    private val categoryDetector: CategoryDetector,
    private val draftMapper: DocumentDraftMapper,
    private val documentRepository: DocumentRepository,
    private val attachmentRepository: AttachmentRepository,
    private val containerRepository: ContainerRepository,
    private val imageStorageManager: ImageStorageManager,
    private val kitItemRepository: KitItemRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OcrViewModel::class.java)) {
            return OcrViewModel(
                ocrRepository = ocrRepository,
                parser = parser,
                categoryDetector = categoryDetector,
                draftMapper = draftMapper,
                documentRepository = documentRepository,
                attachmentRepository = attachmentRepository,
                containerRepository = containerRepository,
                imageStorageManager = imageStorageManager,
                kitItemRepository = kitItemRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
