package com.example.nestory.ui.screen.scanner

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect

enum class ScannerMode {
    Preview,
    Crop,
    FullscreenView
}

data class ScannerPageUiModel(
    val bitmap: Bitmap,
    val cropRect: Rect? = null,
)

val fullImageCropRect = Rect(0f, 0f, 1f, 1f)
private const val DEFAULT_CROP_MARGIN = 0.08f

fun ScannerPageUiModel.withCropRatio(ratio: String): ScannerPageUiModel =
    copy(cropRect = centeredCropRectForRatio(bitmap.width, bitmap.height, ratio))

fun centeredCropRectForRatio(width: Int, height: Int, ratio: String): Rect {
    if (width <= 0 || height <= 0) return insetFullImageCropRect()
    if (ratio == "free") return insetFullImageCropRect()

    val targetAspect = when (ratio) {
        "1:1" -> 1f
        "4:5" -> 4f / 5f
        "3:4" -> 3f / 4f
        "16:9" -> 16f / 9f
        else -> return insetFullImageCropRect()
    }
    val imageAspect = width.toFloat() / height.toFloat()
    val availableWidth = 1f - DEFAULT_CROP_MARGIN * 2f
    val availableHeight = 1f - DEFAULT_CROP_MARGIN * 2f

    return if (imageAspect > targetAspect) {
        val normalizedWidth = (targetAspect / imageAspect * availableHeight).coerceAtMost(availableWidth)
        val normalizedHeight = normalizedWidth * imageAspect / targetAspect
        val left = (1f - normalizedWidth) / 2f
        val top = (1f - normalizedHeight) / 2f
        Rect(left, top, left + normalizedWidth, top + normalizedHeight)
    } else {
        val normalizedHeight = (imageAspect / targetAspect * availableWidth).coerceAtMost(availableHeight)
        val normalizedWidth = normalizedHeight * targetAspect / imageAspect
        val left = (1f - normalizedWidth) / 2f
        val top = (1f - normalizedHeight) / 2f
        Rect(left, top, left + normalizedWidth, top + normalizedHeight)
    }
}

fun insetFullImageCropRect(): Rect =
    Rect(
        DEFAULT_CROP_MARGIN,
        DEFAULT_CROP_MARGIN,
        1f - DEFAULT_CROP_MARGIN,
        1f - DEFAULT_CROP_MARGIN,
    )

data class ScannerUiState(
    val mode: ScannerMode = ScannerMode.Preview,
    val pages: List<ScannerPageUiModel> = emptyList(),
    val selectedPageIndex: Int = 0,
    val currentCropRatio: String = "free",
    val zoomPercent: Int = 100,
) {
    val currentPage: ScannerPageUiModel?
        get() = pages.getOrNull(selectedPageIndex)

    val pageIndicator: String
        get() = if (pages.isEmpty()) "0/0" else "${selectedPageIndex + 1}/${pages.size}"

    val zoomText: String
        get() = "$zoomPercent%"

    val canGoToPreviousPage: Boolean
        get() = selectedPageIndex > 0

    val canGoToNextPage: Boolean
        get() = selectedPageIndex < pages.lastIndex
}

sealed interface ScannerEvent {
    object OnBackClick : ScannerEvent
    object OnRotateLeftClick : ScannerEvent
    object OnRotateRightClick : ScannerEvent
    object OnCropClick : ScannerEvent
    object OnDeleteClick : ScannerEvent
    object OnAddImageClick : ScannerEvent
    object OnCancelClick : ScannerEvent
    object OnContinueClick : ScannerEvent
    object OnPreviewImageClick : ScannerEvent

    data class OnPageSelected(val index: Int) : ScannerEvent

    object OnCloseCropClick : ScannerEvent
    object OnDoneCropClick : ScannerEvent
    object OnResetCropClick : ScannerEvent
    data class OnCropRectChanged(val cropRect: Rect) : ScannerEvent
    data class OnRatioSelected(val ratio: String) : ScannerEvent

    object OnShareClick : ScannerEvent
    object OnMenuClick : ScannerEvent
    object OnZoomInClick : ScannerEvent
    object OnZoomOutClick : ScannerEvent
    object OnPreviousPageClick : ScannerEvent
    object OnNextPageClick : ScannerEvent
}
