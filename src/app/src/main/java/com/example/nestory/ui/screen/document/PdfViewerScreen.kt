package com.example.nestory.ui.screen.document

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import kotlin.math.roundToInt

/**
 * Read-only page-based viewer for a scanned document.
 *
 * Treats the document as a collection of individual page images rather than a
 * single PDF. Only the currently selected page is decoded at full resolution;
 * the page selector shows lightweight thumbnails. For legacy documents without
 * per-page image files it falls back to rendering PDF pages on demand.
 */
@Composable
fun PdfViewerScreen(
    filePath: String,
    documentId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val attachmentRepository = remember { AttachmentRepositoryImpl(db.attachmentDao()) }

    // Resolved page metadata: either per-page image paths or a PDF fallback.
    var resolved by remember { mutableStateOf<ScannedPages?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(filePath, documentId) {
        loadFailed = false
        val attachments = attachmentRepository.getAttachmentsByDocumentId(documentId)
            .getOrNull().orEmpty()
        resolved = resolveScannedPages(context, attachments, filePath)
        if (resolved == null) loadFailed = true
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = GeneratedColor.FigmaFfffff
    ) {
        val pages = resolved
        when {
            loadFailed -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    PdfViewerTopBar(onBack = onBack)
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Không thể mở file scan",
                            style = NestoryTextStyles.Body13Semi,
                            color = GeneratedColor.Figma919191,
                        )
                    }
                }
            }

            pages == null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    PdfViewerTopBar(onBack = onBack)
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = GeneratedColor.Figma1a60e2,
                        )
                    }
                }
            }

            else -> {
                ScannedPageViewer(
                    pages = pages,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun ScannedPageViewer(
    pages: ScannedPages,
    onBack: () -> Unit,
) {
    val pageCount = pages.pagePaths.size
    val pdfPath = pages.pdfPath
    var selectedPage by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        PdfViewerTopBar(onBack = onBack)

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(GeneratedColor.FigmaFfffff),
        ) {
            val density = LocalDensity.current
            val previewWidthPx = with(density) { maxWidth.roundToPx() }
            val pagePath = pages.pagePaths.getOrNull(selectedPage)
            val bitmap by produceState<Bitmap?>(initialValue = null, selectedPage, previewWidthPx) {
                value = if (pagePath != null) {
                    decodePageBitmap(pagePath)
                } else if (pdfPath != null) {
                    renderPdfPageBitmap(pdfPath, selectedPage, previewWidthPx)
                } else {
                    null
                }
            }

            val current = bitmap
            if (current != null) {
                Image(
                    bitmap = current.asImageBitmap(),
                    contentDescription = "Trang ${selectedPage + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(NestorySpacing.S12),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = GeneratedColor.Figma1a60e2,
                    )
                }
            }
        }

        PageThumbnailStrip(
            pages = pages,
            pageCount = pageCount,
            selectedPage = selectedPage,
            onSelectPage = { selectedPage = it },
        )
    }
}

@Composable
private fun PageThumbnailStrip(
    pages: ScannedPages,
    pageCount: Int,
    selectedPage: Int,
    onSelectPage: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val thumbWidthPx = with(density) { 72.dp.roundToPx() }
    val stripPadding = (0.3f * 160f / 2.54f).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GeneratedColor.FigmaFfffff)
            .padding(horizontal = stripPadding, vertical = NestorySpacing.S10),
        horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S8),
        ) {
            itemsIndexed(
                items = pages.pagePaths,
                key = { index, _ -> index },
            ) { index, path ->
                val isSelected = index == selectedPage
                val pdfPath = pages.pdfPath
                val thumb by produceState<Bitmap?>(initialValue = null, index, thumbWidthPx, path) {
                    value = decodePageThumbnail(path, thumbWidthPx)
                        ?: pdfPath?.let { renderPdfPageBitmap(it, index, thumbWidthPx) }
                }
                val current = thumb
                val shape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .size(width = 72.dp, height = 56.dp)
                        .border(
                            2.dp,
                            if (isSelected) GeneratedColor.Figma1a60e2 else GeneratedColor.FigmaE5e7eb,
                            shape,
                        )
                        .clickable { onSelectPage(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (current != null) {
                        Image(
                            bitmap = current.asImageBitmap(),
                            contentDescription = "Trang ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GeneratedColor.FigmaFfffff),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = GeneratedColor.Figma1a60e2,
                        )
                    }
                }
            }
        }
        Text(
            text = "$pageCount trang",
            style = NestoryTextStyles.Body12Semi,
            color = GeneratedColor.Figma919191,
        )
    }
}

@Composable
private fun PdfViewerTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .statusBarsPadding()
            .padding(horizontal = NestorySpacing.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(AppIcons.IcBackwardArrow),
            contentDescription = "Quay lại",
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onBack),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Xem PDF",
            style = NestoryTextStyles.Heading25Bold,
            color = GeneratedColor.Figma000000,
        )
    }
}
