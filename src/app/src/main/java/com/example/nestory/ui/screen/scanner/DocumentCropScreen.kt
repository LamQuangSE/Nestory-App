package com.example.nestory.ui.screen.scanner

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.assets.AppIcons

@Composable
fun DocumentCropScreen(
    bitmap: Bitmap?,
    currentCropRatio: String = "free",
    cropRect: Rect? = null,
    onCloseClick: () -> Unit,
    onDoneClick: () -> Unit,
    onResetClick: () -> Unit,
    onCropRectChange: (Rect) -> Unit,
    onRatioSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var cropAreaSize by remember { mutableStateOf(IntSize.Zero) }
    var activeDragMode by remember { mutableStateOf(CropDragMode.None) }
    var activeDisplayedCropRect by remember { mutableStateOf<Rect?>(null) }
    val currentCropRect by rememberUpdatedState(cropRect)
    val imageRect = remember(cropAreaSize, bitmap) {
        bitmap?.let {
            calculateFitImageRect(
                containerSize = Size(cropAreaSize.width.toFloat(), cropAreaSize.height.toFloat()),
                imageWidth = it.width,
                imageHeight = it.height,
            )
        } ?: Rect.Zero
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar (Đã sửa icon Back đúng chuẩn, không bị nhầm thùng rác)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = AppIcons.IcBackwardArrow), 
                contentDescription = "Trở về",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onCloseClick() },
                tint = Color.Unspecified
            )
            Text(
                text = "Crop ảnh",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Xong",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1855EE),
                modifier = Modifier.clickable { onDoneClick() }
            )
        }

        // Vùng chứa ảnh nền đen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .onSizeChanged { cropAreaSize = it }
                .pointerInput(imageRect) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val displayedCropRect = normalizedCropToImageRect(
                                currentCropRect ?: insetFullImageCropRect(),
                                imageRect,
                            )
                            activeDragMode = cropDragModeFor(offset, displayedCropRect)
                            activeDisplayedCropRect = displayedCropRect
                        },
                        onDragEnd = {
                            activeDragMode = CropDragMode.None
                            activeDisplayedCropRect = null
                        },
                        onDragCancel = {
                            activeDragMode = CropDragMode.None
                            activeDisplayedCropRect = null
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val displayedCropRect = activeDisplayedCropRect
                                ?: normalizedCropToImageRect(
                                    currentCropRect ?: insetFullImageCropRect(),
                                    imageRect,
                                )
                            val changedRect = displayedCropRect.draggedBy(
                                mode = activeDragMode,
                                dragAmount = dragAmount,
                                bounds = imageRect,
                                minSizePx = 64.dp.toPx(),
                            )
                            activeDisplayedCropRect = changedRect
                            onCropRectChange(changedRect.toNormalizedCrop(imageRect))
                        },
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Original Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Canvas vẽ lớp phủ tối và đục lỗ khung crop
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.99f }
            ) {
                // Phủ tối toàn bộ khung nhìn nền đen
                drawRect(color = Color.Black.copy(alpha = 0.6f))

                val resolvedImageRect = if (imageRect != Rect.Zero) imageRect else Rect(
                    left = size.width * 0.1f,
                    top = size.height * 0.2f,
                    right = size.width * 0.9f,
                    bottom = size.height * 0.8f
                )

                // Khung crop mặc định fit khít 100% vào tấm ảnh, không tràn ra ngoài vùng đen
                val actualCropRect = normalizedCropToImageRect(cropRect ?: insetFullImageCropRect(), resolvedImageRect)

                // Ràng buộc tọa độ tuyệt đối nằm chặt bên trong biên của ảnh
                val left = actualCropRect.left.coerceIn(resolvedImageRect.left, resolvedImageRect.right)
                val top = actualCropRect.top.coerceIn(resolvedImageRect.top, resolvedImageRect.bottom)
                val right = actualCropRect.right.coerceIn(left, resolvedImageRect.right)
                val bottom = actualCropRect.bottom.coerceIn(top, resolvedImageRect.bottom)
                
                val cropWidth = right - left
                val cropHeight = bottom - top
                
                if (cropWidth > 0 && cropHeight > 0) {
                    // Đục lỗ sáng vùng crop
                    drawRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(cropWidth, cropHeight),
                        blendMode = BlendMode.Clear
                    )
                    
                    // Viền trắng khung crop
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(left, top),
                        size = Size(cropWidth, cropHeight),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                    
                    // Lưới 3x3 bên trong khung crop
                    val thirdW = cropWidth / 3
                    val thirdH = cropHeight / 3
                    val gridColor = Color.White.copy(alpha = 0.6f)
                    val gridStroke = 1.dp.toPx()
                    
                    drawLine(gridColor, Offset(left + thirdW, top), Offset(left + thirdW, top + cropHeight), gridStroke)
                    drawLine(gridColor, Offset(left + thirdW * 2, top), Offset(left + thirdW * 2, top + cropHeight), gridStroke)
                    drawLine(gridColor, Offset(left, top + thirdH), Offset(left + cropWidth, top + thirdH), gridStroke)
                    drawLine(gridColor, Offset(left, top + thirdH * 2), Offset(left + cropWidth, top + thirdH * 2), gridStroke)
                    
                    // 4 góc vuông của khung crop
                    val cornerLength = 20.dp.toPx()
                    val cornerStroke = 4.dp.toPx()
                    
                    drawLine(Color.White, Offset(left, top), Offset(left + cornerLength, top), cornerStroke)
                    drawLine(Color.White, Offset(left, top), Offset(left, top + cornerLength), cornerStroke)
                    drawLine(Color.White, Offset(left + cropWidth, top), Offset(left + cropWidth - cornerLength, top), cornerStroke)
                    drawLine(Color.White, Offset(left + cropWidth, top), Offset(left + cropWidth, top + cornerLength), cornerStroke)
                    drawLine(Color.White, Offset(left, top + cropHeight), Offset(left + cornerLength, top + cropHeight), cornerStroke)
                    drawLine(Color.White, Offset(left, top + cropHeight), Offset(left, top + cropHeight - cornerLength), cornerStroke)
                    drawLine(Color.White, Offset(left + cropWidth, top + cropHeight), Offset(left + cropWidth - cornerLength, top + cropHeight), cornerStroke)
                    drawLine(Color.White, Offset(left + cropWidth, top + cropHeight), Offset(left + cropWidth, top + cropHeight - cornerLength), cornerStroke)

                    val handleRadius = 7.dp.toPx()
                    val edgeHandleLong = 24.dp.toPx()
                    val edgeHandleShort = 6.dp.toPx()
                    val edgeHandleRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    val centerX = left + cropWidth / 2f
                    val centerY = top + cropHeight / 2f
                    drawCircle(Color.White, handleRadius, Offset(left, top))
                    drawCircle(Color.White, handleRadius, Offset(right, top))
                    drawCircle(Color.White, handleRadius, Offset(left, bottom))
                    drawCircle(Color.White, handleRadius, Offset(right, bottom))
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(centerX - edgeHandleLong / 2f, top - edgeHandleShort / 2f),
                        size = Size(edgeHandleLong, edgeHandleShort),
                        cornerRadius = edgeHandleRadius,
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(centerX - edgeHandleLong / 2f, bottom - edgeHandleShort / 2f),
                        size = Size(edgeHandleLong, edgeHandleShort),
                        cornerRadius = edgeHandleRadius,
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(left - edgeHandleShort / 2f, centerY - edgeHandleLong / 2f),
                        size = Size(edgeHandleShort, edgeHandleLong),
                        cornerRadius = edgeHandleRadius,
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(right - edgeHandleShort / 2f, centerY - edgeHandleLong / 2f),
                        size = Size(edgeHandleShort, edgeHandleLong),
                        cornerRadius = edgeHandleRadius,
                    )
                }
            }
        }

        // Bottom Controls (Đã nhận trực tiếp currentCropRatio để chuyển đổi trạng thái nút được chọn)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CropRatioItem(iconRes = AppIcons.IcFreeCrop, text = "Tự do", isSelected = currentCropRatio == "free") { onRatioSelected("free") }
                CropRatioItem(iconRes = AppIcons.Ic11Crop, text = "1:1", isSelected = currentCropRatio == "1:1") { onRatioSelected("1:1") }
                CropRatioItem(iconRes = AppIcons.Ic45Crop, text = "4:5", isSelected = currentCropRatio == "4:5") { onRatioSelected("4:5") }
                CropRatioItem(iconRes = AppIcons.Ic34Crop, text = "3:4", isSelected = currentCropRatio == "3:4") { onRatioSelected("3:4") }
                CropRatioItem(iconRes = AppIcons.Ic169Crop, text = "16:9", isSelected = currentCropRatio == "16:9") { onRatioSelected("16:9") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onResetClick,
                    modifier = Modifier.weight(1f).height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text("Đặt lại", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onDoneClick,
                    modifier = Modifier.weight(1f).height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1855EE)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Xong", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun calculateFitImageRect(containerSize: Size, imageWidth: Int, imageHeight: Int): Rect {
    if (imageWidth <= 0 || imageHeight <= 0 || containerSize.width <= 0f || containerSize.height <= 0f) {
        return Rect.Zero
    }

    val imageAspect = imageWidth.toFloat() / imageHeight.toFloat()
    val containerAspect = containerSize.width / containerSize.height
    return if (containerAspect > imageAspect) {
        val displayedHeight = containerSize.height
        val displayedWidth = displayedHeight * imageAspect
        val left = (containerSize.width - displayedWidth) / 2f
        Rect(left, 0f, left + displayedWidth, displayedHeight)
    } else {
        val displayedWidth = containerSize.width
        val displayedHeight = displayedWidth / imageAspect
        val top = (containerSize.height - displayedHeight) / 2f
        Rect(0f, top, displayedWidth, top + displayedHeight)
    }
}

private fun normalizedCropToImageRect(normalizedCrop: Rect, imageRect: Rect): Rect {
    val left = imageRect.left + imageRect.width * normalizedCrop.left.coerceIn(0f, 1f)
    val top = imageRect.top + imageRect.height * normalizedCrop.top.coerceIn(0f, 1f)
    val right = imageRect.left + imageRect.width * normalizedCrop.right.coerceIn(0f, 1f)
    val bottom = imageRect.top + imageRect.height * normalizedCrop.bottom.coerceIn(0f, 1f)
    return Rect(left, top, right, bottom)
}

private enum class CropDragMode {
    None,
    Move,
    Top,
    Bottom,
    Left,
    Right,
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
}

private fun cropDragModeFor(offset: Offset, cropRect: Rect): CropDragMode {
    val cornerTouchRadius = 64f
    val edgeTouchSlop = 36f
    val moveInset = 42f
    val centerX = cropRect.left + cropRect.width / 2f
    val centerY = cropRect.top + cropRect.height / 2f
    return when {
        offset.distanceTo(Offset(cropRect.left, cropRect.top)) <= cornerTouchRadius -> CropDragMode.TopLeft
        offset.distanceTo(Offset(cropRect.right, cropRect.top)) <= cornerTouchRadius -> CropDragMode.TopRight
        offset.distanceTo(Offset(cropRect.left, cropRect.bottom)) <= cornerTouchRadius -> CropDragMode.BottomLeft
        offset.distanceTo(Offset(cropRect.right, cropRect.bottom)) <= cornerTouchRadius -> CropDragMode.BottomRight
        offset.distanceTo(Offset(centerX, cropRect.top)) <= cornerTouchRadius -> CropDragMode.Top
        offset.distanceTo(Offset(centerX, cropRect.bottom)) <= cornerTouchRadius -> CropDragMode.Bottom
        offset.distanceTo(Offset(cropRect.left, centerY)) <= cornerTouchRadius -> CropDragMode.Left
        offset.distanceTo(Offset(cropRect.right, centerY)) <= cornerTouchRadius -> CropDragMode.Right
        kotlin.math.abs(offset.y - cropRect.top) <= edgeTouchSlop &&
            offset.x in cropRect.left..cropRect.right -> CropDragMode.Top
        kotlin.math.abs(offset.y - cropRect.bottom) <= edgeTouchSlop &&
            offset.x in cropRect.left..cropRect.right -> CropDragMode.Bottom
        kotlin.math.abs(offset.x - cropRect.left) <= edgeTouchSlop &&
            offset.y in cropRect.top..cropRect.bottom -> CropDragMode.Left
        kotlin.math.abs(offset.x - cropRect.right) <= edgeTouchSlop &&
            offset.y in cropRect.top..cropRect.bottom -> CropDragMode.Right
        Rect(
            cropRect.left + moveInset,
            cropRect.top + moveInset,
            cropRect.right - moveInset,
            cropRect.bottom - moveInset,
        ).contains(offset) -> CropDragMode.Move
        else -> CropDragMode.None
    }
}

private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun Rect.draggedBy(
    mode: CropDragMode,
    dragAmount: Offset,
    bounds: Rect,
    minSizePx: Float,
): Rect {
    if (bounds == Rect.Zero || mode == CropDragMode.None) return this

    return when (mode) {
        CropDragMode.Move -> {
            val maxLeft = bounds.right - width
            val maxTop = bounds.bottom - height
            val left = (this.left + dragAmount.x).coerceIn(bounds.left, maxLeft)
            val top = (this.top + dragAmount.y).coerceIn(bounds.top, maxTop)
            Rect(left, top, left + width, top + height)
        }

        CropDragMode.Top -> Rect(
            left = left,
            top = (top + dragAmount.y).coerceIn(bounds.top, bottom - minSizePx),
            right = right,
            bottom = bottom,
        )

        CropDragMode.Bottom -> Rect(
            left = left,
            top = top,
            right = right,
            bottom = (bottom + dragAmount.y).coerceIn(top + minSizePx, bounds.bottom),
        )

        CropDragMode.Left -> Rect(
            left = (left + dragAmount.x).coerceIn(bounds.left, right - minSizePx),
            top = top,
            right = right,
            bottom = bottom,
        )

        CropDragMode.Right -> Rect(
            left = left,
            top = top,
            right = (right + dragAmount.x).coerceIn(left + minSizePx, bounds.right),
            bottom = bottom,
        )

        CropDragMode.TopLeft -> Rect(
            left = (left + dragAmount.x).coerceIn(bounds.left, right - minSizePx),
            top = (top + dragAmount.y).coerceIn(bounds.top, bottom - minSizePx),
            right = right,
            bottom = bottom,
        )

        CropDragMode.TopRight -> Rect(
            left = left,
            top = (top + dragAmount.y).coerceIn(bounds.top, bottom - minSizePx),
            right = (right + dragAmount.x).coerceIn(left + minSizePx, bounds.right),
            bottom = bottom,
        )

        CropDragMode.BottomLeft -> Rect(
            left = (left + dragAmount.x).coerceIn(bounds.left, right - minSizePx),
            top = top,
            right = right,
            bottom = (bottom + dragAmount.y).coerceIn(top + minSizePx, bounds.bottom),
        )

        CropDragMode.BottomRight -> Rect(
            left = left,
            top = top,
            right = (right + dragAmount.x).coerceIn(left + minSizePx, bounds.right),
            bottom = (bottom + dragAmount.y).coerceIn(top + minSizePx, bounds.bottom),
        )

        CropDragMode.None -> this
    }
}

private fun Rect.toNormalizedCrop(imageRect: Rect): Rect {
    if (imageRect == Rect.Zero || imageRect.width <= 0f || imageRect.height <= 0f) {
        return fullImageCropRect
    }

    return Rect(
        left = ((left - imageRect.left) / imageRect.width).coerceIn(0f, 1f),
        top = ((top - imageRect.top) / imageRect.height).coerceIn(0f, 1f),
        right = ((right - imageRect.left) / imageRect.width).coerceIn(0f, 1f),
        bottom = ((bottom - imageRect.top) / imageRect.height).coerceIn(0f, 1f),
    )
}
