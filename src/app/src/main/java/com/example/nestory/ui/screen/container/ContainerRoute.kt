package com.example.nestory.ui.screen.container

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.repository.ContainerRepositoryImpl

enum class ContainerSubScreen { Selection, Create, Edit }

@Composable
fun ContainerRoute(
    onBack: () -> Unit,
    onConfirmSelection: ((ContainerEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val repository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val factory = remember { ContainerViewModelFactory(repository) }
    val viewModel: ContainerViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    var subScreen by remember { mutableStateOf(ContainerSubScreen.Selection) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<ContainerEntity?>(null) }

    BackHandler {
        when {
            showDeleteDialog -> {
                showDeleteDialog = false
                deleteTarget = null
            }
            subScreen == ContainerSubScreen.Selection -> onBack()
            else -> subScreen = ContainerSubScreen.Selection
        }
    }

    when (subScreen) {
        ContainerSubScreen.Selection -> {
            ContainerSelectionScreen(
                uiState = uiState,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSelectContainer = { viewModel.selectContainer(it) },
                onToggleContainer = { viewModel.toggleContainer(it) },
                onCreateClick = { subScreen = ContainerSubScreen.Create },
                onEditClick = { subScreen = ContainerSubScreen.Edit },
                onConfirmClick = {
                    if (onConfirmSelection != null) {
                        uiState.allContainers.find { it.id == uiState.selectedContainerId }?.let {
                            onConfirmSelection(it)
                        }
                    } else {
                        onBack()
                    }
                },
                onDeleteClick = { id ->
                    val container = uiState.allContainers.find { it.id == id }
                    deleteTarget = container
                    showDeleteDialog = true
                },
                onCloseBreadcrumb = { viewModel.clearSelection() },
                onBackClick = onBack,
                errorMessage = uiState.errorMessage,
                onDismissError = { viewModel.clearError() }
            )
        }

        ContainerSubScreen.Create -> {
            CreateContainerScreen(
                parentContainerName = uiState.containerPath.lastOrNull()?.name ?: "",
                onBackClick = { subScreen = ContainerSubScreen.Selection },
                onCreate = { name ->
                    viewModel.createContainer(name, uiState.selectedContainerId)
                    subScreen = ContainerSubScreen.Selection
                },
                errorMessage = uiState.errorMessage,
                onDismissError = { viewModel.clearError() }
            )
        }

        ContainerSubScreen.Edit -> {
            val selectedContainer = uiState.allContainers.find { it.id == uiState.selectedContainerId }
            EditContainerScreen(
                initialName = selectedContainer?.name ?: "",
                onBackClick = { subScreen = ContainerSubScreen.Selection },
                onSave = { name ->
                    selectedContainer?.let { viewModel.updateContainer(it.copy(name = name)) }
                    subScreen = ContainerSubScreen.Selection
                },
                errorMessage = uiState.errorMessage,
                onDismissError = { viewModel.clearError() }
            )
        }
    }

    if (showDeleteDialog && deleteTarget != null) {
        DeleteContainerDialog(
            containerName = deleteTarget!!.name,
            onConfirmDelete = {
                deleteTarget?.let { viewModel.deleteContainer(it) }
                showDeleteDialog = false
                deleteTarget = null
            },
            onDismiss = {
                showDeleteDialog = false
                deleteTarget = null
            }
        )
    }
}
