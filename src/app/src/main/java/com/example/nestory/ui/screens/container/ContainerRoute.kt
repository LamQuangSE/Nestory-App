package com.example.nestory.ui.screens.container

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.nestory.data.database.AppDatabase
import com.example.nestory.data.entity.ContainerEntity
import com.example.nestory.data.repository.ContainerRepositoryImpl

enum class ContainerSubScreen { Selection, Create, Edit }

@Composable
fun ContainerRoute(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "nestory_database"
        ).build()
    }
    val repository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val factory = remember { ContainerViewModelFactory(repository) }
    val viewModel: ContainerViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    var subScreen by remember { mutableStateOf(ContainerSubScreen.Selection) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<ContainerEntity?>(null) }

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
                onConfirmClick = onBack,
                onDeleteClick = { id ->
                    val container = uiState.allContainers.find { it.id == id }
                    deleteTarget = container
                    showDeleteDialog = true
                },
                onCloseBreadcrumb = { viewModel.clearSelection() },
                onBackClick = onBack
            )
        }

        ContainerSubScreen.Create -> {
            CreateContainerScreen(
                parentContainerName = uiState.containerPath.lastOrNull()?.name ?: "",
                onBackClick = { subScreen = ContainerSubScreen.Selection },
                onCreate = { name ->
                    viewModel.createContainer(name, uiState.selectedContainerId)
                    subScreen = ContainerSubScreen.Selection
                }
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
                }
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
