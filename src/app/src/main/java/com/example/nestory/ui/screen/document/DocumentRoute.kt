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
    FilterCategory,
    FilterContainer
}

@Composable
fun DocumentRoute(
    onAddDocument: () -> Unit,
) {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "nestory_database",
        ).addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
        ).build()
    }
    val documentRepository = remember { DocumentRepositoryImpl(db.documentDao()) }
    val containerRepository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val settingsRepository = remember {
        ExpiryReminderSettingsRepository(context.applicationContext)
    }
    
    val categoryRepository = remember { 
        com.example.nestory.data.repository.CategoryRepositoryImpl(db.categoryDao()) 
    }
    val factory = remember {
        DocumentViewModelFactory(
            documentRepository = documentRepository,
            containerRepository = containerRepository,
            categoryRepository = categoryRepository, // Thêm dòng này
            expiryReminderSettings = settingsRepository.settings,
        )
    }
    val viewModel: DocumentViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var subScreen by remember { mutableStateOf(DocumentSubScreen.Selection) }

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
                onFilterClick = { 
                    viewModel.syncDraftWithActiveFilter()
                    subScreen = DocumentSubScreen.Filter 
                },
                onSearchQueryChange = viewModel::onSearchQueryChange,
            )
        }

        DocumentSubScreen.Detail -> {
            // Logic Detail giữ nguyên...
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
    }

    SnackbarHost(hostState = snackbarHostState)
}