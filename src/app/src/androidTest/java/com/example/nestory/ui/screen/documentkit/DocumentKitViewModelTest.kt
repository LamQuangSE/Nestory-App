package com.example.nestory.ui.screen.documentkit

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.data.repository.DocumentKitRepositoryImpl
import com.example.nestory.data.repository.KitItemRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DocumentKitViewModelTest {
    private lateinit var database: AppDatabase
    private lateinit var viewModel: DocumentKitViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = DocumentKitViewModel(
            DocumentKitRepositoryImpl(database.documentKitDao()),
            KitItemRepositoryImpl(database.kitItemDao()),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun newKit(name: String = "Passport Kit") = DocumentKitEntity(
        name = name,
        category = "identity",
        description = null,
        note = null,
        targetCompletionDate = "31/12/2030",
    )

    @Test
    fun createKit_savesAndAppearsInList() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.createKit(name = "Passport Kit", targetCompletionDate = "31/12/2030")
            val state = viewModel.waitForState { it.kits.isNotEmpty() }

            assertEquals(1, state.kits.size)
            assertEquals("Passport Kit", state.kits.first().kit.name)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun deleteKit_clearsSelectedKit() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.createKit(name = "Temp", targetCompletionDate = "31/12/2030")
            val created = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit
            viewModel.selectKit(created.id)
            viewModel.waitForState { it.selectedKit != null }

            viewModel.deleteKit(created)
            val state = viewModel.waitForState { it.kits.isEmpty() }

            assertTrue(state.kits.isEmpty())
            assertNull(state.selectedKit)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun linkDocument_marksItemReady_whenRequiredDocumentsSatisfied() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            val containerId = database.containerDao().insert(ContainerEntity(name = "Docs"))
            val documentId = database.documentDao().insert(
                DocumentEntity(title = "Passport", categoryId = "identity", containerId = containerId),
            )
            viewModel.createKit(name = "Passport Kit", targetCompletionDate = "31/12/2030")
            val kit = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit
            viewModel.selectKit(kit.id)

            viewModel.addItem(kitId = kit.id, name = "Passport", requiredDocuments = 1)
            val item = viewModel.waitForState { it.kitItems.isNotEmpty() }.kitItems.first()

            viewModel.linkDocument(item.id, documentId)
            val state = viewModel.waitForState {
                it.kitItems.firstOrNull()?.status == KitItemStatus.READY
            }

            assertEquals(KitItemStatus.READY, state.kitItems.first().status)
            assertEquals(documentId, state.kitItems.first().linkedDocumentId)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun linkDocument_keepsPending_whenRequiredDocumentsNotMet() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            val containerId = database.containerDao().insert(ContainerEntity(name = "Docs"))
            val documentId = database.documentDao().insert(
                DocumentEntity(title = "Passport", categoryId = "identity", containerId = containerId),
            )
            viewModel.createKit(name = "Passport Kit", targetCompletionDate = "31/12/2030")
            val kit = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit
            viewModel.selectKit(kit.id)

            viewModel.addItem(kitId = kit.id, name = "Passport", requiredDocuments = 2)
            val item = viewModel.waitForState { it.kitItems.isNotEmpty() }.kitItems.first()

            viewModel.linkDocument(item.id, documentId)
            val state = viewModel.waitForState {
                it.kitItems.firstOrNull()?.linkedDocumentId == documentId
            }

            assertEquals(KitItemStatus.PENDING, state.kitItems.first().status)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun linkDocument_keepsStatus_whenRequiredDocumentsEmpty() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            val containerId = database.containerDao().insert(ContainerEntity(name = "Docs"))
            val documentId = database.documentDao().insert(
                DocumentEntity(title = "Passport", categoryId = "identity", containerId = containerId),
            )
            viewModel.createKit(name = "Passport Kit", targetCompletionDate = "31/12/2030")
            val kit = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit
            viewModel.selectKit(kit.id)

            viewModel.addItem(kitId = kit.id, name = "Passport", requiredDocuments = null)
            val item = viewModel.waitForState { it.kitItems.isNotEmpty() }.kitItems.first()

            viewModel.linkDocument(item.id, documentId)
            val state = viewModel.waitForState {
                it.kitItems.firstOrNull()?.linkedDocumentId == documentId
            }

            assertEquals(KitItemStatus.PENDING, state.kitItems.first().status)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun unlinkDocument_returnsToPending() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            val containerId = database.containerDao().insert(ContainerEntity(name = "Docs"))
            val documentId = database.documentDao().insert(
                DocumentEntity(title = "Passport", categoryId = "identity", containerId = containerId),
            )
            viewModel.createKit(name = "Passport Kit", targetCompletionDate = "31/12/2030")
            val kit = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit
            viewModel.selectKit(kit.id)

            viewModel.addItem(kitId = kit.id, name = "Passport", requiredDocuments = 1)
            val item = viewModel.waitForState { it.kitItems.isNotEmpty() }.kitItems.first()
            viewModel.linkDocument(item.id, documentId)
            viewModel.waitForState { it.kitItems.firstOrNull()?.status == KitItemStatus.READY }

            viewModel.unlinkDocument(item.id)
            val state = viewModel.waitForState { it.kitItems.firstOrNull()?.linkedDocumentId == null }

            assertEquals(KitItemStatus.PENDING, state.kitItems.first().status)
            assertNull(state.kitItems.first().linkedDocumentId)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun updateKit_changesNameInList() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.createKit(name = "Old Name", targetCompletionDate = "31/12/2030")
            val kit = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit

            viewModel.updateKit(kit.copy(name = "New Name"))
            val state = viewModel.waitForState { it.kits.firstOrNull()?.kit?.name == "New Name" }

            assertEquals("New Name", state.kits.first().kit.name)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun addItem_appearsInKitItems() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.createKit(name = "Passport Kit", targetCompletionDate = "31/12/2030")
            val kit = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit
            viewModel.selectKit(kit.id)

            viewModel.addItem(kitId = kit.id, name = "Passport", requiredDocuments = 1)
            val state = viewModel.waitForState { it.kitItems.isNotEmpty() }

            assertEquals("Passport", state.kitItems.first().name)
            assertEquals(1, state.kitItems.first().requiredDocuments)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun updateItem_changesItemDetails() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.createKit(name = "Passport Kit", targetCompletionDate = "31/12/2030")
            val kit = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit
            viewModel.selectKit(kit.id)
            viewModel.addItem(kitId = kit.id, name = "Passport", requiredDocuments = 1)
            val item = viewModel.waitForState { it.kitItems.isNotEmpty() }.kitItems.first()

            viewModel.updateItem(item.copy(name = "Passport 2", requiredDocuments = 2))
            val state = viewModel.waitForState {
                it.kitItems.firstOrNull()?.name == "Passport 2"
            }

            assertEquals("Passport 2", state.kitItems.first().name)
            assertEquals(2, state.kitItems.first().requiredDocuments)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun removeItem_removesFromKitItems() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.createKit(name = "Passport Kit", targetCompletionDate = "31/12/2030")
            val kit = viewModel.waitForState { it.kits.isNotEmpty() }.kits.first().kit
            viewModel.selectKit(kit.id)
            viewModel.addItem(kitId = kit.id, name = "Passport", requiredDocuments = 1)
            val item = viewModel.waitForState { it.kitItems.isNotEmpty() }.kitItems.first()

            viewModel.removeItem(item)
            val state = viewModel.waitForState { it.kitItems.isEmpty() }

            assertTrue(state.kitItems.isEmpty())
        } finally {
            collection.cancel()
        }
    }

    private suspend fun DocumentKitViewModel.waitForState(
        predicate: (DocumentKitUiState) -> Boolean,
    ): DocumentKitUiState {
        repeat(100) {
            val state = uiState.value
            if (predicate(state)) return state
            delay(50)
        }
        error("Timed out waiting for expected document kit UI state. Last state=${uiState.value}")
    }
}
