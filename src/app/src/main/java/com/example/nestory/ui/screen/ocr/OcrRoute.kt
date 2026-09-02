package com.example.nestory.ui.screen.ocr

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.R
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.data.repository.KitItemRepositoryImpl
import com.example.nestory.data.repository.MlKitOcrRepository
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.ConfirmDialog
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.screen.category.CategoryRoute
import com.example.nestory.ui.screen.category.CategoryDivider
import com.example.nestory.ui.screen.category.CategoryHeader
import com.example.nestory.ui.screen.category.CategoryPrimaryActionButton
import com.example.nestory.ui.screen.category.CategoryUiModel
import com.example.nestory.ui.screen.category.defaultCategoryColors
import com.example.nestory.ui.screen.scanner.ScannerEvent
import com.example.nestory.ui.screen.scanner.ScannerMode
import com.example.nestory.ui.screen.scanner.ScannerPageUiModel
import com.example.nestory.ui.screen.scanner.ScannerScreen
import com.example.nestory.ui.screen.scanner.ScannerUiState
import com.example.nestory.ui.screen.scanner.cropBy
import com.example.nestory.ui.screen.scanner.decodeBitmaps
import com.example.nestory.ui.screen.scanner.insetFullImageCropRect
import com.example.nestory.ui.screen.scanner.rotateBy
import com.example.nestory.ui.screen.scanner.withCropRatio
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryTextStyles
import com.example.nestory.utils.ocr.CategoryDetector
import com.example.nestory.utils.ocr.DocumentDraftMapper
import com.example.nestory.utils.ocr.OcrTextParser
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * Scan flow route: ML Kit document scanner -> custom scan preview/editing -> OCR review form.
 */
@Composable
fun OcrRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    linkToItemId: Long? = null,
    editLeaveRequested: Boolean = false,
    onEditLeaveComplete: () -> Unit = {},
    onEditLeaveDismiss: () -> Unit = {},
    onEditModeChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }

    val ocrRepository = remember { MlKitOcrRepository() }
    val categoryDetector = remember { CategoryDetector() }
    val documentRepository = remember { DocumentRepositoryImpl(db.documentDao()) }
    val attachmentRepository = remember { AttachmentRepositoryImpl(db.attachmentDao()) }
    val containerRepository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val kitItemRepository = remember { KitItemRepositoryImpl(db.kitItemDao()) }
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
            kitItemRepository = kitItemRepository,
        )
    }

    val viewModel: OcrViewModel = viewModel(factory = factory)
    
    // Set pending link item if provided
    LaunchedEffect(linkToItemId) {
        viewModel.setPendingKitLinkItemId(linkToItemId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val containers by viewModel.containers.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var scannerUiState by remember { mutableStateOf(ScannerUiState()) }
    var hasRequestedInitialScan by remember { mutableStateOf(false) }
    var appendScanResult by remember { mutableStateOf(false) }
    var scannerError by remember { mutableStateOf<String?>(null) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(10)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember(scannerOptions) { GmsDocumentScanning.getClient(scannerOptions) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            if (scannerUiState.pages.isEmpty()) onBack()
            return@rememberLauncherForActivityResult
        }

        val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        val pageUris = scanResult?.pages.orEmpty().map { it.imageUri }
        if (pageUris.isEmpty()) {
            scannerError = "Không nhận được ảnh scan từ máy quét."
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            val bitmaps = context.contentResolver.decodeBitmaps(pageUris)
            if (bitmaps.isEmpty()) {
                scannerError = "Không thể đọc ảnh scan."
                return@launch
            }

            val newPages = bitmaps.map { bitmap -> ScannerPageUiModel(bitmap = bitmap) }
            scannerUiState = if (appendScanResult) {
                scannerUiState.copy(
                    mode = ScannerMode.Preview,
                    pages = scannerUiState.pages + newPages,
                    selectedPageIndex = scannerUiState.pages.size,
                    currentCropRatio = "free",
                )
            } else {
                ScannerUiState(
                    mode = ScannerMode.Preview,
                    pages = newPages,
                    selectedPageIndex = 0,
                )
            }
            scannerError = null
        }
    }

    fun launchDocumentScanner(append: Boolean) {
        val currentActivity = activity
        if (currentActivity == null) {
            scannerError = "Không thể mở máy quét trên màn hình hiện tại."
            return
        }

        appendScanResult = append
        scanner.getStartScanIntent(currentActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { error ->
                scannerError = error.message ?: "Không thể mở máy quét tài liệu."
            }
    }

    fun updateSelectedPage(transform: (ScannerPageUiModel) -> ScannerPageUiModel) {
        val selectedIndex = scannerUiState.selectedPageIndex
        val updatedPages = scannerUiState.pages.mapIndexed { index, page ->
            if (index == selectedIndex) transform(page) else page
        }
        scannerUiState = scannerUiState.copy(pages = updatedPages)
    }

    val hasInProgressWork = scannerUiState.pages.isNotEmpty() || uiState is OcrUiState.Success

    fun requestExit() {
        if (hasInProgressWork) {
            showDiscardConfirm = true
        } else {
            scannerUiState = ScannerUiState()
            viewModel.cancelOcr()
            onBack()
        }
    }

    fun confirmDiscard() {
        showDiscardConfirm = false
        scannerUiState = ScannerUiState()
        viewModel.cancelOcr()
        if (editLeaveRequested) {
            onEditLeaveComplete()
        } else {
            onBack()
        }
    }

    LaunchedEffect(hasInProgressWork) {
        onEditModeChange(hasInProgressWork)
    }

    DisposableEffect(Unit) {
        onDispose { onEditModeChange(false) }
    }

    LaunchedEffect(editLeaveRequested) {
        if (editLeaveRequested) {
            if (hasInProgressWork) {
                showDiscardConfirm = true
            } else {
                onEditLeaveComplete()
            }
        }
    }

    fun handleScannerEvent(event: ScannerEvent) {
        when (event) {
            ScannerEvent.OnBackClick -> {
                if (scannerUiState.mode == ScannerMode.FullscreenView) {
                    scannerUiState = scannerUiState.copy(
                        mode = ScannerMode.Preview,
                        zoomPercent = 100,
                    )
                } else {
                    requestExit()
                }
            }

            ScannerEvent.OnCancelClick -> {
                requestExit()
            }

            ScannerEvent.OnRotateLeftClick -> updateSelectedPage { page ->
                page.copy(bitmap = page.bitmap.rotateBy(-90f), cropRect = null)
            }

            ScannerEvent.OnRotateRightClick -> updateSelectedPage { page ->
                page.copy(bitmap = page.bitmap.rotateBy(90f), cropRect = null)
            }

            ScannerEvent.OnCropClick -> {
                scannerUiState = scannerUiState.copy(mode = ScannerMode.Crop, currentCropRatio = "free")
            }

            ScannerEvent.OnDeleteClick -> {
                val pages = scannerUiState.pages.toMutableList()
                if (pages.isEmpty()) return
                pages.removeAt(scannerUiState.selectedPageIndex)
                if (pages.isEmpty()) {
                    scannerUiState = ScannerUiState()
                    launchDocumentScanner(append = false)
                } else {
                    scannerUiState = scannerUiState.copy(
                        pages = pages,
                        selectedPageIndex = min(scannerUiState.selectedPageIndex, pages.lastIndex),
                        currentCropRatio = "free",
                    )
                }
            }

            ScannerEvent.OnAddImageClick -> launchDocumentScanner(append = true)

            ScannerEvent.OnContinueClick -> {
                val processedBitmaps = scannerUiState.pages.map { page ->
                    page.bitmap.cropBy(page.cropRect)
                }
                viewModel.processImages(processedBitmaps)
            }

            ScannerEvent.OnPreviewImageClick -> {
                if (scannerUiState.currentPage != null) {
                    scannerUiState = scannerUiState.copy(
                        mode = ScannerMode.FullscreenView,
                        zoomPercent = 100,
                    )
                }
            }

            is ScannerEvent.OnPageSelected -> {
                scannerUiState = scannerUiState.copy(
                    selectedPageIndex = event.index.coerceIn(0, max(scannerUiState.pages.lastIndex, 0)),
                    currentCropRatio = "free",
                )
            }

            ScannerEvent.OnCloseCropClick -> {
                updateSelectedPage { page -> page.copy(cropRect = null) }
                scannerUiState = scannerUiState.copy(mode = ScannerMode.Preview, currentCropRatio = "free")
            }

            ScannerEvent.OnDoneCropClick -> {
                updateSelectedPage { page ->
                    page.copy(bitmap = page.bitmap.cropBy(page.cropRect), cropRect = null)
                }
                scannerUiState = scannerUiState.copy(mode = ScannerMode.Preview, currentCropRatio = "free")
            }

            ScannerEvent.OnResetCropClick -> {
                updateSelectedPage { page -> page.copy(cropRect = insetFullImageCropRect()) }
                scannerUiState = scannerUiState.copy(currentCropRatio = "free")
            }

            is ScannerEvent.OnCropRectChanged -> {
                updateSelectedPage { page -> page.copy(cropRect = event.cropRect) }
                scannerUiState = scannerUiState.copy(currentCropRatio = "free")
            }

            is ScannerEvent.OnRatioSelected -> {
                updateSelectedPage { page -> page.withCropRatio(event.ratio) }
                scannerUiState = scannerUiState.copy(currentCropRatio = event.ratio)
            }

            ScannerEvent.OnShareClick,
            ScannerEvent.OnMenuClick -> Unit

            ScannerEvent.OnZoomInClick -> {
                scannerUiState = scannerUiState.copy(zoomPercent = min(scannerUiState.zoomPercent + 25, 300))
            }

            ScannerEvent.OnZoomOutClick -> {
                scannerUiState = scannerUiState.copy(zoomPercent = max(scannerUiState.zoomPercent - 25, 50))
            }

            ScannerEvent.OnPreviousPageClick -> {
                if (scannerUiState.canGoToPreviousPage) {
                    scannerUiState = scannerUiState.copy(
                        selectedPageIndex = scannerUiState.selectedPageIndex - 1,
                        zoomPercent = 100,
                    )
                }
            }

            ScannerEvent.OnNextPageClick -> {
                if (scannerUiState.canGoToNextPage) {
                    scannerUiState = scannerUiState.copy(
                        selectedPageIndex = scannerUiState.selectedPageIndex + 1,
                        zoomPercent = 100,
                    )
                }
            }
        }
    }

    // Reset any state left over from a previous scan/create-document session so that
    // entering Scan always starts a brand-new session (never reuses the last document).
    LaunchedEffect(Unit) {
        viewModel.resetForNewSession()
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedInitialScan) {
            hasRequestedInitialScan = true
            launchDocumentScanner(append = false)
        }
    }

    when (val state = uiState) {
        is OcrUiState.Idle -> {
            when {
                scannerUiState.pages.isNotEmpty() -> {
                    ScannerScreen(uiState = scannerUiState, onEvent = ::handleScannerEvent)
                }

                scannerError != null -> {
                    ScannerErrorScreen(
                        message = scannerError.orEmpty(),
                        onRetry = { launchDocumentScanner(append = false) },
                        onBack = onBack,
                    )
                }

                else -> {
                    ScannerOpeningScreen(onBack = onBack)
                }
            }
        }

        is OcrUiState.Processing -> {
            BackHandler {
                viewModel.cancelOcr()
                onBack()
            }

            NestoryScreen(
                useStatusBarPadding = true,
                verticalPadding = 20.dp,
            ) {
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
            BackHandler {
                viewModel.cancelOcr()
                onBack()
            }

            NestoryScreen(
                useStatusBarPadding = true,
                verticalPadding = 20.dp,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_gridicons_cross),
                            contentDescription = "Close",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    viewModel.cancelOcr()
                                    onBack()
                                }
                                .size(24.dp),
                            tint = GeneratedColor.Figma000000,
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
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
        }

        is OcrUiState.Success -> {
            OcrReviewScreen(
                draft = state.draft,
                bitmaps = state.bitmaps,
                containers = containers,
                onDraftChange = viewModel::updateDraft,
                onBack = {
                    viewModel.cancelOcr()
                    onBack()
                },
                onSave = {
                    viewModel.saveDocument(onSaved = { onSaved() })
                },
                fieldErrors = fieldErrors,
                isSaving = isSaving,
                categorySelectionContent = { onBack, onConfirmSelection ->
                    CategoryRoute(
                        onBack = onBack,
                        onConfirmSelection = onConfirmSelection,
                        selectionOnly = true,
                        allowCreate = true,
                        initialSelectedName = state.draft.categoryName
                            ?: state.draft.category?.toVietnameseLabel(),
                    )
                },
            )
        }
    }

    if (showDiscardConfirm) {
        ConfirmDialog(
            title = "Huỷ tạo giấy tờ",
            message = "Bạn có muốn thoát khỏi quá trình tạo giấy tờ không?",
            highlightRange = 13..16,
            confirmLabel = "Thoát",
            dismissLabel = "Tiếp tục",
            onConfirm = { confirmDiscard() },
            onDismiss = { showDiscardConfirm = false },
        )
    }
}

@Composable
private fun ScannerOpeningScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = GeneratedColor.Figma1a60e2)
        Text(
            text = "Đang mở máy quét tài liệu...",
            style = NestoryTextStyles.Body14Medium,
            color = GeneratedColor.Figma919191,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun ScannerErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    NestoryScreen(
        useStatusBarPadding = true,
        verticalPadding = 20.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = NestoryTextStyles.Body14Medium,
                color = GeneratedColor.Figma000000,
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(text = "Thử lại")
            }
            Text(
                text = "Quay lại",
                modifier = Modifier.padding(top = 16.dp).clickable(onClick = onBack),
                style = NestoryTextStyles.Body14Medium,
                color = GeneratedColor.Figma1a60e2,
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
