package com.example.nestory.ui.screen.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.assets.AppIcons

@Composable
fun DocumentPreviewScreen(
    uiState: ScannerUiState,
    onBackClick: () -> Unit,
    onRotateLeftClick: () -> Unit,
    onRotateRightClick: () -> Unit,
    onCropClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddImageClick: () -> Unit,
    onPageSelected: (Int) -> Unit,
    onPreviewImageClick: () -> Unit,
    onCancelClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPage = uiState.currentPage

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Icon(
                painter = painterResource(id = AppIcons.IcBackwardArrow),
                contentDescription = "Back",
                modifier = Modifier.size(26.dp).clickable { onBackClick() },
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Xem trước",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (currentPage != null) {
                Image(
                    bitmap = currentPage.bitmap.asImageBitmap(),
                    contentDescription = "Document",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(currentPage.bitmap.width.toFloat() / currentPage.bitmap.height.toFloat())
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onPreviewImageClick() }
                        .graphicsLayer { rotationZ = 0f },
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "Chưa có ảnh scan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF717171)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionToolItem(
                iconRes = AppIcons.IcRotateLeft,
                text = "Xoay trái",
                onClick = onRotateLeftClick,
                modifier = Modifier.weight(1f),
            )
            ActionToolItem(
                iconRes = AppIcons.IcRotateRight,
                text = "Xoay phải",
                onClick = onRotateRightClick,
                modifier = Modifier.weight(1f),
            )
            ActionToolItem(
                iconRes = AppIcons.IcCrop,
                text = "Cắt",
                onClick = onCropClick,
                modifier = Modifier.weight(1f),
            )
            ActionToolItem(
                iconRes = AppIcons.IcBlackBin,
                text = "Xoá",
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            uiState.pages.forEachIndexed { index, page ->
                ImageThumbnailItem(
                    bitmap = page.bitmap,
                    isSelected = index == uiState.selectedPageIndex,
                    onClick = { onPageSelected(index) }
                )
            }
            
            Box(
                modifier = Modifier
                    .size(71.dp, 56.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0xFFE5E7EB),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f)
                            ),
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAddImageClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = AppIcons.IcBaselinePlus),
                    contentDescription = "Add",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f).height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Text("Hủy", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onContinueClick,
                modifier = Modifier.weight(1f).height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1855EE)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Tiếp tục", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
