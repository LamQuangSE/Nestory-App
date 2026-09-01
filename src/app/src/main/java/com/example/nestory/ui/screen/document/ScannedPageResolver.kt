package com.example.nestory.ui.screen.document

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.example.nestory.data.local.entity.AttachmentEntity
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Resolves the individual page images of a scanned document.
 *
 * A scanned document is stored as a collection of per-page image files (the
 * pages) plus one PDF (the generated document representation). This helper
 * exposes the page image paths so screens can load page metadata and only
 * decode the currently selected page instead of rendering the whole PDF.
 */
data class ScannedPages(
    val pagePaths: List<String>,
    val pdfPath: String?,
    val pageCount: Int,
)

suspend fun resolveScannedPages(
    context: Context,
    attachments: List<AttachmentEntity>,
    pdfPath: String?,
): ScannedPages = withContext(Dispatchers.IO) {
    val pagePaths = attachments
        .filter { !it.fileUri.endsWith(".pdf", ignoreCase = true) }
        .sortedBy { it.displayOrder }
        .map { it.fileUri }
        .filter { File(it).exists() }

    val attachmentPdfPath = attachments
        .filter { it.fileUri.endsWith(".pdf", ignoreCase = true) }
        .minByOrNull { it.displayOrder }
        ?.fileUri
    val resolvedPdfPath = listOfNotNull(attachmentPdfPath, pdfPath)
        .firstOrNull { File(it).exists() }

    val resolved = if (resolvedPdfPath != null) {
        val pdfPages = pdfPageCount(resolvedPdfPath)
        if (pagePaths.isNotEmpty() && (pdfPages == 0 || pagePaths.size == pdfPages)) {
            ScannedPages(pagePaths = pagePaths, pdfPath = resolvedPdfPath, pageCount = pagePaths.size)
        } else {
            // Legacy document: no per-page images matching the PDF, so render
            // the PDF pages on demand via the renderer.
            ScannedPages(pagePaths = emptyList(), pdfPath = resolvedPdfPath, pageCount = pdfPages)
        }
    } else {
        ScannedPages(pagePaths = pagePaths, pdfPath = null, pageCount = pagePaths.size)
    }
    resolved
}

/**
 * Decodes a single full-resolution page image from disk.
 * Used only for the currently selected page, not all pages at once.
 */
suspend fun decodePageBitmap(path: String): Bitmap? = withContext(Dispatchers.IO) {
    val file = File(path)
    if (!file.exists()) return@withContext null
    try {
        BitmapFactory.decodeFile(path)
    } catch (e: Exception) {
        null
    }
}

/**
 * Decodes a small, memory-light thumbnail for the page selector. The source
 * image is sampled down so the full-resolution page is never fully decoded for
 * a thumbnail.
 */
suspend fun decodePageThumbnail(path: String, targetWidthPx: Int): Bitmap? =
    withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext null
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null
            var sample = 1
            while (bounds.outWidth / sample > targetWidthPx * 2) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            null
        }
    }

private fun pdfPageCount(path: String): Int {
    val file = File(path)
    if (!file.exists()) return 0
    return try {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        try {
            renderer.pageCount
        } finally {
            renderer.close()
            fd.close()
        }
    } catch (e: Exception) {
        0
    }
}

/**
 * Renders a single PDF page to a bitmap on demand (used as a fallback for
 * legacy documents that do not have per-page image files).
 */
suspend fun renderPdfPageBitmap(path: String, index: Int, widthPx: Int): Bitmap? =
    withContext(Dispatchers.IO) {
        if (widthPx <= 0) return@withContext null
        val file = File(path)
        if (!file.exists()) return@withContext null
        try {
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            try {
                if (index < 0 || index >= renderer.pageCount) return@withContext null
                renderer.openPage(index).use { page ->
                    val width = page.width.coerceAtLeast(1)
                    val height = page.height.coerceAtLeast(1)
                    val scale = widthPx.toFloat() / width
                    val heightPx = (height * scale).roundToInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            } finally {
                renderer.close()
                fd.close()
            }
        } catch (e: Exception) {
            null
        }
    }
