package com.example.nestory.ui.screens.ocr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.nestory.data.database.AppDatabase
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.data.ocr.CategoryDetector
import com.example.nestory.data.ocr.DocumentDraftMapper
import com.example.nestory.data.ocr.OcrTextParser
import com.example.nestory.data.repository.MlKitOcrRepository
import com.example.nestory.ui.screens.ocr.OcrViewModel
import com.example.nestory.ui.screens.ocr.OcrViewModelFactory
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryTextStyles

/**
 * OCR flow route: picks an image -> processes (OCR + parse) -> review -> save.
 */
@Composable
fun OcrRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "nestory_database",
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }

    val ocrRepository = remember { MlKitOcrRepository() }
    val categoryDetector = remember { CategoryDetector() }
    val documentRepository = remember { DocumentRepositoryImpl(db.documentDao()) }
    val attachmentRepository = remember { AttachmentRepositoryImpl(db.attachmentDao()) }
    val containerRepository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val imageStorageManager = remember { ImageStorageManager(context.applicationContext) }

    val factory = remember {
        OcrViewModelFactory(
            ocrRepository = ocrRepository,
            parser = OcrTextParser(),
            categoryDetector = categoryDetector,
            draftMapper = DocumentDraftMapper(categoryDetector),
            documentRepository = documentRepository,
            attachmentRepository = attachmentRepository,
            containerRepository = containerRepository,
            imageStorageManager = imageStorageManager,
        )
    }

    val viewModel: OcrViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val containers by viewModel.containers.collectAsState()

    var pickerAttempt by remember { mutableIntStateOf(0) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val bitmap = context.contentResolver
                .openInputStream(uri)
                ?.use { input -> android.graphics.BitmapFactory.decodeStream(input) }
            if (bitmap != null) {
                viewModel.processImage(bitmap)
            } else {
                pickerAttempt += 1
            }
        } else {
            pickerAttempt += 1
        }
    }

    when (val state = uiState) {
        is OcrUiState.Idle -> {
            // Ask the user to pick an image first.
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Chọn ảnh giấy tờ để nhận dạng",
                    style = NestoryTextStyles.Title22Semi,
                    color = GeneratedColor.Figma000000,
                )
            }
            LaunchedEffect(pickerAttempt) {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }
        }

        is OcrUiState.Processing -> {
            NestoryScreen {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = GeneratedColor.Figma1a60e2)
                    Text(
                        text = "Đang nhận dạng văn bản...",
                        style = NestoryTextStyles.Body14Medium,
                        color = GeneratedColor.Figma919191,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
        }

        is OcrUiState.Error -> {
            NestoryScreen {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.message,
                        style = NestoryTextStyles.Body14Medium,
                        color = GeneratedColor.Figma000000,
                    )
                }
            }
        }

        is OcrUiState.Success -> {
            OcrReviewScreen(
                draft = state.draft,
                containers = containers,
                onDraftChange = viewModel::updateDraft,
                onBack = onBack,
                onSave = {
                    viewModel.saveDocument(onSaved = { onSaved() })
                },
            )
        }
    }
}

