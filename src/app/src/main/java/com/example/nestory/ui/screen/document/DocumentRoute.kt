package com.example.nestory.ui.screen.document

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.entity.ReminderEntity
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.data.repository.ReminderRepositoryImpl
import com.example.nestory.ui.screen.setting.ExpiryReminderSettingScreen
import com.example.nestory.ui.screen.setting.ExpiryReminderUiState
import com.example.nestory.ui.screen.setting.toExpiryReminderUiState
import com.example.nestory.ui.screen.setting.toReminderEntity
import kotlinx.coroutines.launch

private enum class DocumentSubScreen {
    Selection,
    Detail,
    Reminder,
    Filter,
    FilterCategory,
    FilterContainer,
    PdfViewer,
    EditScan
}

@Composable
fun DocumentRoute(
    onAddDocument: () -> Unit,
    initialDocumentId: String? = null,
    onClearInitialId: () -> Unit = {},
    onPdfViewerActiveChange: (Boolean) -> Unit = {},
    editLeaveRequested: Boolean = false,
    onEditLeaveComplete: () -> Unit = {},
    onEditLeaveDismiss: () -> Unit = {},
    onEditModeChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val documentRepository = remember { DocumentRepositoryImpl(db.documentDao()) }
    val containerRepository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val attachmentRepository = remember { AttachmentRepositoryImpl(db.attachmentDao()) }
    val reminderRepository = remember { ReminderRepositoryImpl(db.reminderDao(), context.applicationContext) }
    val imageStorageManager = remember { ImageStorageManager(context.applicationContext) }

    val categoryRepository = remember {
        com.example.nestory.data.repository.CategoryRepositoryImpl(db.categoryDao())
    }
    val factory = remember {
        DocumentViewModelFactory(
            documentRepository = documentRepository,
            containerRepository = containerRepository,
            categoryRepository = categoryRepository,
            attachmentRepository = attachmentRepository,
            reminderRepository = reminderRepository,
            imageStorageManager = imageStorageManager,
        )
    }
    val viewModel: DocumentViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var subScreen by remember { mutableStateOf(DocumentSubScreen.Selection) }
    var isDocumentEditActive by remember { mutableStateOf(false) }
    var reminderDocumentId by remember { mutableStateOf<Long?>(null) }
    var pdfViewerPath by remember { mutableStateOf<String?>(null) }
    var pdfViewerDocumentId by remember { mutableStateOf<Long?>(null) }
    var editScanPath by remember { mutableStateOf<String?>(null) }
    var editScanDocumentId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(isDocumentEditActive) {
        onEditModeChange(isDocumentEditActive)
    }

    DisposableEffect(Unit) {
        onDispose { onEditModeChange(false) }
    }

    LaunchedEffect(editLeaveRequested) {
        if (editLeaveRequested && isDocumentEditActive && subScreen != DocumentSubScreen.Detail) {
            subScreen = DocumentSubScreen.Detail
        }
    }

    LaunchedEffect(subScreen) {
        onPdfViewerActiveChange(subScreen == DocumentSubScreen.PdfViewer)
    }

    // Handle initial document navigation (e.g. from notification)
    LaunchedEffect(initialDocumentId) {
        if (initialDocumentId != null) {
            viewModel.selectDocument(initialDocumentId)
            subScreen = DocumentSubScreen.Detail
            onClearInitialId()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    BackHandler(
        enabled = subScreen == DocumentSubScreen.Filter ||
            subScreen == DocumentSubScreen.FilterCategory ||
            subScreen == DocumentSubScreen.FilterContainer ||
            subScreen == DocumentSubScreen.Reminder ||
            subScreen == DocumentSubScreen.PdfViewer ||
            subScreen == DocumentSubScreen.EditScan,
    ) {
        subScreen = when (subScreen) {
            DocumentSubScreen.Filter -> DocumentSubScreen.Selection
            DocumentSubScreen.FilterCategory,
            DocumentSubScreen.FilterContainer -> DocumentSubScreen.Filter
            DocumentSubScreen.Reminder -> DocumentSubScreen.Detail
            DocumentSubScreen.PdfViewer -> DocumentSubScreen.Detail
            DocumentSubScreen.EditScan -> DocumentSubScreen.Detail
            else -> subScreen
        }
    }

    when (subScreen) {
        DocumentSubScreen.Selection -> {
            DocumentSelectionScreen(
                uiState = uiState,
                onAddDocument = onAddDocument,
                onDocumentClick = { documentId ->
                    viewModel.selectDocument(documentId)
                    subScreen = DocumentSubScreen.Detail
                },
                onFilterClick = { 
                    viewModel.syncDraftWithActiveFilter()
                    subScreen = DocumentSubScreen.Filter 
                },
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onToggleFavorite = { documentId ->
                    viewModel.toggleFavorite(documentId)
                },
            )
        }

        DocumentSubScreen.Detail -> {
            val selectedDocument = uiState.selectedDocument
            if (selectedDocument == null) {
                LaunchedEffect(Unit) {
                    subScreen = DocumentSubScreen.Selection
                }
            } else {
                DocumentDetailScreen(
                    document = selectedDocument,
                    onBack = {
                        viewModel.clearSelection()
                        isDocumentEditActive = false
                        subScreen = DocumentSubScreen.Selection
                    },
                    onDelete = {
                        viewModel.deleteSelectedDocument {
                            isDocumentEditActive = false
                            subScreen = DocumentSubScreen.Selection
                        }
                    },
                    onOpenPdf = { path ->
                        pdfViewerPath = path
                        pdfViewerDocumentId = selectedDocument.id.toLongOrNull()
                        subScreen = DocumentSubScreen.PdfViewer
                    },
                    onEditScan = { path ->
                        editScanPath = path
                        editScanDocumentId = selectedDocument.id.toLongOrNull()
                        subScreen = DocumentSubScreen.EditScan
                    },
                    onSave = { name, categoryLabelValue, expiryDate, containerId, pdfFileName ->
                        viewModel.updateDocumentDetails(
                            title = name,
                            categoryLabelValue = categoryLabelValue,
                            expirationDate = expiryDate,
                            containerId = containerId,
                            pdfFileName = pdfFileName,
                        )
                    },
                    isEditMode = isDocumentEditActive,
                    onEditModeChange = { isDocumentEditActive = it },
                    isSaving = uiState.isSaving,
                    existingTitles = uiState.documents
                        .map { it.name }
                        .filter { it != selectedDocument.name },
                    editLeaveRequested = editLeaveRequested,
                    onEditLeaveComplete = onEditLeaveComplete,
                    onEditLeaveDismiss = onEditLeaveDismiss,
                    onReminderClick = {
                        reminderDocumentId = selectedDocument.id.toLongOrNull()
                        subScreen = DocumentSubScreen.Reminder
                    },
                    resolveContainerPath = { containerId ->
                        uiState.availableContainers.find { it.id == containerId }?.fullPath ?: ""
                    },
                )
            }
        }

        DocumentSubScreen.Reminder -> {
            val documentId = reminderDocumentId
            if (documentId == null) {
                LaunchedEffect(Unit) { subScreen = DocumentSubScreen.Detail }
            } else {
                val reminder by reminderRepository
                    .observeReminderByDocumentId(documentId)
                    .collectAsState(initial = null)
                var savedReminderId by remember { mutableLongStateOf(0L) }
                LaunchedEffect(reminder) { savedReminderId = reminder?.id ?: 0L }
                val expiryDate = uiState.documents
                    .firstOrNull { it.id == documentId.toString() }
                    ?.expiryDate

                ExpiryReminderSettingScreen(
                    state = (reminder ?: ReminderEntity(documentId = documentId))
                        .toExpiryReminderUiState(expiryDate),
                    onStateChange = { ui: ExpiryReminderUiState ->
                        val id = savedReminderId
                        val entity = ui.toReminderEntity(
                            documentId = documentId,
                            expiryDate = expiryDate,
                            id = id,
                        )
                        coroutineScope.launch {
                            if (id == 0L) {
                                reminderRepository.createReminder(entity)
                                    .onSuccess { newId -> savedReminderId = newId }
                            } else {
                                reminderRepository.updateReminder(entity)
                            }
                        }
                    },
                    onBack = { subScreen = DocumentSubScreen.Detail },
                )
            }
        }

        DocumentSubScreen.Filter -> {
            FilterSelectionScreen(
                uiState = uiState,
                onBack = { subScreen = DocumentSubScreen.Selection },
                onApply = { 
                    viewModel.applyFilter()
                    subScreen = DocumentSubScreen.Selection 
                },
                onReset = { viewModel.resetFilter() },
                onCategoryClick = { subScreen = DocumentSubScreen.FilterCategory },
                onContainerClick = { subScreen = DocumentSubScreen.FilterContainer },
                onFavoriteToggle = { viewModel.updateDraftFavorite(it) },
                onStatusToggle = { viewModel.toggleDraftStatus(it) }
            )
        }

        DocumentSubScreen.FilterCategory -> {
            FilterCategoryScreen(
                uiState = uiState,
                onBack = { subScreen = DocumentSubScreen.Filter },
                onCategorySelected = { category ->
                    viewModel.updateDraftCategory(category)
                    subScreen = DocumentSubScreen.Filter
                }
            )
        }

        DocumentSubScreen.FilterContainer -> {
            FilterContainerScreen(
                uiState = uiState,
                onBack = { subScreen = DocumentSubScreen.Filter },
                onContainerSelected = { containerId ->
                    viewModel.updateDraftContainer(containerId)
                    subScreen = DocumentSubScreen.Filter
                }
            )
        }

        DocumentSubScreen.PdfViewer -> {
            val path = pdfViewerPath
            val documentId = pdfViewerDocumentId
            if (path == null || documentId == null) {
                LaunchedEffect(Unit) { subScreen = DocumentSubScreen.Detail }
            } else {
                PdfViewerScreen(
                    filePath = path,
                    documentId = documentId,
                    onBack = {
                        pdfViewerPath = null
                        pdfViewerDocumentId = null
                        subScreen = DocumentSubScreen.Detail
                    },
                )
            }
        }

        DocumentSubScreen.EditScan -> {
            val path = editScanPath
            val documentId = editScanDocumentId
            if (path == null || documentId == null) {
                LaunchedEffect(Unit) { subScreen = DocumentSubScreen.Detail }
            } else {
                EditScanRoute(
                    filePath = path,
                    documentId = documentId,
                    onBack = {
                        editScanPath = null
                        editScanDocumentId = null
                        subScreen = DocumentSubScreen.Detail
                    },
                )
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}
