package com.example.nestory.ui.screen.document

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.AttachmentEntity
import com.example.nestory.data.local.entity.CategoryEntity
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.data.repository.CategoryRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.domain.model.DocumentFilter
import com.example.nestory.domain.model.ExpiryReminderSettings
import com.example.nestory.domain.repository.AttachmentRepository
import com.example.nestory.domain.repository.CategoryRepository
import com.example.nestory.domain.repository.ContainerRepository
import com.example.nestory.domain.repository.DocumentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DocumentDeleteTest {
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
    fun deleteExistingDocument_removesDocumentFromDatabaseAndList() {
        runBlocking {
            val containerId = createContainer("Main")
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Delete Me",
                    categoryId = "identity",
                    containerId = containerId,
                ),
            ).getOrThrow()
            val document = documentRepository.getDocumentById(documentId).getOrThrow()

            assertNotNull(document)
            documentRepository.deleteDocument(document!!).getOrThrow()

            assertNull(documentRepository.getDocumentById(documentId).getOrThrow())
            assertTrue(documentRepository.getAllDocuments().getOrThrow().isEmpty())
        }
    }

    @Test
    fun deleteDocument_cascadesAttachmentMetadata() {
        runBlocking {
            val containerId = createContainer("Main")
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Document With Scans",
                    categoryId = "identity",
                    containerId = containerId,
                ),
            ).getOrThrow()
            database.attachmentDao().insert(
                AttachmentEntity(
                    fileUri = "file:///scan/front.jpg",
                    documentId = documentId,
                    displayOrder = 0,
                ),
            )
            database.attachmentDao().insert(
                AttachmentEntity(
                    fileUri = "file:///scan/back.jpg",
                    documentId = documentId,
                    displayOrder = 1,
                ),
            )
            val document = documentRepository.getDocumentById(documentId).getOrThrow()!!

            documentRepository.deleteDocument(document).getOrThrow()

            assertTrue(documentRepository.getAllDocuments().getOrThrow().isEmpty())
            assertTrue(database.attachmentDao().getAllAttachments().isEmpty())
            assertTrue(database.attachmentDao().getByDocumentId(documentId).isEmpty())
        }
    }

    @Test
    fun selectedDocumentDeletedBeforeDetailResolves_clearsSelectedDocumentState() {
        runBlocking {
            val containerId = createContainer("Main")
            val documentId = documentRepository.createDocument(
                DocumentEntity(
                    title = "Race Delete",
                    categoryId = "identity",
                    containerId = containerId,
                ),
            ).getOrThrow()
            val viewModel = createRealViewModel()
            val collection = launch { viewModel.uiState.collect() }

            try {
                viewModel.selectDocument(documentId.toString())
                viewModel.waitForState { it.selectedDocument?.id == documentId.toString() }

                val document = documentRepository.getDocumentById(documentId).getOrThrow()!!
                documentRepository.deleteDocument(document).getOrThrow()

                val state = viewModel.waitForState { uiState ->
                    uiState.selectedDocument == null &&
                        uiState.documents.none { it.id == documentId.toString() }
                }
                assertNull(state.selectedDocument)
                assertFalse(state.documents.any { it.id == documentId.toString() })
            } finally {
                collection.cancel()
            }
        }
    }

    @Test
    fun deleteSelectedDocument_whenRepositoryDeleteFailsKeepsSelectionAndShowsError() {
        runBlocking {
            val document = DocumentEntity(
                id = 7L,
                title = "Protected Document",
                categoryId = "identity",
                containerId = 1L,
            )
            val documentRepository = FailingDeleteDocumentRepository(document)
            val viewModel = DocumentViewModel(
                documentRepository = documentRepository,
                containerRepository = StaticContainerRepository(ContainerEntity(id = 1L, name = "Main")),
                categoryRepository = StaticCategoryRepository(
                    CategoryEntity(id = "identity", name = "Identity", colorValue = 0xFF1855EE),
                ),
                attachmentRepository = StaticAttachmentRepository(),
                expiryReminderSettings = flowOf(ExpiryReminderSettings()),
                todayProvider = { LocalDate.of(2026, 8, 14) },
            )
            val collection = launch { viewModel.uiState.collect() }
            var onDeletedCalled = false

            try {
                viewModel.selectDocument(document.id.toString())
                viewModel.waitForState { it.selectedDocument?.id == document.id.toString() }

                viewModel.deleteSelectedDocument { onDeletedCalled = true }

                val state = viewModel.waitForState {
                    it.errorMessage == "Delete failed" &&
                        it.selectedDocument?.id == document.id.toString()
                }

                assertEquals("Delete failed", state.errorMessage)
                assertEquals(document.id.toString(), state.selectedDocument?.id)
                assertFalse(onDeletedCalled)
            } finally {
                collection.cancel()
            }
        }
    }

    private fun createRealViewModel(): DocumentViewModel =
        DocumentViewModel(
            documentRepository = DocumentRepositoryImpl(database.documentDao()),
            containerRepository = ContainerRepositoryImpl(database.containerDao()),
            categoryRepository = CategoryRepositoryImpl(database.categoryDao()),
            attachmentRepository = AttachmentRepositoryImpl(database.attachmentDao()),
            expiryReminderSettings = flowOf(ExpiryReminderSettings()),
            todayProvider = { LocalDate.of(2026, 8, 14) },
        )

    private suspend fun createContainer(name: String): Long =
        database.containerDao().insert(ContainerEntity(name = name))

    private suspend fun DocumentViewModel.waitForState(
        predicate: (DocumentUiState) -> Boolean,
    ): DocumentUiState {
        repeat(100) {
            val state = uiState.value
            if (predicate(state)) return state
            delay(50)
        }
        error("Timed out waiting for expected document delete state. Last state=${uiState.value}")
    }
}

private class FailingDeleteDocumentRepository(
    document: DocumentEntity,
) : DocumentRepository {
    private val documents = MutableStateFlow(listOf(document))

    override fun observeAllDocuments(): Flow<List<DocumentEntity>> = documents

    override suspend fun getAllDocuments(): Result<List<DocumentEntity>> =
        Result.success(documents.value)

    override fun observeDocumentById(documentId: Long): Flow<DocumentEntity?> =
        MutableStateFlow(documents.value.firstOrNull { it.id == documentId })

    override suspend fun getDocumentById(documentId: Long): Result<DocumentEntity?> =
        Result.success(documents.value.firstOrNull { it.id == documentId })

    override fun observeDocumentsByContainer(containerId: Long): Flow<List<DocumentEntity>> =
        MutableStateFlow(documents.value.filter { it.containerId == containerId })

    override suspend fun getDocumentsByContainer(containerId: Long): Result<List<DocumentEntity>> =
        Result.success(documents.value.filter { it.containerId == containerId })

    override fun searchDocuments(query: String): Flow<List<DocumentEntity>> = documents

    override fun filterDocuments(filter: DocumentFilter): Flow<List<DocumentEntity>> = documents

    override suspend fun createDocument(document: DocumentEntity): Result<Long> =
        Result.failure(UnsupportedOperationException("Not needed"))

    override suspend fun updateDocument(document: DocumentEntity): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not needed"))

    override suspend fun deleteDocument(document: DocumentEntity): Result<Unit> =
        Result.failure(IllegalStateException("Delete failed"))

    override suspend fun updateFavoriteStatus(documentId: Long, isFavorite: Boolean): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not needed"))

    override suspend fun updateDocumentLocation(documentId: Long, containerId: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not needed"))

    override suspend fun updateDocumentExpiryDate(documentId: Long, expirationDate: String?): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not needed"))
}

private class StaticContainerRepository(
    private val container: ContainerEntity,
) : ContainerRepository {
    override fun observeAllContainers(): Flow<List<ContainerEntity>> = flowOf(listOf(container))
    override suspend fun getAllContainers(): Result<List<ContainerEntity>> = Result.success(listOf(container))
    override fun observeContainerById(containerId: Long): Flow<ContainerEntity?> = flowOf(container.takeIf { it.id == containerId })
    override suspend fun getContainerById(containerId: Long): Result<ContainerEntity?> = Result.success(container.takeIf { it.id == containerId })
    override fun observeChildContainers(parentId: Long?): Flow<List<ContainerEntity>> = flowOf(emptyList())
    override suspend fun getChildContainers(parentId: Long?): Result<List<ContainerEntity>> = Result.success(emptyList())
    override suspend fun getContainerPath(containerId: Long): List<ContainerEntity> = listOf(container).filter { it.id == containerId }
    override suspend fun createContainer(container: ContainerEntity): Result<Long> = Result.failure(UnsupportedOperationException("Not needed"))
    override suspend fun updateContainer(container: ContainerEntity): Result<Unit> = Result.failure(UnsupportedOperationException("Not needed"))
    override suspend fun deleteContainer(container: ContainerEntity): Result<Unit> = Result.failure(UnsupportedOperationException("Not needed"))
}

private class StaticCategoryRepository(
    private val category: CategoryEntity,
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<CategoryEntity>> = flowOf(listOf(category))
    override suspend fun insertCategory(category: CategoryEntity) = Unit
    override suspend fun updateCategory(category: CategoryEntity) = Unit
    override suspend fun deleteCategory(categoryId: String) = Unit
}

private class StaticAttachmentRepository : AttachmentRepository {
    override fun observeAllAttachments(): Flow<List<AttachmentEntity>> = flowOf(emptyList())
    override fun observeAttachmentsByDocumentId(documentId: Long): Flow<List<AttachmentEntity>> = flowOf(emptyList())
    override suspend fun getAttachmentById(attachmentId: Long): Result<AttachmentEntity?> = Result.success(null)
    override suspend fun getAttachmentsByDocumentId(documentId: Long): Result<List<AttachmentEntity>> = Result.success(emptyList())
    override suspend fun addAttachmentMetadata(attachment: AttachmentEntity): Result<Long> = Result.failure(UnsupportedOperationException("Not needed"))
    override suspend fun updateAttachmentMetadata(attachment: AttachmentEntity): Result<Unit> = Result.failure(UnsupportedOperationException("Not needed"))
    override suspend fun deleteAttachmentMetadata(attachment: AttachmentEntity): Result<Unit> = Result.failure(UnsupportedOperationException("Not needed"))
}
