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
            val file = uuidFile(dir, "ocr", "jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        }
    }

    /**
     * Persists a single page image (used as the document preview/thumbnail) into
     * app-internal storage with a UUID-backed name.
     */
    suspend fun saveBitmap(bitmap: Bitmap, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "attachments").apply { mkdirs() }
            val file = uuidFile(dir, "ocr", "jpg")
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
     * Renames an existing stored PDF to a new user-provided base file name. The
     * file bytes (the scanned pages/content) are left untouched; only the file
     * name changes. Returns the new absolute path, or the original path when the
     * file cannot be renamed.
     */
    suspend fun renamePdf(oldPath: String, newBaseName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val oldFile = File(oldPath)
            if (!oldFile.exists()) return@runCatching oldPath

            val newName = "${sanitizeFileName(newBaseName)}.pdf"
            if (oldFile.name == newName) return@runCatching oldPath

            val newFile = File(oldFile.parentFile, newName)
            if (newFile.exists()) {
                newFile.delete()
            }
            if (oldFile.renameTo(newFile)) newFile.absolutePath else oldPath
        }
    }

    /**
     * Combines all captured bitmaps into a UUID-backed PDF file in app-internal
     * storage so it can be referenced as the document's actual scanned file.
     */
    suspend fun saveBitmapsAsPdf(bitmaps: List<Bitmap>): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "attachments").apply { mkdirs() }
            val file = uuidFile(dir, "scan", "pdf")
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

    private fun uuidFile(dir: File, prefix: String, extension: String): File {
        var file: File
        do {
            file = File(dir, "${prefix}_${UUID.randomUUID()}.$extension")
        } while (file.exists())
        return file
    }

    /**
     * Normalizes a user-entered file name into a safe base name that can be used
     * inside the app's internal storage: strips path separators and illegal
     * characters, collapses whitespace into underscores and falls back to a
     * default when blank.
     */
    internal fun sanitizeFileName(fileName: String): String {
        val illegalChars = Regex("""[\\/:*?"<>|\u0000-\u001f]""")
        val sanitized = fileName
            .trim()
            .replace(illegalChars, "_")
            .replace(Regex("""\s+"""), "_")
            .trim('_', '.', ' ')
            .take(80)
        return sanitized.ifBlank { "scan" }
    }
}
