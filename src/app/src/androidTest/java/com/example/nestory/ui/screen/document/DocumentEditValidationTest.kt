package com.example.nestory.ui.screen.document

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.CategoryEntity
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.data.repository.CategoryRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.ui.theme.NestoryTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class DocumentEditValidationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: AppDatabase
    private lateinit var documentRepository: DocumentRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        documentRepository = DocumentRepositoryImpl(database.documentDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun updateDocument_withModifiedFields_persistsChangesById() {
        runBlocking {
            val firstContainerId = createContainer("First")
            val secondContainerId = createContainer("Second")
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Original Title",
                    categoryId = "identity",
                    expirationDate = "31/12/2030",
                    notes = "Old notes",
                    containerId = firstContainerId,
                ),
            ).getOrThrow()

            val original = documentRepository.getDocumentById(documentId).getOrThrow()!!
            documentRepository.updateDocument(
                original.copy(
                    title = "Updated Title",
                    categoryId = "contract",
                    expirationDate = "01/01/2031",
                    notes = "Updated notes",
                    containerId = secondContainerId,
                ),
            ).getOrThrow()

            val saved = documentRepository.getDocumentById(documentId).getOrThrow()!!

            assertEquals("Updated Title", saved.title)
            assertEquals("contract", saved.categoryId)
            assertEquals("01/01/2031", saved.expirationDate)
            assertEquals("Updated notes", saved.notes)
            assertEquals(secondContainerId, saved.containerId)
        }
    }

    @Test
    fun updateDocumentDetails_blankTitleAndNoExpiry_preservesTitleAndClearsExpiry() {
        runBlocking {
            val containerId = createContainer("Main")
            database.categoryDao().insertCategory(
                CategoryEntity(id = "identity", name = "Identity", colorValue = 0xFF1855EE),
            )
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Original Title",
                    categoryId = "identity",
                    expirationDate = "31/12/2030",
                    containerId = containerId,
                ),
            ).getOrThrow()
            val viewModel = createViewModel()
            val collection = launch { viewModel.uiState.collect() }

            try {
                viewModel.selectDocument(documentId.toString())
                viewModel.waitForState { it.selectedDocument?.id == documentId.toString() }

                viewModel.updateDocumentDetails(
                    title = "",
                    categoryLabelValue = "Identity",
                    expirationDate = "Chưa có hạn",
                )

                val saved = waitForDocument(documentId) {
                    it.title == "Original Title" && it.expirationDate == null
                }
                assertEquals("Original Title", saved.title)
                assertNull(saved.expirationDate)
            } finally {
                collection.cancel()
            }
        }
    }

    @Test
    fun updateDocumentDetails_unknownCategoryLabel_keepsOriginalCategoryId() {
        runBlocking {
            val containerId = createContainer("Main")
            database.categoryDao().insertCategory(
                CategoryEntity(id = "identity", name = "Identity", colorValue = 0xFF1855EE),
            )
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Original Title",
                    categoryId = "identity",
                    expirationDate = "31/12/2030",
                    containerId = containerId,
                ),
            ).getOrThrow()
            val viewModel = createViewModel()
            val collection = launch { viewModel.uiState.collect() }

            try {
                viewModel.selectDocument(documentId.toString())
                viewModel.waitForState { it.selectedDocument?.id == documentId.toString() }

                viewModel.updateDocumentDetails(
                    title = "Renamed Title",
                    categoryLabelValue = "Unknown Category",
                    expirationDate = "01/01/2031",
                )

                val saved = waitForDocument(documentId) { it.title == "Renamed Title" }
                assertEquals("identity", saved.categoryId)
                assertEquals("Renamed Title", saved.title)
            } finally {
                collection.cancel()
            }
        }
    }

    @Test
    fun toggleFavorite_twice_updatesDocumentAndUiState() {
        runBlocking {
            val containerId = createContainer("Main")
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Favorite Document",
                    categoryId = "identity",
                    containerId = containerId,
                    isFavorite = false,
                ),
            ).getOrThrow()
            val viewModel = createViewModel()
            val collection = launch { viewModel.uiState.collect() }

            try {
                viewModel.selectDocument(documentId.toString())
                viewModel.waitForState { it.selectedDocument?.id == documentId.toString() }

                viewModel.toggleFavorite()
                viewModel.waitForState { it.selectedDocument?.isFavorite == true }
                assertTrue(documentRepository.getDocumentById(documentId).getOrThrow()!!.isFavorite)

                viewModel.toggleFavorite()
                viewModel.waitForState { it.selectedDocument?.isFavorite == false }
                assertFalse(documentRepository.getDocumentById(documentId).getOrThrow()!!.isFavorite)
            } finally {
                collection.cancel()
            }
        }
    }

    @Test
    fun updateDocumentLocation_validTarget_updatesContainerAndUiPath() {
        runBlocking {
            val sourceContainerId = createContainer("Source")
            val targetParentId = createContainer("Target Parent")
            val targetChildId = createContainer("Target Child", parentId = targetParentId)
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Movable Document",
                    categoryId = "identity",
                    containerId = sourceContainerId,
                ),
            ).getOrThrow()

            documentRepository.updateDocumentLocation(documentId, targetChildId).getOrThrow()
            val saved = documentRepository.getDocumentById(documentId).getOrThrow()!!

            assertEquals(targetChildId, saved.containerId)
            assertEquals(
                "Target Parent > Target Child",
                buildContainerPath(saved.containerId, database.containerDao().getAllContainers()),
            )
        }
    }

    @Test
    fun updateDocumentExpiryDate_canSetAndClearDate() {
        runBlocking {
            val containerId = createContainer("Main")
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Expiry Document",
                    categoryId = "identity",
                    containerId = containerId,
                ),
            ).getOrThrow()

            documentRepository.updateDocumentExpiryDate(documentId, "15/09/2030").getOrThrow()
            assertEquals(
                "15/09/2030",
                documentRepository.getDocumentById(documentId).getOrThrow()?.expirationDate,
            )

            documentRepository.updateDocumentExpiryDate(documentId, null).getOrThrow()
            assertNull(documentRepository.getDocumentById(documentId).getOrThrow()?.expirationDate)
        }
    }

    @Test
    fun confirmLeavingEditMode_savesChangesAndContinuesPendingNavigation() {
        var requestLeave: () -> Unit = {}
        var savedName: String? = null
        var editMode = true
        var leaveCompleted = false

        composeRule.setContent {
            NestoryTheme {
                var editLeaveRequested by remember { mutableStateOf(false) }
                var isEditMode by remember { mutableStateOf(true) }
                requestLeave = { editLeaveRequested = true }
                editMode = isEditMode

                DocumentDetailScreen(
                    document = editableUiDocument(),
                    onBack = {},
                    onSave = { name, _, _, _, _ -> savedName = name },
                    isEditMode = isEditMode,
                    onEditModeChange = { isEditMode = it },
                    editLeaveRequested = editLeaveRequested,
                    onEditLeaveComplete = { leaveCompleted = true },
                    onEditLeaveDismiss = { editLeaveRequested = false },
                )
            }
        }

        composeRule.onNodeWithText("Original Title").performTextClearance()
        composeRule.onNodeWithText("").performTextInput("Updated Title")
        composeRule.runOnIdle { requestLeave() }
        composeRule.onNodeWithText("Xác nhận dừng chỉnh sửa giấy tờ").fetchSemanticsNode()
        composeRule.onNodeWithText("Có").performClick()

        composeRule.runOnIdle {
            assertEquals("Updated Title", savedName)
            assertFalse(editMode)
            assertTrue(leaveCompleted)
        }
    }

    @Test
    fun dismissLeavingEditMode_keepsUserOnEditScreenWithoutSaving() {
        var requestLeave: () -> Unit = {}
        var savedName: String? = null
        var editMode = true
        var leaveDismissed = false

        composeRule.setContent {
            NestoryTheme {
                var editLeaveRequested by remember { mutableStateOf(false) }
                var isEditMode by remember { mutableStateOf(true) }
                requestLeave = { editLeaveRequested = true }
                editMode = isEditMode

                DocumentDetailScreen(
                    document = editableUiDocument(),
                    onBack = {},
                    onSave = { name, _, _, _, _ -> savedName = name },
                    isEditMode = isEditMode,
                    onEditModeChange = { isEditMode = it },
                    editLeaveRequested = editLeaveRequested,
                    onEditLeaveComplete = {},
                    onEditLeaveDismiss = {
                        editLeaveRequested = false
                        leaveDismissed = true
                    },
                )
            }
        }

        composeRule.onNodeWithText("Original Title").performTextClearance()
        composeRule.onNodeWithText("").performTextInput("Unsaved Title")
        composeRule.runOnIdle { requestLeave() }
        composeRule.onNodeWithText("Không").performClick()

        composeRule.runOnIdle {
            assertNull(savedName)
            assertTrue(editMode)
            assertTrue(leaveDismissed)
        }
        composeRule.onNodeWithText("Chỉnh sửa giấy tờ").fetchSemanticsNode()
    }

    @Test
    fun documentEntityRequiresTitleCategoryAndContainerAtConstruction() {
        val document = DocumentEntity(
            title = "Required Fields",
            categoryId = "identity",
            containerId = 1L,
        )

        assertEquals("Required Fields", document.title)
        assertEquals("identity", document.categoryId)
        assertEquals(1L, document.containerId)
    }

    @Test
    fun createDocument_missingContainerReferenceFailsAndSavesNothing() {
        runBlocking {
            val result = documentRepository.createDocument(
                DocumentEntity(
                    title = "Invalid Document",
                    categoryId = "identity",
                    containerId = 404L,
                ),
            )

            assertTrue(result.isFailure)
            assertTrue(documentRepository.getAllDocuments().getOrThrow().isEmpty())
        }
    }

    @Test
    fun createDocument_duplicatePrimaryKeyFailsAndKeepsOriginalOnly() {
        runBlocking {
            val containerId = createContainer("Main")
            documentRepository.createDocument(
                DocumentEntity(
                    id = 42L,
                    title = "Original Document",
                    categoryId = "identity",
                    containerId = containerId,
                ),
            ).getOrThrow()

            val duplicateResult = documentRepository.createDocument(
                DocumentEntity(
                    id = 42L,
                    title = "Duplicate Document",
                    categoryId = "identity",
                    containerId = containerId,
                ),
            )
            val documents = documentRepository.getAllDocuments().getOrThrow()

            assertTrue(duplicateResult.isFailure)
            assertEquals(1, documents.size)
            assertEquals("Original Document", documents.first().title)
        }
    }

    @Test
    fun updateDocumentLocation_missingContainerFailsAndKeepsOriginalContainer() {
        runBlocking {
            val originalContainerId = createContainer("Main")
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Document",
                    categoryId = "identity",
                    containerId = originalContainerId,
                ),
            ).getOrThrow()

            val result = documentRepository.updateDocumentLocation(documentId, 404L)
            val saved = documentRepository.getDocumentById(documentId).getOrThrow()!!

            assertTrue(result.isFailure)
            assertEquals(originalContainerId, saved.containerId)
        }
    }

    private fun createViewModel(): DocumentViewModel =
        DocumentViewModel(
            documentRepository = DocumentRepositoryImpl(database.documentDao()),
            containerRepository = ContainerRepositoryImpl(database.containerDao()),
            categoryRepository = CategoryRepositoryImpl(database.categoryDao()),
            attachmentRepository = AttachmentRepositoryImpl(database.attachmentDao()),
            imageStorageManager = ImageStorageManager(InstrumentationRegistry.getInstrumentation().targetContext),
            todayProvider = { LocalDate.of(2026, 8, 14) },
        )

    private suspend fun createContainer(
        name: String,
        parentId: Long? = null,
    ): Long = database.containerDao().insert(ContainerEntity(name = name, parentId = parentId))

    private suspend fun DocumentViewModel.waitForState(
        predicate: (DocumentUiState) -> Boolean,
    ): DocumentUiState {
        repeat(100) {
            val state = uiState.value
            if (predicate(state)) return state
            delay(50)
        }
        error("Timed out waiting for expected document UI state. Last state=${uiState.value}")
    }

    private suspend fun waitForDocument(
        documentId: Long,
        predicate: (DocumentEntity) -> Boolean,
    ): DocumentEntity {
        repeat(100) {
            val document = documentRepository.getDocumentById(documentId).getOrThrow()
            if (document != null && predicate(document)) return document
            delay(50)
        }
        error("Timed out waiting for expected document. Last document=${documentRepository.getDocumentById(documentId).getOrThrow()}")
    }

    private fun editableUiDocument(): DocumentUiModel =
        DocumentUiModel(
            id = "1",
            name = "Original Title",
            category = "Identity",
            containerPath = "Main",
            containerId = 1L,
            status = DocumentStatus.Active,
            expiryDate = "31/12/2030",
            categoryColor = Color(0xFF1855EE),
        )
}
