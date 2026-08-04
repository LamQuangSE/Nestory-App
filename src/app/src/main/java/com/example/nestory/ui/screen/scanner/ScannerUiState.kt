package com.example.nestory.ui.screen.scanner

import androidx.compose.ui.geometry.Rect

enum class ScannerMode {
    Preview,
    Crop,
    FullscreenView // Bổ sung mode xem toàn màn hình nền đen
}

data class ScannerUiState(
    val mode: ScannerMode = ScannerMode.Preview,
    val currentCropRatio: String = "free",
    val cropRect: Rect? = null,
    val rotationDegrees: Float = 0f,
    val pageIndicator: String = "1/3",
    val zoomText: String = "100%"
)

sealed interface ScannerEvent {
    object OnBackClick : ScannerEvent
    object OnRotateLeftClick : ScannerEvent
    object OnRotateRightClick : ScannerEvent
    object OnCropClick : ScannerEvent
    object OnDeleteClick : ScannerEvent
    object OnAddImageClick : ScannerEvent
    object OnCancelClick : ScannerEvent
    object OnContinueClick : ScannerEvent

    object OnCloseCropClick : ScannerEvent
    object OnDoneCropClick : ScannerEvent
    object OnResetCropClick : ScannerEvent
    data class OnRatioSelected(val ratio: String) : ScannerEvent

    // Event cho màn hình Fullscreen View
    object OnShareClick : ScannerEvent
    object OnMenuClick : ScannerEvent
    object OnZoomInClick : ScannerEvent
    object OnZoomOutClick : ScannerEvent
}