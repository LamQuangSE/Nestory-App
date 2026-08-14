package com.example.nestory.data.filesystem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Persists scanned bitmaps into app-internal storage so they can be
 * referenced as attachment file URIs.
 */
class ImageStorageManager(val context: Context) {

    suspend fun saveBitmap(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "attachments").apply { mkdirs() }
            val file = File(dir, "ocr_${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        }
    }

    suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            Unit
        }
    }

    /**
     * Combines all captured bitmaps into a single PDF file and persists it into
     * app-internal storage so it can be referenced as the document's scan file.
     */
    suspend fun saveBitmapsAsPdf(bitmaps: List<Bitmap>): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "attachments").apply { mkdirs() }
            val file = File(dir, "scan_${UUID.randomUUID()}.pdf")
            FileOutputStream(file).use { out ->
                val pdfDocument = PdfDocument()
                try {
                    bitmaps.forEachIndexed { index, bitmap ->
                        val pageInfo = PdfDocument.PageInfo.Builder(
                            bitmap.width,
                            bitmap.height,
                            index + 1,
                        ).create()
                        val page = pdfDocument.startPage(pageInfo)
                        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                        pdfDocument.finishPage(page)
                    }
                    pdfDocument.writeTo(out)
                } finally {
                    pdfDocument.close()
                }
            }
            file.absolutePath
        }
    }
}
