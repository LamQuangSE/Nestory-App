package com.example.nestory.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DocumentRepositoryCreateTest {
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
    fun createDocument_withValidContainer_savesAndAppearsInList() = runBlocking {
        val containerId = createContainer("Main folder")
        val document = DocumentEntity(
            title = "Passport",
            categoryId = "identity",
            expirationDate = "31/12/2030",
            notes = "Primary travel document",
            containerId = containerId,
        )

        val createdId = documentRepository.createDocument(document).getOrThrow()
        val documents = documentRepository.getAllDocuments().getOrThrow()

        assertEquals(1, documents.size)
        assertEquals(createdId, documents.first().id)
        assertEquals("Passport", documents.first().title)
        assertEquals("identity", documents.first().categoryId)
        assertEquals("31/12/2030", documents.first().expirationDate)
        assertEquals("Primary travel document", documents.first().notes)
        assertEquals(containerId, documents.first().containerId)
    }

    @Test
    fun createDocument_withOptionalOcrMetadata_preservesMetadataWhenQueriedById() = runBlocking {
        val containerId = createContainer("OCR folder")
        val createdId = documentRepository.createDocument(
            DocumentEntity(
                title = "Citizen ID",
                categoryId = "identity",
                expirationDate = "20/08/2031",
                notes = "OCR import",
                issueDate = "20/08/2021",
                holderName = "Nguyen Van A",
                documentNumber = "012345678901",
                ocrText = "CONG HOA XA HOI CHU NGHIA VIET NAM",
                containerId = containerId,
            ),
        ).getOrThrow()

        val saved = documentRepository.getDocumentById(createdId).getOrThrow()

        assertNotNull(saved)
        assertEquals("20/08/2021", saved?.issueDate)
        assertEquals("Nguyen Van A", saved?.holderName)
        assertEquals("012345678901", saved?.documentNumber)
        assertEquals("CONG HOA XA HOI CHU NGHIA VIET NAM", saved?.ocrText)
    }

    @Test
    fun getAllDocuments_afterCreatingMultipleDocuments_returnsDocumentsOrderedByTitle() = runBlocking {
        val containerId = createContainer("Sorted folder")
        listOf("Warranty", "Contract", "Bill").forEach { title ->
            documentRepository.createDocument(
                DocumentEntity(
                    title = title,
                    categoryId = "general",
                    containerId = containerId,
                ),
            ).getOrThrow()
        }

        val titles = documentRepository.getAllDocuments().getOrThrow().map { it.title }

        assertEquals(listOf("Bill", "Contract", "Warranty"), titles)
        assertTrue(titles == titles.sorted())
    }

    private suspend fun createContainer(name: String): Long =
        database.containerDao().insert(ContainerEntity(name = name))
}
