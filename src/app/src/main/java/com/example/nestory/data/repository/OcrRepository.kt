package com.example.nestory.data.repository

import android.graphics.Bitmap

/**
 * OCR engine abstraction. Responsibility: Bitmap -> raw text.
 * No mapping, no saving.
 */
interface OcrRepository {
    suspend fun recognizeText(bitmap: Bitmap): Result<String>
}
