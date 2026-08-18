package com.example.nestory.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContainerRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ContainerRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ContainerRepositoryImpl(database.containerDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createContainer_savesAndAppearsInList() = runBlocking {
        val id = repository.createContainer(ContainerEntity(name = "Main")).getOrThrow()

        val containers = repository.observeAllContainers().first()

        assertEquals(1, containers.size)
        assertEquals("Main", containers.first().name)
        assertEquals(id, containers.first().id)
    }

    @Test
    fun createContainer_withParent_createsNestedChild() = runBlocking {
        val parentId = repository.createContainer(ContainerEntity(name = "Parent")).getOrThrow()
        val childId = repository.createContainer(
            ContainerEntity(name = "Child", parentId = parentId),
        ).getOrThrow()

        val children = repository.getChildContainers(parentId).getOrThrow()

        assertEquals(1, children.size)
        assertEquals(childId, children.first().id)
        assertEquals(parentId, children.first().parentId)
    }

    @Test
    fun createContainer_duplicateName_isRejected() = runBlocking {
        repository.createContainer(ContainerEntity(name = "Duplicated")).getOrThrow()

        val result = repository.createContainer(ContainerEntity(name = "Duplicated"))

        assertTrue(result.isFailure)
    }

    @Test
    fun updateContainer_renamesContainer() = runBlocking {
        val id = repository.createContainer(ContainerEntity(name = "Old")).getOrThrow()

        repository.updateContainer(ContainerEntity(id = id, name = "New")).getOrThrow()

        val saved = repository.getContainerById(id).getOrThrow()
        assertEquals("New", saved?.name)
    }

    @Test
    fun deleteContainer_notReferenced_succeeds() = runBlocking {
        val id = repository.createContainer(ContainerEntity(name = "Empty")).getOrThrow()

        repository.deleteContainer(ContainerEntity(id = id, name = "Empty")).getOrThrow()

        val containers = repository.getAllContainers().getOrThrow()
        assertTrue(containers.isEmpty())
    }

    @Test
    fun deleteContainer_referencedByDocument_failsWithForeignKey() = runBlocking {
        val containerId = repository.createContainer(ContainerEntity(name = "Busy")).getOrThrow()
        database.documentDao().insert(
            DocumentEntity(title = "Doc", categoryId = "general", containerId = containerId),
        )

        val result = repository.deleteContainer(ContainerEntity(id = containerId, name = "Busy"))

        assertTrue(result.isFailure)
        val containers = repository.getAllContainers().getOrThrow()
        assertEquals(1, containers.size)
    }

    @Test
    fun deleteContainer_thatIsParent_failsWithForeignKey() = runBlocking {
        val parentId = repository.createContainer(ContainerEntity(name = "Parent")).getOrThrow()
        repository.createContainer(ContainerEntity(name = "Child", parentId = parentId)).getOrThrow()

        val result = repository.deleteContainer(ContainerEntity(id = parentId, name = "Parent"))

        assertTrue(result.isFailure)
    }

    @Test
    fun getContainerPath_buildsNestedPath() = runBlocking {
        val parentId = repository.createContainer(ContainerEntity(name = "Parent")).getOrThrow()
        val childId = repository.createContainer(ContainerEntity(name = "Child", parentId = parentId)).getOrThrow()

        val path = repository.getContainerPath(childId)

        assertEquals(2, path.size)
        assertEquals("Parent", path[0].name)
        assertEquals("Child", path[1].name)
    }
}
