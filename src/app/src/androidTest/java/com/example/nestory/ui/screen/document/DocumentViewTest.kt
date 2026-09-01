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
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.AttachmentEntity
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class DocumentViewTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun tapDocumentListItem_opensDetailWithCoreFields() {
        val document = DocumentUiModel(
            id = "1",
            name = "Travel Passport",
            category = "Identity",
            containerPath = "Parent > Child",
            containerId = 2L,
            status = DocumentStatus.Active,
            expiryDate = "31/12/2030",
            categoryColor = Color(0xFF1855EE),
            attachmentUris = listOf("file:///storage/emulated/0/Documents/passport.pdf"),
        )

        composeRule.setContent {
            NestoryTheme {
                var selectedDocument by remember { mutableStateOf<DocumentUiModel?>(null) }
                val selected = selectedDocument
                if (selected == null) {
                    DocumentSelectionScreen(
                        uiState = DocumentUiState(documents = listOf(document)),
                        onAddDocument = {},
                        onDocumentClick = { selectedDocument = document },
                        onFilterClick = {},
                    )
                } else {
                    DocumentDetailScreen(
                        document = selected,
                        onBack = { selectedDocument = null },
                    )
                }
            }
        }

        composeRule.onNodeWithText("Travel Passport").performClick()

        composeRule.onNodeWithText("Thông tin chính").fetchSemanticsNode()
        composeRule.onNodeWithText("Travel Passport").fetchSemanticsNode()
        composeRule.onNodeWithText("Identity").fetchSemanticsNode()
        composeRule.onNodeWithText("31/12/2030").fetchSemanticsNode()
        composeRule.onNodeWithText("Còn hiệu lực").fetchSemanticsNode()
        composeRule.onNodeWithText("Child").fetchSemanticsNode()
        composeRule.onNodeWithText("Parent > Child").fetchSemanticsNode()
        composeRule.onNodeWithText("Tệp scan").fetchSemanticsNode()
        composeRule.onNodeWithText("passport.pdf").fetchSemanticsNode()
    }

    @Test
    fun documentWithMissingCategory_usesOtherFallbackAndDoesNotCrash() {
        runBlocking {
            val containerId = createContainer("Main folder")
            val documentId = database.documentDao().insert(
                DocumentEntity(
                    title = "Uncategorized Document",
                    categoryId = "missing-category",
                    containerId = containerId,
                ),
            )
            val viewModel = createViewModel()
            val collection = launch { viewModel.uiState.collect() }

            try {
                val state = viewModel.waitForState { it.documents.any { document -> document.id == documentId.toString() } }
                val document = state.documents.first { it.id == documentId.toString() }

                assertEquals("Khác", document.category)
                assertEquals(Color(0xFF717171), document.categoryColor)
            } finally {
                collection.cancel()
            }
        }
    }

    @Test
    fun documentInNestedContainer_usesFullParentChildPathInUiModel() {
        runBlocking {
            val parentId = createContainer("Parent")
            val childId = createContainer("Child", parentId = parentId)
            val documentId = database.documentDao().insert(
                DocumentEntity(
                    title = "Nested Document",
                    categoryId = "identity",
                    containerId = childId,
                ),
            )
            database.categoryDao().insertCategory(
                CategoryEntity(id = "identity", name = "Identity", colorValue = 0xFF1855EE),
            )
            val viewModel = createViewModel()
            val collection = launch { viewModel.uiState.collect() }

            try {
                viewModel.selectDocument(documentId.toString())
                val state = viewModel.waitForState { it.selectedDocument?.id == documentId.toString() }

                assertEquals("Parent > Child", state.selectedDocument?.containerPath)
            } finally {
                collection.cancel()
            }
        }
    }

    @Test
    fun documentDetailDisplaysAttachmentFileNamesInDisplayOrder() {
        runBlocking {
            val containerId = createContainer("Scan folder")
            val documentId = database.documentDao().insert(
                DocumentEntity(
                    title = "Scanned Document",
                    categoryId = "general",
                    containerId = containerId,
                ),
            )
            database.attachmentDao().insert(
                AttachmentEntity(
                    fileUri = "file:///storage/emulated/0/Nestory/back.pdf",
                    documentId = documentId,
                    displayOrder = 1,
                ),
            )
            database.attachmentDao().insert(
                AttachmentEntity(
                    fileUri = "file:///storage/emulated/0/Nestory/front.jpg",
                    documentId = documentId,
                    displayOrder = 0,
                ),
            )
            val viewModel = createViewModel()
            val collection = launch { viewModel.uiState.collect() }

            try {
                val state = viewModel.waitForState { uiState ->
                    uiState.documents.firstOrNull { it.id == documentId.toString() }?.attachmentUris?.size == 2
                }
                val document = state.documents.first { it.id == documentId.toString() }

                assertEquals(
                    listOf(
                        "file:///storage/emulated/0/Nestory/front.jpg",
                        "file:///storage/emulated/0/Nestory/back.pdf",
                    ),
                    document.attachmentUris,
                )

                composeRule.setContent {
                    NestoryTheme {
                        DocumentDetailScreen(
                            document = document,
                            onBack = {},
                        )
                    }
                }

                // Only the PDF is shown as the actual scanned file; the first
                // image is rendered as the top preview (image, not a text node).
                composeRule.onNodeWithText("back.pdf").fetchSemanticsNode()
                composeRule.onNodeWithText("front.jpg").assertDoesNotExist()
            } finally {
                collection.cancel()
            }
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
}
