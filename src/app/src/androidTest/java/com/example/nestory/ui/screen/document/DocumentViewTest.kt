package com.example.nestory.ui.screen.document

import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DocumentViewTest {
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
