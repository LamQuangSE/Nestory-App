package com.example.nestory.ui.screen.ocr

import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.data.repository.KitItemRepositoryImpl
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.repository.OcrRepository
import com.example.nestory.utils.ocr.CategoryDetector
import com.example.nestory.utils.ocr.DocumentDraftMapper
import com.example.nestory.utils.ocr.OcrTextParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class OcrViewModelTest {
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

    private fun createViewModel(rawText: String): OcrViewModel {
        val categoryDetector = CategoryDetector()
        return OcrViewModel(
            ocrRepository = FakeOcrRepository(Result.success(rawText)),
            parser = OcrTextParser(),
            categoryDetector = categoryDetector,
            draftMapper = DocumentDraftMapper(categoryDetector),
            documentRepository = DocumentRepositoryImpl(database.documentDao()),
            attachmentRepository = AttachmentRepositoryImpl(database.attachmentDao()),
            containerRepository = ContainerRepositoryImpl(database.containerDao()),
            imageStorageManager = ImageStorageManager(InstrumentationRegistry.getInstrumentation().targetContext),
            kitItemRepository = KitItemRepositoryImpl(database.kitItemDao()),
        )
    }

    @Test
    fun processImages_knownIdentityCardText_autoFillsCategoryHolderAndDates() = runBlocking {
        val viewModel = createViewModel(
            """
            CĂN CƯỚC CÔNG DÂN
            Họ và tên: NGUYỄN VĂN A
            Ngày sinh: 01/01/1990
            Số: 012345678901
            Có giá trị đến ngày: 31/12/2030
            """.trimIndent(),
        )

        viewModel.processImages(listOf(bitmap()))
        val state = viewModel.waitForState { it is OcrUiState.Success }

        val draft = (state as OcrUiState.Success).draft
        assertEquals(DocumentCategory.IDENTITY, draft.category)
        assertEquals("NGUYỄN VĂN A", draft.holderName)
        assertEquals("01/01/1990", draft.issueDate)
        assertEquals("31/12/2030", draft.expiryDate)
    }

    @Test
    fun processImages_numberOnlyText_fallsBackWithoutCrash() = runBlocking {
        val viewModel = createViewModel("012345678901")

        viewModel.processImages(listOf(bitmap()))
        val state = viewModel.waitForState { it is OcrUiState.Success }

        val draft = (state as OcrUiState.Success).draft
        assertNull(draft.category)
    }

    @Test
    fun processImages_unrecognizedText_assignsDefaultsAndStaysEditable() = runBlocking {
        val viewModel = createViewModel("lorem ipsum dolor sit amet")

        viewModel.processImages(listOf(bitmap()))
        val state = viewModel.waitForState { it is OcrUiState.Success }

        val draft = (state as OcrUiState.Success).draft
        assertEquals("", draft.title)
        assertNull(draft.category)
        assertNull(draft.holderName)
        assertEquals("lorem ipsum dolor sit amet", draft.ocrText)
    }

    @Test
    fun processImages_ocrFailure_setsErrorState() = runBlocking {
        val categoryDetector = CategoryDetector()
        val viewModel = OcrViewModel(
            ocrRepository = FakeOcrRepository(Result.failure(RuntimeException("OCR failed"))),
            parser = OcrTextParser(),
            categoryDetector = categoryDetector,
            draftMapper = DocumentDraftMapper(categoryDetector),
            documentRepository = DocumentRepositoryImpl(database.documentDao()),
            attachmentRepository = AttachmentRepositoryImpl(database.attachmentDao()),
            containerRepository = ContainerRepositoryImpl(database.containerDao()),
            imageStorageManager = ImageStorageManager(InstrumentationRegistry.getInstrumentation().targetContext),
            kitItemRepository = KitItemRepositoryImpl(database.kitItemDao()),
        )

        viewModel.processImages(listOf(bitmap()))
        val state = viewModel.waitForState { it is OcrUiState.Error }

        assertEquals("OCR failed", (state as OcrUiState.Error).message)
    }

    private fun bitmap(): Bitmap =
        Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)

    private suspend fun OcrViewModel.waitForState(
        predicate: (OcrUiState) -> Boolean,
    ): OcrUiState {
        repeat(100) {
            val state = uiState.value
            if (predicate(state)) return state
            delay(50)
        }
        error("Timed out waiting for expected OCR state. Last state=${uiState.value}")
    }
}

private class FakeOcrRepository(
    private val result: Result<String>,
) : OcrRepository {
    override suspend fun recognizeText(bitmap: Bitmap): Result<String> = result
}