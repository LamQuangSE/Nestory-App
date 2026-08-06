package com.example.nestory.ui.screen.scanner

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

suspend fun ContentResolver.decodeBitmaps(uris: List<Uri>): List<Bitmap> =
    withContext(Dispatchers.IO) {
        uris.mapNotNull { uri ->
            openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }
    }

fun Bitmap.rotateBy(degrees: Float): Bitmap {
    if (degrees % 360f == 0f) return this

    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.cropBy(normalizedCrop: Rect?): Bitmap {
    val crop = normalizedCrop ?: return this
    val left = (crop.left.coerceIn(0f, 1f) * width).roundToInt().coerceIn(0, width - 1)
    val top = (crop.top.coerceIn(0f, 1f) * height).roundToInt().coerceIn(0, height - 1)
    val right = (crop.right.coerceIn(0f, 1f) * width).roundToInt().coerceIn(left + 1, width)
    val bottom = (crop.bottom.coerceIn(0f, 1f) * height).roundToInt().coerceIn(top + 1, height)

    return Bitmap.createBitmap(this, left, top, right - left, bottom - top)
}
