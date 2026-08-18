package com.example.nestory.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.data.local.entity.KitItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DocumentKitRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var kitRepository: DocumentKitRepositoryImpl
    private lateinit var itemRepository: KitItemRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        kitRepository = DocumentKitRepositoryImpl(database.documentKitDao())
        itemRepository = KitItemRepositoryImpl(database.kitItemDao())
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
        val id = kitRepository.createKit(newKit()).getOrThrow()

        val kits = kitRepository.observeAllKits().first()

        assertEquals(1, kits.size)
        assertEquals(id, kits.first().kit.id)
        assertEquals("Passport Kit", kits.first().kit.name)
    }

    @Test
    fun updateKit_persistsChanges() = runBlocking {
        val id = kitRepository.createKit(newKit("Old")).getOrThrow()

        kitRepository.updateKit(newKit("New").copy(id = id)).getOrThrow()

        val saved = kitRepository.getKitById(id).getOrThrow()
        assertEquals("New", saved?.kit?.name)
    }

    @Test
    fun deleteKit_cascadesKitItems() = runBlocking {
        val kitId = kitRepository.createKit(newKit()).getOrThrow()
        itemRepository.addItem(
            KitItemEntity(status = "PENDING", documentKitId = kitId, linkedDocumentId = null, name = "Item A"),
        ).getOrThrow()
        itemRepository.addItem(
            KitItemEntity(status = "PENDING", documentKitId = kitId, linkedDocumentId = null, name = "Item B"),
        ).getOrThrow()

        kitRepository.deleteKit(newKit().copy(id = kitId)).getOrThrow()

        val kits = kitRepository.getAllKits().getOrThrow()
        val items = database.kitItemDao().observeItemsByKit(kitId).first()
        assertTrue(kits.isEmpty())
        assertTrue(items.isEmpty())
    }

    @Test
    fun addItem_appearsUnderSelectedKit() = runBlocking {
        val kitId = kitRepository.createKit(newKit()).getOrThrow()
        val itemId = itemRepository.addItem(
            KitItemEntity(
                status = "PENDING",
                documentKitId = kitId,
                linkedDocumentId = null,
                name = "ID Card",
                requiredDocuments = 1,
            ),
        ).getOrThrow()

        val kit = kitRepository.getKitById(kitId).getOrThrow()

        assertEquals(1, kit?.items?.size)
        assertEquals(itemId, kit?.items?.first()?.id)
    }

    @Test
    fun updateItem_persistsChanges() = runBlocking {
        val kitId = kitRepository.createKit(newKit()).getOrThrow()
        val itemId = itemRepository.addItem(
            KitItemEntity(status = "PENDING", documentKitId = kitId, linkedDocumentId = null, name = "Old"),
        ).getOrThrow()
        val item = database.kitItemDao().getItemById(itemId)!!

        itemRepository.updateItem(item.copy(name = "New")).getOrThrow()

        assertEquals("New", database.kitItemDao().getItemById(itemId)?.name)
    }

    @Test
    fun deleteItem_removesItem() = runBlocking {
        val kitId = kitRepository.createKit(newKit()).getOrThrow()
        val itemId = itemRepository.addItem(
            KitItemEntity(status = "PENDING", documentKitId = kitId, linkedDocumentId = null, name = "Gone"),
        ).getOrThrow()
        val item = database.kitItemDao().getItemById(itemId)!!

        itemRepository.deleteItem(item).getOrThrow()

        val kit = kitRepository.getKitById(kitId).getOrThrow()
        assertTrue(kit?.items.isNullOrEmpty())
    }

    @Test
    fun deleteDocument_clearsLinkedDocumentIdOnKitItem() = runBlocking {
        val containerId = database.containerDao().insert(ContainerEntity(name = "Docs"))
        val documentId = database.documentDao().insert(
            DocumentEntity(title = "Linked", categoryId = "general", containerId = containerId),
        )
        val kitId = kitRepository.createKit(newKit()).getOrThrow()
        val itemId = itemRepository.addItem(
            KitItemEntity(
                status = "PENDING",
                documentKitId = kitId,
                linkedDocumentId = documentId,
                name = "Linked item",
            ),
        ).getOrThrow()

        database.documentDao().delete(
            DocumentEntity(id = documentId, title = "Linked", categoryId = "general", containerId = containerId),
        )

        val item = database.kitItemDao().getItemById(itemId)
        assertEquals(null, item?.linkedDocumentId)
    }
}
