package com.example.nestory.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.domain.model.DocumentFilter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DocumentFilterTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: DocumentRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DocumentRepositoryImpl(database.documentDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun filterDocuments_byCategory_returnsOnlyMatchingCategory() = runBlocking {
        val containerId = createContainer("Main")
        createDocument(containerId, title = "Hộ chiếu", categoryId = "identity")
        createDocument(containerId, title = "Hóa đơn", categoryId = "finance")

        val result = repository.filterDocuments(DocumentFilter(categoryId = "finance")).first()

        assertEquals(listOf("Hóa đơn"), result.map { it.title })
    }

    @Test
    fun filterDocuments_byFavorite_returnsOnlyMatchingFlag() = runBlocking {
        val containerId = createContainer("Main")
        createDocument(containerId, title = "Yêu thích", categoryId = "identity", isFavorite = true)
        createDocument(containerId, title = "Không yêu thích", categoryId = "identity", isFavorite = false)

        val favorites = repository.filterDocuments(DocumentFilter(isFavorite = true)).first()
        val nonFavorites = repository.filterDocuments(DocumentFilter(isFavorite = false)).first()

        assertEquals(listOf("Yêu thích"), favorites.map { it.title })
        assertEquals(listOf("Không yêu thích"), nonFavorites.map { it.title })
    }

    @Test
    fun filterDocuments_byContainer_returnsOnlyMatchingContainer() = runBlocking {
        val containerA = createContainer("Tủ A")
        val containerB = createContainer("Tủ B")
        createDocument(containerA, title = "Trong A", categoryId = "identity")
        createDocument(containerB, title = "Trong B", categoryId = "identity")

        val result = repository.filterDocuments(DocumentFilter(containerId = containerB)).first()

        assertEquals(listOf("Trong B"), result.map { it.title })
    }

    @Test
    fun filterDocuments_byCombinedCriteria_appliesAllFilters() = runBlocking {
        val containerA = createContainer("Tủ A")
        val containerB = createContainer("Tủ B")
        createDocument(containerA, title = "A-Identity-Fav", categoryId = "identity", isFavorite = true)
        createDocument(containerA, title = "A-Finance-Fav", categoryId = "finance", isFavorite = true)
        createDocument(containerB, title = "B-Identity-Fav", categoryId = "identity", isFavorite = true)

        val result = repository.filterDocuments(
            DocumentFilter(categoryId = "identity", isFavorite = true, containerId = containerA),
        ).first()

        assertEquals(listOf("A-Identity-Fav"), result.map { it.title })
    }

    @Test
    fun filterDocuments_withNoCriteria_returnsAllDocuments() = runBlocking {
        val containerId = createContainer("Main")
        createDocument(containerId, title = "A", categoryId = "identity")
        createDocument(containerId, title = "B", categoryId = "finance")

        val result = repository.filterDocuments(DocumentFilter()).first()

        assertEquals(2, result.size)
    }

    private suspend fun createDocument(
        containerId: Long,
        title: String,
        categoryId: String,
        isFavorite: Boolean = false,
    ): Long = database.documentDao().insert(
        DocumentEntity(
            title = title,
            categoryId = categoryId,
            containerId = containerId,
            isFavorite = isFavorite,
        ),
    )

    private suspend fun createContainer(name: String): Long =
        database.containerDao().insert(ContainerEntity(name = name))
}