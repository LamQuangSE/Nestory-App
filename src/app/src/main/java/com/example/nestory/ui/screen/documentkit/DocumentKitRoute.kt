package com.example.nestory.ui.screen.documentkit

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
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentKitRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.data.repository.KitItemRepositoryImpl
import com.example.nestory.domain.model.ExpiryReminderSettings
import com.example.nestory.ui.screen.document.DocumentDetailScreen
import com.example.nestory.ui.screen.document.DocumentUiModel
import com.example.nestory.ui.screen.document.buildContainerPath
import com.example.nestory.ui.screen.document.calculateDocumentStatus
import com.example.nestory.ui.screen.document.categoryColor
import com.example.nestory.ui.screen.document.categoryLabel

enum class DocumentKitSubScreen {
    List,
    Create,
    Detail,
    ItemList,
    ItemCreate,
    ItemDetail,
    LinkedDocDetail,
}

@Composable
fun DocumentKitRoute(
    onBack: () -> Unit,
    onScanDocument: (Long?) -> Unit = {},
) {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "nestory_database"
        ).addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
        ).build()
    }
    val documentKitRepository = remember { DocumentKitRepositoryImpl(db.documentKitDao()) }
    val kitItemRepository = remember { KitItemRepositoryImpl(db.kitItemDao()) }
    val documentRepository = remember { DocumentRepositoryImpl(db.documentDao()) }
    val containerRepository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val factory = remember {
        DocumentKitViewModelFactory(documentKitRepository, kitItemRepository)
    }
    val viewModel: DocumentKitViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    val allDocuments by documentRepository.observeAllDocuments().collectAsState(initial = emptyList())
    val containers by containerRepository.observeAllContainers().collectAsState(initial = emptyList())
    val documentsById = remember(allDocuments) { allDocuments.associateBy { it.id } }

    var subScreen by remember { mutableStateOf(DocumentKitSubScreen.List) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedKitId by remember { mutableStateOf<Long?>(null) }
    var editingKit by remember { mutableStateOf<DocumentKitEntity?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingForm by remember { mutableStateOf<FormData?>(null) }

    var selectedItemId by remember { mutableStateOf<Long?>(null) }
    var editingItem by remember { mutableStateOf<KitItemEntity?>(null) }
    var showItemConfirmDialog by remember { mutableStateOf(false) }
    var pendingItemForm by remember { mutableStateOf<ItemFormData?>(null) }

    var showLinkSourceSheet by remember { mutableStateOf(false) }
    var showUnlinkConfirmDialog by remember { mutableStateOf(false) }
    var showDocumentPicker by remember { mutableStateOf(false) }
    var linkingItemId by remember { mutableStateOf<Long?>(null) }
    var viewingDocumentId by remember { mutableStateOf<Long?>(null) }
    var linkedDocReturnSubScreen by remember { mutableStateOf(DocumentKitSubScreen.ItemDetail) }

    val selectedKit = selectedKitId?.let { id -> uiState.kits.find { it.kit.id == id } }
    val kitItems = selectedKit?.items ?: emptyList()
    val selectedItem = selectedItemId?.let { id -> kitItems.find { it.id == id } }

    val progressState = remember(selectedKit, documentsById, uiState.error) {
        buildKitProgressUiState(
            kit = selectedKit,
            documentsById = documentsById,
            isLoading = false,
            error = uiState.error,
        )
    }

    val freshEditingItem = editingItem?.let { stale ->
        uiState.kits.asSequence()
            .flatMap { it.items.asSequence() }
            .firstOrNull { it.id == stale.id } ?: stale
    }

    val viewingDocument = viewingDocumentId?.let { id -> allDocuments.find { it.id == id } }
    val viewingDocumentUi = viewingDocument?.let { doc ->
        DocumentUiModel(
            id = doc.id.toString(),
            name = doc.title,
            category = categoryLabel(doc.category),
            containerPath = buildContainerPath(doc.containerId, containers),
            containerId = doc.containerId,
            status = calculateDocumentStatus(doc.expirationDate, ExpiryReminderSettings()),
            expiryDate = doc.expirationDate ?: "Chưa có hạn",
            categoryColor = categoryColor(doc.category),
        )
    }

    when (subScreen) {
        DocumentKitSubScreen.List -> {
            DocumentKitListScreen(
                kits = uiState.kits,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onKitClick = { kitId ->
                    selectedKitId = kitId
                    viewModel.selectKit(kitId)
                    subScreen = DocumentKitSubScreen.Detail
                },
                onToggleFavorite = { kitId ->
                    viewModel.toggleFavorite(kitId)
                },
                onCreateKitClick = {
                    editingKit = null
                    subScreen = DocumentKitSubScreen.Create
                }
            )
        }

        DocumentKitSubScreen.Create -> {
            DocumentKitCreateScreen(
                onBackClick = {
                    subScreen = DocumentKitSubScreen.List
                },
                onSubmit = { name, date, category, description, note ->
                    if (editingKit == null) {
                        viewModel.createKit(
                            name = name,
                            description = description,
                            targetCompletionDate = date,
                            category = category,
                            note = note,
                        )
                        subScreen = DocumentKitSubScreen.List
                    } else {
                        val kit = editingKit!!
                        viewModel.updateKit(
                            kit.copy(
                                name = name,
                                category = category,
                                description = description,
                                note = note,
                                targetCompletionDate = date,
                            )
                        )
                        editingKit = null
                        subScreen = DocumentKitSubScreen.Detail
                    }
                },
                onEditBack = { name, date, category, description, note ->
                    pendingForm = FormData(name, date, category, description, note)
                    showConfirmDialog = true
                },
                initialName = editingKit?.name.orEmpty(),
                initialDate = editingKit?.targetCompletionDate.orEmpty(),
                initialCategory = editingKit?.category,
                initialDescription = editingKit?.description.orEmpty(),
                initialNote = editingKit?.note.orEmpty(),
                submitLabel = if (editingKit == null) "Tạo bộ hồ sơ mới" else "Lưu thay đổi",
                isEdit = editingKit != null,
                onDelete = editingKit?.let { kit ->
                    {
                        viewModel.deleteKit(kit)
                        editingKit = null
                        subScreen = DocumentKitSubScreen.List
                    }
                }
            )
        }

        DocumentKitSubScreen.Detail -> {
            if (selectedKit != null) {
                DocumentKitDetailScreen(
                    state = progressState,
                    onBackClick = { subScreen = DocumentKitSubScreen.List },
                    onEditClick = {
                        editingKit = selectedKit.kit
                        subScreen = DocumentKitSubScreen.Create
                    },
                    onViewAllItemsClick = {
                        subScreen = DocumentKitSubScreen.ItemList
                    },
                    onRetry = { viewModel.clearError() }
                )
            }
        }

        DocumentKitSubScreen.ItemList -> {
            if (selectedKit != null) {
                DocumentKitItemListScreen(
                    items = kitItems,
                    onBackClick = { subScreen = DocumentKitSubScreen.Detail },
                    onItemClick = { itemId ->
                        selectedItemId = itemId
                        subScreen = DocumentKitSubScreen.ItemDetail
                    },
                    onCreateItemClick = {
                        editingItem = null
                        subScreen = DocumentKitSubScreen.ItemCreate
                    }
                )
            }
        }

        DocumentKitSubScreen.ItemCreate -> {
            DocumentKitItemFormScreen(
                onBackClick = {
                    subScreen = if (editingItem == null) {
                        DocumentKitSubScreen.ItemList
                    } else {
                        DocumentKitSubScreen.ItemDetail
                    }
                },
                onSubmit = { name, description, note, requiredDocuments ->
                    if (editingItem == null) {
                        selectedKitId?.let { kitId ->
                            viewModel.addItem(
                                kitId = kitId,
                                name = name,
                                description = description,
                                note = note,
                                requiredDocuments = requiredDocuments,
                            )
                        }
                        subScreen = DocumentKitSubScreen.ItemList
                    } else {
                        val item = freshEditingItem!!
                        viewModel.updateItem(
                            item.copy(
                                name = name,
                                description = description,
                                note = note,
                                requiredDocuments = requiredDocuments,
                            )
                        )
                        editingItem = null
                        subScreen = DocumentKitSubScreen.ItemDetail
                    }
                },
                onEditBack = { name, description, note, requiredDocuments ->
                    pendingItemForm = ItemFormData(name, description, note, requiredDocuments)
                    showItemConfirmDialog = true
                },
                onDelete = editingItem?.let { item ->
                    {
                        viewModel.removeItem(item)
                        editingItem = null
                        subScreen = DocumentKitSubScreen.ItemList
                    }
                },
                initialName = editingItem?.name.orEmpty(),
                initialDescription = editingItem?.description.orEmpty(),
                initialNote = editingItem?.note.orEmpty(),
                initialRequiredDocuments = editingItem?.requiredDocuments?.toString().orEmpty(),
                isEdit = editingItem != null,
                linkedDocumentCount = if (freshEditingItem?.linkedDocumentId == null) 0 else 1,
                linkedDocumentTitle = freshEditingItem?.linkedDocumentId?.let { id ->
                    allDocuments.find { it.id == id }?.title
                },
                onAddLinkedDocumentClick = {
                    freshEditingItem?.let {
                        linkingItemId = it.id
                        showLinkSourceSheet = true
                    }
                },
                onLinkedDocumentClick = {
                    freshEditingItem?.linkedDocumentId?.let { documentId ->
                        viewingDocumentId = documentId
                        linkedDocReturnSubScreen = DocumentKitSubScreen.ItemCreate
                        subScreen = DocumentKitSubScreen.LinkedDocDetail
                    }
                },
                onRemoveLinkedDocumentClick = {
                    if (freshEditingItem?.linkedDocumentId != null) {
                        linkingItemId = freshEditingItem!!.id
                        showUnlinkConfirmDialog = true
                    }
                }
            )
        }

        DocumentKitSubScreen.ItemDetail -> {
            if (selectedKit != null && selectedItem != null) {
                DocumentKitItemDetailScreen(
                    item = selectedItem,
                    onBackClick = { subScreen = DocumentKitSubScreen.ItemList },
                    onEditClick = {
                        editingItem = selectedItem
                        subScreen = DocumentKitSubScreen.ItemCreate
                    },
                    linkedDocumentTitle = selectedItem.linkedDocumentId?.let { id ->
                        allDocuments.find { it.id == id }?.title
                    },
                    onLinkedDocumentClick = {
                        selectedItem.linkedDocumentId?.let { documentId ->
                            viewingDocumentId = documentId
                            linkedDocReturnSubScreen = DocumentKitSubScreen.ItemDetail
                            subScreen = DocumentKitSubScreen.LinkedDocDetail
                        }
                    }
                )
            }
        }

        DocumentKitSubScreen.LinkedDocDetail -> {
            val document = viewingDocumentUi
            if (document == null) {
                LaunchedEffect(Unit) { subScreen = linkedDocReturnSubScreen }
            } else {
                DocumentDetailScreen(
                    document = document,
                    readOnly = true,
                    onBack = {
                        viewingDocumentId = null
                        subScreen = linkedDocReturnSubScreen
                    },
                )
            }
        }
    }

    if (showConfirmDialog && pendingForm != null && editingKit != null) {
        ConfirmEditKitDialog(
            onConfirm = {
                val form = pendingForm!!
                val kit = editingKit!!
                viewModel.updateKit(
                    kit.copy(
                        name = form.name,
                        category = form.category,
                        description = form.description,
                        note = form.note,
                        targetCompletionDate = form.date,
                    )
                )
                showConfirmDialog = false
                pendingForm = null
                editingKit = null
                subScreen = DocumentKitSubScreen.Detail
            },
            onDismiss = {
                showConfirmDialog = false
                pendingForm = null
                editingKit = null
                subScreen = DocumentKitSubScreen.Detail
            }
        )
    }

    if (showItemConfirmDialog && pendingItemForm != null) {
        if (editingItem == null) {
            ConfirmCreateItemDialog(
                onConfirm = {
                    showItemConfirmDialog = false
                    pendingItemForm = null
                },
                onDismiss = {
                    showItemConfirmDialog = false
                    pendingItemForm = null
                    subScreen = DocumentKitSubScreen.ItemList
                }
            )
        } else {
            ConfirmEditItemDialog(
                onConfirm = {
                    val form = pendingItemForm!!
                    val item = freshEditingItem!!
                    viewModel.updateItem(
                        item.copy(
                            name = form.name,
                            description = form.description,
                            note = form.note,
                            requiredDocuments = form.requiredDocuments,
                        )
                    )
                    showItemConfirmDialog = false
                    pendingItemForm = null
                    editingItem = null
                    subScreen = DocumentKitSubScreen.ItemDetail
                },
                onDismiss = {
                    showItemConfirmDialog = false
                    pendingItemForm = null
                    editingItem = null
                    subScreen = DocumentKitSubScreen.ItemDetail
                }
            )
        }
    }

    val linkItemId = linkingItemId
    if (linkItemId != null) {
        if (showLinkSourceSheet) {
            KitLinkSourceSheet(
                onDismiss = { showLinkSourceSheet = false },
                onScanClick = {
                    showLinkSourceSheet = false
                    onScanDocument(linkItemId)
                },
                onPickSavedClick = {
                    showLinkSourceSheet = false
                    showDocumentPicker = true
                }
            )
        }
        if (showDocumentPicker) {
            KitLinkDocumentPickerSheet(
                documents = allDocuments,
                onDismiss = { showDocumentPicker = false },
                onSelect = { document: DocumentEntity ->
                    showDocumentPicker = false
                    viewModel.linkDocument(linkItemId, document.id)
                }
            )
        }
    }

    if (showUnlinkConfirmDialog) {
        ConfirmUnlinkDocumentDialog(
            onConfirm = {
                showUnlinkConfirmDialog = false
                linkingItemId?.let { viewModel.unlinkDocument(it) }
            },
            onDismiss = { showUnlinkConfirmDialog = false }
        )
    }
}

private data class FormData(
    val name: String,
    val date: String,
    val category: String?,
    val description: String?,
    val note: String?,
)

private data class ItemFormData(
    val name: String,
    val description: String,
    val note: String,
    val requiredDocuments: Int?,
)
