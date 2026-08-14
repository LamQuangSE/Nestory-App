package com.example.nestory.ui.screen.scanner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScannerScreen(
    uiState: ScannerUiState,
    onEvent: (ScannerEvent) -> Unit
) {
    BackHandler {
        when (uiState.mode) {
            ScannerMode.Crop -> onEvent(ScannerEvent.OnCloseCropClick)
            ScannerMode.FullscreenView,
            ScannerMode.Preview -> onEvent(ScannerEvent.OnBackClick)
        }
    }

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
                    onPageSelected = { index -> onEvent(ScannerEvent.OnPageSelected(index)) },
                    onPreviewImageClick = { onEvent(ScannerEvent.OnPreviewImageClick) },
                    onCancelClick = { onEvent(ScannerEvent.OnCancelClick) },
                    onContinueClick = { onEvent(ScannerEvent.OnContinueClick) }
                )
            }
            ScannerMode.Crop -> {
                DocumentCropScreen(
                    bitmap = uiState.currentPage?.bitmap,
                    currentCropRatio = uiState.currentCropRatio,
                    cropRect = uiState.currentPage?.cropRect,
                    onCloseClick = { onEvent(ScannerEvent.OnCloseCropClick) },
                    onDoneClick = { onEvent(ScannerEvent.OnDoneCropClick) },
                    onResetClick = { onEvent(ScannerEvent.OnResetCropClick) },
                    onCropRectChange = { cropRect -> onEvent(ScannerEvent.OnCropRectChanged(cropRect)) },
                    onRatioSelected = { ratio -> onEvent(ScannerEvent.OnRatioSelected(ratio)) }
                )
            }
            ScannerMode.FullscreenView -> {
                DocumentFullscreenViewScreen(
                    bitmap = uiState.currentPage?.bitmap,
                    pageIndicator = uiState.pageIndicator,
                    zoomText = uiState.zoomText,
                    canGoToPreviousPage = uiState.canGoToPreviousPage,
                    canGoToNextPage = uiState.canGoToNextPage,
                    onBackClick = { onEvent(ScannerEvent.OnBackClick) },
                    onShareClick = { onEvent(ScannerEvent.OnShareClick) },
                    onMenuClick = { onEvent(ScannerEvent.OnMenuClick) },
                    onZoomInClick = { onEvent(ScannerEvent.OnZoomInClick) },
                    onZoomOutClick = { onEvent(ScannerEvent.OnZoomOutClick) },
                    onPreviousPageClick = { onEvent(ScannerEvent.OnPreviousPageClick) },
                    onNextPageClick = { onEvent(ScannerEvent.OnNextPageClick) }
                )
            }
        }
    }
}
