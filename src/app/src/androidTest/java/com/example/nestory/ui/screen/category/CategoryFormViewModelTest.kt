package com.example.nestory.ui.screen.category

import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.CategoryRepositoryImpl
import kotlinx.coroutines.delay
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

class CategoryFormViewModelTest {
    private lateinit var database: AppDatabase
    private lateinit var viewModel: CategoryViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = CategoryViewModel(CategoryRepositoryImpl(database.categoryDao()))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun submitForm_blankName_showsNameRequiredError() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.onEvent(CategoryEvent.OnAddClick)

            viewModel.onEvent(CategoryEvent.OnSubmitForm)
            val state = viewModel.waitForState { it.form.nameError != null }

            assertEquals("Tên giấy tờ không được để trống", state.form.nameError)
            assertTrue(state.form.colorError == null)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun submitForm_duplicateName_showsDuplicateError() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.onEvent(CategoryEvent.OnNameChanged("Giấy tờ cá nhân"))
            viewModel.submitForm { }
            val state = viewModel.waitForState { it.categories.isNotEmpty() }

            viewModel.onEvent(CategoryEvent.OnAddClick)
            viewModel.onEvent(CategoryEvent.OnNameChanged("Giấy tờ cá nhân"))
            viewModel.onEvent(CategoryEvent.OnSubmitForm)
            val errorState = viewModel.waitForState { it.form.nameError != null }

            assertEquals("Tên 'Giấy tờ cá nhân' đã tồn tại", errorState.form.nameError)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun submitForm_duplicateColor_showsColorError() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.onEvent(CategoryEvent.OnAddClick)
            val color = viewModel.waitForState { it.form.selectedColor != null }.form.selectedColor!!

            viewModel.onEvent(CategoryEvent.OnNameChanged("Thứ nhất"))
            viewModel.onEvent(CategoryEvent.OnSubmitForm)
            viewModel.waitForState { it.categories.isNotEmpty() }

            viewModel.onEvent(CategoryEvent.OnAddClick)
            viewModel.onEvent(CategoryEvent.OnNameChanged("Thứ hai"))
            viewModel.onEvent(CategoryEvent.OnColorSelected(color))
            viewModel.onEvent(CategoryEvent.OnSubmitForm)
            val state = viewModel.waitForState { it.form.colorError != null }

            assertEquals("Màu sắc đã tồn tại", state.form.colorError)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun submitForm_validForm_createsCategoryAndInvokesOnCreated() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            var createdName: String? = null
            viewModel.onEvent(CategoryEvent.OnAddClick)
            viewModel.onEvent(CategoryEvent.OnNameChanged("Hợp đồng lao động"))
            viewModel.submitForm { created ->
                createdName = created.name
            }

            val state = viewModel.waitForState { it.categories.isNotEmpty() }

            assertEquals(1, state.categories.size)
            assertEquals("Hợp đồng lao động", state.categories.first().name)
            assertEquals("Hợp đồng lao động", createdName)
            assertEquals(CategoryMode.Selection, state.mode)
        } finally {
            collection.cancel()
        }
    }

    @Test
    fun typingName_clearsNameError() = runBlocking {
        val collection = launch { viewModel.uiState.collect {} }
        try {
            viewModel.onEvent(CategoryEvent.OnAddClick)
            viewModel.onEvent(CategoryEvent.OnSubmitForm)
            viewModel.waitForState { it.form.nameError != null }

            viewModel.onEvent(CategoryEvent.OnNameChanged("Mới"))
            val state = viewModel.waitForState { it.form.nameError == null }

            assertNull(state.form.nameError)
            assertFalse(state.categories.isNotEmpty())
        } finally {
            collection.cancel()
        }
    }

    private suspend fun CategoryViewModel.waitForState(
        predicate: (CategoryUiState) -> Boolean,
    ): CategoryUiState {
        repeat(100) {
            val state = uiState.value
            if (predicate(state)) return state
            delay(50)
        }
        error("Timed out waiting for expected category UI state. Last state=${uiState.value}")
    }
}