package com.example.nestory.ui.screen.scanner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScannerScreen(
    uiState: ScannerUiState,
    onEvent: (ScannerEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.mode) {
            ScannerMode.Preview -> {
                DocumentPreviewScreen(
                    uiState = uiState,
                    onBackClick = { onEvent(ScannerEvent.OnBackClick) },
                    onRotateLeftClick = { onEvent(ScannerEvent.OnRotateLeftClick) },
                    onRotateRightClick = { onEvent(ScannerEvent.OnRotateRightClick) },
                    onCropClick = { onEvent(ScannerEvent.OnCropClick) },
                    onDeleteClick = { onEvent(ScannerEvent.OnDeleteClick) },
                    onAddImageClick = { onEvent(ScannerEvent.OnAddImageClick) },
                    onCancelClick = { onEvent(ScannerEvent.OnCancelClick) },
                    onContinueClick = { onEvent(ScannerEvent.OnContinueClick) }
                )
            }
            ScannerMode.Crop -> {
                DocumentCropScreen(
                    currentCropRatio = uiState.currentCropRatio,
                    cropRect = uiState.cropRect,
                    onCloseClick = { onEvent(ScannerEvent.OnCloseCropClick) },
                    onDoneClick = { onEvent(ScannerEvent.OnDoneCropClick) },
                    onResetClick = { onEvent(ScannerEvent.OnResetCropClick) },
                    onRatioSelected = { ratio -> onEvent(ScannerEvent.OnRatioSelected(ratio)) }
                )
            }
            ScannerMode.FullscreenView -> {
                DocumentFullscreenViewScreen(
                    pageIndicator = uiState.pageIndicator,
                    zoomText = uiState.zoomText,
                    rotationDegrees = uiState.rotationDegrees,
                    onBackClick = { onEvent(ScannerEvent.OnBackClick) },
                    onShareClick = { onEvent(ScannerEvent.OnShareClick) },
                    onMenuClick = { onEvent(ScannerEvent.OnMenuClick) },
                    onZoomInClick = { onEvent(ScannerEvent.OnZoomInClick) },
                    onZoomOutClick = { onEvent(ScannerEvent.OnZoomOutClick) }
                )
            }
        }
    }
}