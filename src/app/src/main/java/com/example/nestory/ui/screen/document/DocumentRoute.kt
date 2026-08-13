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
import androidx.room.Room
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
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
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "nestory_database",
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }
    val documentRepository = remember { DocumentRepositoryImpl(db.documentDao()) }
    val containerRepository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val settingsRepository = remember {
        ExpiryReminderSettingsRepository(context.applicationContext)
    }
    val factory = remember {
        DocumentViewModelFactory(
            documentRepository = documentRepository,
            containerRepository = containerRepository,
            expiryReminderSettings = settingsRepository.settings,
        )
    }
    val viewModel: DocumentViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var subScreen by remember { mutableStateOf(DocumentSubScreen.Selection) }

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
