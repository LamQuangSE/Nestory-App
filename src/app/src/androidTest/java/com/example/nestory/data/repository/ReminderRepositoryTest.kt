package com.example.nestory.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ReminderRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ReminderRepositoryImpl(database.reminderDao(), context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createReminder_savesDocumentReminderAndReturnsId() = runBlocking {
        val documentId = createDocument()
        val id = repository.createReminder(
            ReminderEntity(
                documentId = documentId,
                isEnabled = false,
                reminderDate = "01/01/2099",
                reminderTime = "09:00",
            ),
        ).getOrThrow()

        val saved = repository.getReminderByDocumentId(documentId).getOrThrow()

        assertNotNull(saved)
        assertEquals(id, saved?.id)
        assertEquals(documentId, saved?.documentId)
    }

    @Test
    fun createReminder_savesKitReminder() = runBlocking {
        val kitId = createKit()
        val id = repository.createReminder(
            ReminderEntity(
                documentKitId = kitId,
                isEnabled = false,
                reminderDate = "01/01/2099",
                reminderTime = "09:00",
            ),
        ).getOrThrow()

        val saved = repository.getReminderByDocumentKitId(kitId).getOrThrow()

        assertNotNull(saved)
        assertEquals(id, saved?.id)
        assertEquals(kitId, saved?.documentKitId)
    }

    @Test
    fun updateReminder_persistsChanges() = runBlocking {
        val documentId = createDocument()
        val id = repository.createReminder(
            ReminderEntity(
                documentId = documentId,
                isEnabled = false,
                reminderDate = "01/01/2099",
                reminderTime = "09:00",
            ),
        ).getOrThrow()
        val reminder = database.reminderDao().getById(id)!!

        repository.updateReminder(reminder.copy(isEnabled = true, reminderTime = "18:30")).getOrThrow()

        val saved = database.reminderDao().getById(id)
        assertEquals(true, saved?.isEnabled)
        assertEquals("18:30", saved?.reminderTime)
    }

    @Test
    fun deleteReminder_removesRow() = runBlocking {
        val documentId = createDocument()
        val id = repository.createReminder(
            ReminderEntity(documentId = documentId, isEnabled = false),
        ).getOrThrow()
        val reminder = database.reminderDao().getById(id)!!

        repository.deleteReminder(reminder).getOrThrow()

        assertNull(database.reminderDao().getById(id))
    }

    @Test
    fun observeReminderByDocumentId_emitsMatchingReminder() = runBlocking {
        val documentId = createDocument()
        repository.createReminder(
            ReminderEntity(
                documentId = documentId,
                isEnabled = false,
                reminderDate = "01/01/2099",
                reminderTime = "09:00",
            ),
        ).getOrThrow()

        val reminder = repository.observeReminderByDocumentId(documentId).first()

        assertNotNull(reminder)
        assertEquals(documentId, reminder?.documentId)
    }

    private suspend fun createDocument(): Long {
        val containerId = database.containerDao().insert(ContainerEntity(name = "Docs"))
        return database.documentDao().insert(
            DocumentEntity(title = "Doc", categoryId = "general", containerId = containerId),
        )
    }

    private suspend fun createKit(): Long =
        database.documentKitDao().insertKit(
            DocumentKitEntity(
                name = "Kit",
                category = null,
                description = null,
                note = null,
                targetCompletionDate = "31/12/2030",
            ),
        )
}
