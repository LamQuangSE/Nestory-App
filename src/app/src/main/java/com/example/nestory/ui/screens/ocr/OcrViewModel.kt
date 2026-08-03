package com.example.nestory.ui.screens.ocr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.entity.AttachmentEntity
import com.example.nestory.data.entity.ContainerEntity
import com.example.nestory.data.entity.DocumentEntity
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.model.DocumentCategory
import com.example.nestory.data.repository.AttachmentRepository
import com.example.nestory.data.repository.ContainerRepository
import com.example.nestory.data.repository.DocumentRepository
import com.example.nestory.data.ocr.CategoryDetector
import com.example.nestory.data.ocr.DocumentDraftMapper
import com.example.nestory.data.model.DocumentDraft
import com.example.nestory.data.ocr.OcrTextParser
import com.example.nestory.data.repository.OcrRepository
import com.example.nestory.ui.screens.ocr.OcrUiState
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<OcrUiState>(OcrUiState.Idle)
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    private val _containers = MutableStateFlow<List<ContainerEntity>>(emptyList())
    val containers: StateFlow<List<ContainerEntity>> = _containers.asStateFlow()

    private var capturedBitmap: Bitmap? = null

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
        capturedBitmap = bitmap
        _uiState.value = OcrUiState.Processing

        viewModelScope.launch {
            ocrRepository.recognizeText(bitmap)
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
        val current = _uiState.value as? OcrUiState.Success ?: return
        _uiState.value = OcrUiState.Success(draft)
    }

    fun saveDocument(onSaved: (Long) -> Unit) {
        val draft = (_uiState.value as? OcrUiState.Success)?.draft ?: return
        val bitmap = capturedBitmap ?: return
        val containerId = draft.containerId

        if (containerId == null) {
            _uiState.value = OcrUiState.Error("Vui lòng chọn container để lưu giấy tờ")
            return
        }

        viewModelScope.launch {
            val document = DocumentEntity(
                title = draft.title,
                category = draft.category ?: DocumentCategory.OTHER,
                expirationDate = draft.expiryDate,
                notes = draft.notes,
                containerId = containerId,
            )

            documentRepository.createDocument(document).fold(
                onSuccess = { documentId ->
                    imageStorageManager.saveBitmap(bitmap).fold(
                        onSuccess = { filePath ->
                            attachmentRepository.addAttachmentMetadata(
                                AttachmentEntity(
                                    fileUri = filePath,
                                    documentId = documentId,
                                    displayOrder = 0,
                                ),
                            )
                            onSaved(documentId)
                        },
                        onFailure = { error ->
                            _uiState.value = OcrUiState.Error(
                                error.message ?: "Không thể lưu ảnh đính kèm",
                            )
                        },
                    )
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
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

