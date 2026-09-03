package com.example.nestory.ui.screen.container

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.repository.ContainerRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContainerViewModelTest {
    private lateinit var database: AppDatabase
    private lateinit var viewModel: ContainerViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = ContainerViewModel(ContainerRepositoryImpl(database.containerDao()))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createContainer_duplicateName_showsErrorMessage() = runBlocking {
        viewModel.createContainer("Main", null)
        viewModel.waitForState { it.allContainers.isNotEmpty() }

        viewModel.createContainer("Main", null)
        val state = viewModel.waitForState { it.errorMessage != null }

        assertEquals("Tên container đã tồn tại", state.errorMessage)
    }

    @Test
    fun createContainer_duplicateNameUnderDifferentParents_appearsInList() = runBlocking {
        var firstParentId: Long? = null
        var secondParentId: Long? = null
        viewModel.createContainer("Parent A", null) { firstParentId = it }
        viewModel.waitForState { it.allContainers.size == 1 }
        viewModel.createContainer("Parent B", null) { secondParentId = it }
        viewModel.waitForState { it.allContainers.size == 2 }

        viewModel.createContainer("Shared", firstParentId)
        viewModel.waitForState { it.allContainers.size == 3 }
        viewModel.createContainer("Shared", secondParentId)
        val state = viewModel.waitForState { it.allContainers.size == 4 }

        assertEquals(2, state.allContainers.count { it.name == "Shared" })
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun createContainer_duplicateNameUnderSameParent_showsErrorMessage() = runBlocking {
        var parentId: Long? = null
        viewModel.createContainer("Parent", null) { parentId = it }
        viewModel.waitForState { it.allContainers.isNotEmpty() }
        viewModel.createContainer("Shared", parentId)
        viewModel.waitForState { it.allContainers.size == 2 }

        viewModel.createContainer("Shared", parentId)
        val state = viewModel.waitForState { it.errorMessage != null }

        assertEquals("Tên container đã tồn tại", state.errorMessage)
    }

    @Test
    fun deleteContainer_referencedByDocument_showsErrorMessage() = runBlocking {
        val containerId = database.containerDao().insert(ContainerEntity(name = "Busy"))
        database.documentDao().insert(
            DocumentEntity(title = "Doc", categoryId = "general", containerId = containerId),
        )
        viewModel.waitForState { it.allContainers.isNotEmpty() }

        viewModel.deleteContainer(ContainerEntity(id = containerId, name = "Busy"))
        val state = viewModel.waitForState { it.errorMessage != null }

        assertEquals("Không thể xóa container đang chứa dữ liệu", state.errorMessage)
    }

    @Test
    fun selectContainer_buildsNestedPath() = runBlocking {
        val parentId = database.containerDao().insert(ContainerEntity(name = "Parent"))
        val childId = database.containerDao().insert(ContainerEntity(name = "Child", parentId = parentId))
        viewModel.waitForState { it.allContainers.size == 2 }

        viewModel.selectContainer(childId)
        val state = viewModel.waitForState { it.containerPath.size == 2 }

        assertEquals(listOf("Parent", "Child"), state.containerPath.map { it.name })
    }

    @Test
    fun toggleContainer_expandsAndCollapses() = runBlocking {
        val id = database.containerDao().insert(ContainerEntity(name = "Folder"))
        viewModel.waitForState { it.allContainers.isNotEmpty() }

        viewModel.toggleContainer(id)
        assertEquals(true, viewModel.uiState.value.isExpanded(id))

        viewModel.toggleContainer(id)
        assertEquals(false, viewModel.uiState.value.isExpanded(id))
    }

    @Test
    fun createContainer_validName_appearsInListAndInvokesOnCreated() = runBlocking {
        var createdId: Long? = null
        viewModel.createContainer("Hồ sơ gia đình", null) { createdId = it }

        val state = viewModel.waitForState { it.allContainers.isNotEmpty() }

        assertEquals(1, state.allContainers.size)
        assertEquals("Hồ sơ gia đình", state.allContainers.first().name)
        assertEquals(state.allContainers.first().id, createdId)
    }

    @Test
    fun createContainer_withParent_createsNestedChild() = runBlocking {
        var parentId: Long? = null
        viewModel.createContainer("Tủ tài liệu", null) { parentId = it }
        viewModel.waitForState { it.allContainers.isNotEmpty() }

        viewModel.createContainer("Ngăn 1", parentId)
        val state = viewModel.waitForState { it.allContainers.size == 2 }

        val child = state.allContainers.first { it.name == "Ngăn 1" }
        assertEquals(parentId, child.parentId)
    }

    @Test
    fun updateContainer_duplicateName_showsErrorMessage() = runBlocking {
        val firstId = database.containerDao().insert(ContainerEntity(name = "Main"))
        val secondId = database.containerDao().insert(ContainerEntity(name = "Docs"))
        viewModel.waitForState { it.allContainers.size == 2 }

        viewModel.updateContainer(ContainerEntity(id = secondId, name = "Main"))
        val state = viewModel.waitForState { it.errorMessage != null }

        assertEquals("Tên container đã tồn tại", state.errorMessage)
    }

    @Test
    fun deleteContainer_unreferenced_removesFromList() = runBlocking {
        val id = database.containerDao().insert(ContainerEntity(name = "Empty"))
        viewModel.waitForState { it.allContainers.isNotEmpty() }

        viewModel.deleteContainer(ContainerEntity(id = id, name = "Empty"))
        val state = viewModel.waitForState { it.allContainers.isEmpty() }

        assertTrue(state.allContainers.isEmpty())
    }

    private suspend fun ContainerViewModel.waitForState(
        predicate: (ContainerUiState) -> Boolean,
    ): ContainerUiState {
        repeat(100) {
            val state = uiState.value
            if (predicate(state)) return state
            delay(50)
        }
        error("Timed out waiting for expected container UI state. Last state=${uiState.value}")
    }
}
