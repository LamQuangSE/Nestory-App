package com.example.nestory.data.filesystem

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Persists scanned bitmaps into app-internal storage so they can be
 * referenced as attachment file URIs.
 */
class ImageStorageManager(private val context: Context) {

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
}
