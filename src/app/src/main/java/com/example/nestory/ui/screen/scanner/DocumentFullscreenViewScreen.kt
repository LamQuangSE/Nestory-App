package com.example.nestory.ui.screen.scanner

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.assets.AppIcons
import kotlin.math.max

@Composable
fun DocumentFullscreenViewScreen(
    bitmap: Bitmap?,
    pageIndicator: String = "1/3",
    zoomText: String = "100%",
    canGoToPreviousPage: Boolean = false,
    canGoToNextPage: Boolean = false,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    onZoomInClick: () -> Unit,
    onZoomOutClick: () -> Unit,
    onPreviousPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomScale = zoomText.removeSuffix("%").toFloatOrNull()?.div(100f) ?: 1f
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(bitmap) {
        panOffset = Offset.Zero
    }

    LaunchedEffect(zoomScale, viewportSize) {
        panOffset = if (zoomScale <= 1f) {
            Offset.Zero
        } else {
            panOffset.constrainPan(viewportSize, zoomScale)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                painter = painterResource(id = AppIcons.IcBackwardArrow),
                contentDescription = "Trở về",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp)
                    .size(26.dp)
                    .clickable { onBackClick() },
                tint = Color.White
            )
            Text(
                text = pageIndicator,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = AppIcons.IcShare),
                    contentDescription = "Chia sẻ",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onShareClick() },
                    tint = Color.White
                )
                Icon(
                    painter = painterResource(id = AppIcons.IcMoreInfor),
                    contentDescription = "Tùy chọn",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onMenuClick() },
                    tint = Color.White
                )
            }
        }

        // 2. Vùng hiển thị ảnh phóng to ở giữa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .onSizeChanged { viewportSize = it }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Fullscreen Document",
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(zoomScale, viewportSize, bitmap) {
                            detectDragGestures { change, dragAmount ->
                                if (zoomScale > 1f) {
                                    change.consume()
                                    panOffset = (panOffset + dragAmount).constrainPan(viewportSize, zoomScale)
                                }
                            }
                        }
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = panOffset.x
                            translationY = panOffset.y
                        },
                    contentScale = ContentScale.Fit
                )
            }

            if (canGoToPreviousPage || canGoToNextPage) {
                FullscreenPageButton(
                    isNext = false,
                    enabled = canGoToPreviousPage,
                    onClick = onPreviousPageClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                )
                FullscreenPageButton(
                    isNext = true,
                    enabled = canGoToNextPage,
                    onClick = onNextPageClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                )
            }
        }

        // 3. Thanh điều khiển Zoom ở đáy (Nút -, Text 100%, Nút +)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .height(46.dp)
                    .background(Color.DarkGray.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = AppIcons.IcMinus),
                    contentDescription = "Zoom Out",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onZoomOutClick() },
                    tint = Color.White
                )
                Text(
                    text = zoomText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Icon(
                    painter = painterResource(id = AppIcons.IcPlus),
                    contentDescription = "Zoom In",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onZoomInClick() },
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun FullscreenPageButton(
    isNext: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(CircleShape)
            .background(Color.DarkGray.copy(alpha = 0.65f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = AppIcons.IcBackwardArrow),
            contentDescription = if (isNext) "Ảnh sau" else "Ảnh trước",
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { rotationZ = if (isNext) 180f else 0f },
            tint = Color.White,
        )
    }
}

private fun Offset.constrainPan(viewportSize: IntSize, scale: Float): Offset {
    if (viewportSize == IntSize.Zero || scale <= 1f) return Offset.Zero
    val maxX = max(0f, viewportSize.width * (scale - 1f) / 2f)
    val maxY = max(0f, viewportSize.height * (scale - 1f) / 2f)
    return Offset(
        x = x.coerceIn(-maxX, maxX),
        y = y.coerceIn(-maxY, maxY),
    )
}
