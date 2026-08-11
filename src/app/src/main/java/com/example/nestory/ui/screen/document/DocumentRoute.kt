package com.example.nestory.ui.screen.document

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.data.repository.ReminderRepositoryImpl
import com.example.nestory.data.settings.ExpiryReminderSettingsRepository

private enum class DocumentSubScreen {
    Selection,
    Detail,
    Filter,
}

@Composable
fun DocumentRoute(
    onAddDocument: () -> Unit,
    initialDocumentId: String? = null,
    onClearInitialId: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val documentRepository = remember { DocumentRepositoryImpl(db.documentDao()) }
    val containerRepository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val attachmentRepository = remember {
        com.example.nestory.data.repository.AttachmentRepositoryImpl(db.attachmentDao())
    }
    val imageStorageManager = remember {
        com.example.nestory.data.filesystem.ImageStorageManager(context)
    }
    val reminderRepository = remember { ReminderRepositoryImpl(db.reminderDao(), context) }
    val categoryRepository = remember {
        val database = AppDatabase.getDatabase(context)
        com.example.nestory.data.repository.CategoryRepositoryImpl(database.categoryDao())
    }
    val factory = remember {
        DocumentViewModelFactory(
            documentRepository = documentRepository,
            containerRepository = containerRepository,
            categoryRepository = categoryRepository,
            attachmentRepository = attachmentRepository,
            reminderRepository = reminderRepository,
            imageStorageManager = imageStorageManager
        )
    }
    val viewModel: DocumentViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var subScreen by remember { mutableStateOf(DocumentSubScreen.Selection) }

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

    when (subScreen) {
        DocumentSubScreen.Selection -> {
            DocumentSelectionScreen(
                uiState = uiState,
                onAddDocument = onAddDocument,
                onDocumentClick = { documentId ->
                    viewModel.selectDocument(documentId)
                    subScreen = DocumentSubScreen.Detail
                },
                onFilterClick = { subScreen = DocumentSubScreen.Filter },
                onSearchQueryChange = viewModel::onSearchQueryChange,
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
                        subScreen = DocumentSubScreen.Selection
                    },
                    onSave = { name, category, expiryDate, containerId ->
                        viewModel.updateDocument(selectedDocument.id, name, category, expiryDate, containerId)
                    },
                    onDelete = {
                        viewModel.deleteSelectedDocument {
                            subScreen = DocumentSubScreen.Selection
                        }
                    },
                )
            }
        }

        DocumentSubScreen.Filter -> {
            FilterSelectionScreen(
                onBack = { subScreen = DocumentSubScreen.Selection },
                onApply = { subScreen = DocumentSubScreen.Selection },
                onReset = {},
            )
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}
