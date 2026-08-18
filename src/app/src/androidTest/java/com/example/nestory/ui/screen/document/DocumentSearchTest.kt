package com.example.nestory.ui.screen.document

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.data.repository.CategoryRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.ui.theme.NestoryTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class DocumentSearchTest {
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

    private fun createViewModel(): DocumentViewModel =
        DocumentViewModel(
            documentRepository = DocumentRepositoryImpl(database.documentDao()),
            containerRepository = ContainerRepositoryImpl(database.containerDao()),
            categoryRepository = CategoryRepositoryImpl(database.categoryDao()),
            attachmentRepository = AttachmentRepositoryImpl(database.attachmentDao()),
            imageStorageManager = ImageStorageManager(InstrumentationRegistry.getInstrumentation().targetContext),
            todayProvider = { LocalDate.of(2026, 8, 14) },
        )

    private suspend fun createContainer(name: String): Long =
        database.containerDao().insert(ContainerEntity(name = name))

    @Test
    fun searchByTitle_filtersDocuments_caseInsensitive() = runBlocking {
        val containerId = createContainer("Main")
        database.documentDao().insert(
            DocumentEntity(title = "Passport", categoryId = "identity", containerId = containerId),
        )
        database.documentDao().insert(
            DocumentEntity(title = "Visa", categoryId = "identity", containerId = containerId),
        )
        val viewModel = createViewModel()
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.waitForState { it.documents.size == 2 }

            viewModel.onSearchQueryChange("passport")
            val state = viewModel.waitForState { it.documents.size == 1 }

            assertEquals("Passport", state.documents.first().name)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun searchByNote_filtersDocuments() = runBlocking {
        val containerId = createContainer("Main")
        database.documentDao().insert(
            DocumentEntity(
                title = "Contract",
                categoryId = "identity",
                containerId = containerId,
                notes = "can ho quan 7",
            ),
        )
        database.documentDao().insert(
            DocumentEntity(
                title = "Invoice",
                categoryId = "identity",
                containerId = containerId,
                notes = "hoa don dien",
            ),
        )
        val viewModel = createViewModel()
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.waitForState { it.documents.size == 2 }

            viewModel.onSearchQueryChange("can ho")
            val state = viewModel.waitForState { it.documents.size == 1 }

            assertEquals("Contract", state.documents.first().name)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun clearSearch_restoresAllDocuments() = runBlocking {
        val containerId = createContainer("Main")
        database.documentDao().insert(
            DocumentEntity(title = "Passport", categoryId = "identity", containerId = containerId),
        )
        database.documentDao().insert(
            DocumentEntity(title = "Visa", categoryId = "identity", containerId = containerId),
        )
        val viewModel = createViewModel()
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.waitForState { it.documents.size == 2 }

            viewModel.onSearchQueryChange("passport")
            viewModel.waitForState { it.documents.size == 1 }

            viewModel.onSearchQueryChange("")
            val state = viewModel.waitForState { it.documents.size == 2 }

            assertEquals(2, state.documents.size)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun searchWithNoMatch_returnsEmptyDocuments() = runBlocking {
        val containerId = createContainer("Main")
        database.documentDao().insert(
            DocumentEntity(title = "Passport", categoryId = "identity", containerId = containerId),
        )
        val viewModel = createViewModel()
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.waitForState { it.documents.isNotEmpty() }

            viewModel.onSearchQueryChange("khong ton tai")
            val state = viewModel.waitForState { it.documents.isEmpty() }

            assertTrue(state.documents.isEmpty())
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun searchNoMatch_showsNotFoundState() {
        composeRule.setContent {
            NestoryTheme {
                DocumentSelectionScreen(
                    uiState = DocumentUiState(
                        documents = emptyList(),
                        searchQuery = "xyz",
                    ),
                    onAddDocument = {},
                    onDocumentClick = {},
                    onFilterClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Không có giấy tờ phù hợp").fetchSemanticsNode()
    }

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