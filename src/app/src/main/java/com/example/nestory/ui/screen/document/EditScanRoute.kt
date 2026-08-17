package com.example.nestory.ui.screen.document

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nestory.data.filesystem.ImageStorageManager
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.AttachmentEntity
import com.example.nestory.data.repository.AttachmentRepositoryImpl
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
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Editable scanned-file flow opened from Edit Document -> Tệp scan.
 *
 * Loads the pages of an existing PDF into the same scan-preview editor used for
 * new scans so the user can view, select, rotate, crop, delete and scan
 * additional pages. On save the PDF is regenerated from the edited bitmaps and
 * the stored attachment file is replaced.
 */
@Composable
fun EditScanRoute(
    filePath: String,
    documentId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val attachmentRepository = remember { AttachmentRepositoryImpl(db.attachmentDao()) }
    val imageStorageManager = remember { ImageStorageManager(context.applicationContext) }

    val fileName = remember(filePath) { baseNameOf(filePath) }
    var uiState by remember { mutableStateOf<ScannerUiState?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(filePath, documentId) {
        loadError = null
        uiState = withContext(Dispatchers.IO) {
            val attachments = attachmentRepository.getAttachmentsByDocumentId(documentId)
                .getOrNull().orEmpty()
            val resolved = resolveScannedPages(context, attachments, filePath)
            val pages = if (resolved.pagePaths.isNotEmpty()) {
                resolved.pagePaths.mapNotNull { path -> decodePageBitmap(path) }
                    .map { bitmap -> ScannerPageUiModel(bitmap = bitmap) }
            } else {
                pdfToBitmaps(filePath)
            }
            if (pages.isEmpty()) {
                loadError = "Không thể đọc các trang đã scan."
                null
            } else {
                ScannerUiState(
                    mode = ScannerMode.Preview,
                    pages = pages,
                    selectedPageIndex = 0,
                )
            }
        }
    }

    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(10)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember(scannerOptions) { GmsDocumentScanning.getClient(scannerOptions) }

    var appendScanResult by remember { mutableStateOf(false) }
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult

        val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        val pageUris = scanResult?.pages.orEmpty().map { it.imageUri }
        if (pageUris.isEmpty()) return@rememberLauncherForActivityResult

        coroutineScope.launch {
            val bitmaps = context.contentResolver.decodeBitmaps(pageUris)
            if (bitmaps.isEmpty()) return@launch
            val newPages = bitmaps.map { bitmap -> ScannerPageUiModel(bitmap = bitmap) }
            val current = uiState ?: ScannerUiState()
            uiState = if (appendScanResult) {
                current.copy(
                    mode = ScannerMode.Preview,
                    pages = current.pages + newPages,
                    selectedPageIndex = current.pages.size,
                    currentCropRatio = "free",
                )
            } else {
                ScannerUiState(
                    mode = ScannerMode.Preview,
                    pages = newPages,
                    selectedPageIndex = 0,
                )
            }
        }
    }

    fun launchScanner(append: Boolean) {
        val currentActivity = activity
        if (currentActivity == null) return
        appendScanResult = append
        scanner.getStartScanIntent(currentActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
    }

    fun updateSelectedPage(transform: (ScannerPageUiModel) -> ScannerPageUiModel) {
        val current = uiState ?: return
        val selectedIndex = current.selectedPageIndex
        uiState = current.copy(
            pages = current.pages.mapIndexed { index, page ->
                if (index == selectedIndex) transform(page) else page
            },
        )
    }

    fun handleEvent(event: ScannerEvent) {
        val current = uiState ?: return
        when (event) {
            ScannerEvent.OnBackClick,
            ScannerEvent.OnCancelClick -> {
                if (current.mode == ScannerMode.FullscreenView) {
                    uiState = current.copy(mode = ScannerMode.Preview, zoomPercent = 100)
                } else {
                    onBack()
                }
            }

            ScannerEvent.OnRotateLeftClick -> updateSelectedPage { page ->
                page.copy(bitmap = page.bitmap.rotateBy(-90f), cropRect = null)
            }

            ScannerEvent.OnRotateRightClick -> updateSelectedPage { page ->
                page.copy(bitmap = page.bitmap.rotateBy(90f), cropRect = null)
            }

            ScannerEvent.OnCropClick -> {
                uiState = current.copy(mode = ScannerMode.Crop, currentCropRatio = "free")
            }

            ScannerEvent.OnDeleteClick -> {
                val pages = current.pages.toMutableList()
                if (pages.isEmpty()) return
                pages.removeAt(current.selectedPageIndex)
                uiState = current.copy(
                    pages = pages,
                    selectedPageIndex = min(current.selectedPageIndex, pages.lastIndex),
                    currentCropRatio = "free",
                )
            }

            ScannerEvent.OnAddImageClick -> launchScanner(append = true)

            ScannerEvent.OnContinueClick -> {
                if (saving) return
                val processed = current.pages.map { page -> page.bitmap.cropBy(page.cropRect) }
                if (processed.isEmpty()) return
                saving = true
                coroutineScope.launch {
                    val pdfResult = imageStorageManager.saveBitmapsAsPdf(processed, fileName)
                    val pageResults = processed.map { bitmap ->
                        imageStorageManager.saveBitmap(bitmap)
                    }
                    pdfResult.fold(
                        onSuccess = { pdfPath ->
                            val pagePaths = pageResults.mapNotNull { it.getOrNull() }
                            val anyPageFailed = pageResults.any { it.isFailure }
                            if (!anyPageFailed) {
                                attachmentRepository.getAttachmentsByDocumentId(documentId)
                                    .fold(
                                        onSuccess = { attachments ->
                                            attachments.forEach { attachment ->
                                                attachmentRepository.deleteAttachmentMetadata(attachment)
                                                imageStorageManager.deleteFile(attachment.fileUri)
                                            }
                                            pagePaths.forEachIndexed { index, pagePath ->
                                                attachmentRepository.addAttachmentMetadata(
                                                    AttachmentEntity(
                                                        fileUri = pagePath,
                                                        documentId = documentId,
                                                        displayOrder = index,
                                                    ),
                                                )
                                            }
                                            attachmentRepository.addAttachmentMetadata(
                                                AttachmentEntity(
                                                    fileUri = pdfPath,
                                                    documentId = documentId,
                                                    displayOrder = pagePaths.size,
                                                ),
                                            )
                                            saving = false
                                            onBack()
                                        },
                                        onFailure = {
                                            saving = false
                                            onBack()
                                        },
                                    )
                            } else {
                                pagePaths.forEach { imageStorageManager.deleteFile(it) }
                                imageStorageManager.deleteFile(pdfPath)
                                saving = false
                                onBack()
                            }
                        },
                        onFailure = {
                            saving = false
                            onBack()
                        },
                    )
                }
            }

            ScannerEvent.OnPreviewImageClick -> {
                if (current.currentPage != null) {
                    uiState = current.copy(mode = ScannerMode.FullscreenView, zoomPercent = 100)
                }
            }

            is ScannerEvent.OnPageSelected -> {
                uiState = current.copy(
                    selectedPageIndex = event.index.coerceIn(0, max(current.pages.lastIndex, 0)),
                    currentCropRatio = "free",
                )
            }

            ScannerEvent.OnCloseCropClick -> {
                updateSelectedPage { page -> page.copy(cropRect = null) }
                uiState = uiState?.copy(mode = ScannerMode.Preview, currentCropRatio = "free")
            }

            ScannerEvent.OnDoneCropClick -> {
                updateSelectedPage { page ->
                    page.copy(bitmap = page.bitmap.cropBy(page.cropRect), cropRect = null)
                }
                uiState = uiState?.copy(mode = ScannerMode.Preview, currentCropRatio = "free")
            }

            ScannerEvent.OnResetCropClick -> {
                updateSelectedPage { page -> page.copy(cropRect = insetFullImageCropRect()) }
                uiState = uiState?.copy(currentCropRatio = "free")
            }

            is ScannerEvent.OnCropRectChanged -> {
                updateSelectedPage { page -> page.copy(cropRect = event.cropRect) }
                uiState = uiState?.copy(currentCropRatio = "free")
            }

            is ScannerEvent.OnRatioSelected -> {
                updateSelectedPage { page -> page.withCropRatio(event.ratio) }
                uiState = uiState?.copy(currentCropRatio = event.ratio)
            }

            ScannerEvent.OnShareClick,
            ScannerEvent.OnMenuClick -> Unit

            ScannerEvent.OnZoomInClick -> {
                uiState = current.copy(
                    zoomPercent = min(current.zoomPercent + 25, 300),
                )
            }

            ScannerEvent.OnZoomOutClick -> {
                uiState = current.copy(
                    zoomPercent = max(current.zoomPercent - 25, 50),
                )
            }

            ScannerEvent.OnPreviousPageClick -> {
                if (current.canGoToPreviousPage) {
                    uiState = current.copy(
                        selectedPageIndex = current.selectedPageIndex - 1,
                        zoomPercent = 100,
                    )
                }
            }

            ScannerEvent.OnNextPageClick -> {
                if (current.canGoToNextPage) {
                    uiState = current.copy(
                        selectedPageIndex = current.selectedPageIndex + 1,
                        zoomPercent = 100,
                    )
                }
            }
        }
    }

    val state = uiState
    when {
        loadError != null -> {
            EditScanErrorScreen(message = loadError.orEmpty(), onBack = onBack)
        }

        state != null -> {
            ScannerScreen(uiState = state, onEvent = ::handleEvent)
        }

        else -> {
            EditScanLoadingScreen(onBack = onBack)
        }
    }
}

@Composable
private fun EditScanLoadingScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        color = GeneratedColor.Figma1a60e2,
    )
}

@Composable
private fun EditScanErrorScreen(
    message: String,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .padding(24.dp),
    ) {
        Text(
            text = message,
            style = NestoryTextStyles.Body14Medium,
            color = GeneratedColor.Figma000000,
        )
        TextButton(onClick = onBack) {
            Text("Quay lại")
        }
    }
}

private fun baseNameOf(path: String): String {
    val name = path.substringAfterLast('/')
    return name.removeSuffix(".pdf").removeSuffix(".PDF")
}

private fun pdfToBitmaps(path: String, targetWidthPx: Int = 1200): List<ScannerPageUiModel> {
    val file = File(path)
    if (!file.exists()) return emptyList()
    return try {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        try {
            val pages = mutableListOf<ScannerPageUiModel>()
            for (index in 0 until renderer.pageCount) {
                renderer.openPage(index).use { page ->
                    val width = page.width.coerceAtLeast(1)
                    val height = page.height.coerceAtLeast(1)
                    val scale = targetWidthPx.toFloat() / width
                    val heightPx = (height * scale).roundToInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(targetWidthPx, heightPx, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pages.add(ScannerPageUiModel(bitmap = bitmap))
                }
            }
            pages
        } finally {
            renderer.close()
            fd.close()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
