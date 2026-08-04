package com.example.nestory.ui.screen.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages

@Composable
fun DocumentCropScreen(
    currentCropRatio: String = "free",
    cropRect: Rect? = null,
    onCloseClick: () -> Unit,
    onDoneClick: () -> Unit,
    onResetClick: () -> Unit,
    onRatioSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Lưu tọa độ thực tế của ảnh quy chiếu trong Box cha
    var imageBounds by remember { mutableStateOf(Rect.Zero) }

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
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Tấm ảnh chính - Dùng boundsInParent để lấy đúng biên dạng trong Box chứa nó
            Image(
                painter = painterResource(id = AppImages.ImgCccd),
                contentDescription = "Original Image",
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        imageBounds = coordinates.boundsInParent()
                    },
                contentScale = ContentScale.Fit
            )
            
            // Canvas vẽ lớp phủ tối và đục lỗ khung crop
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.99f }
            ) {
                // Phủ tối toàn bộ khung nhìn nền đen
                drawRect(color = Color.Black.copy(alpha = 0.6f))
                
                // Giới hạn biên tối đa khớp tuyệt đối với kích thước hiển thị thực tế của ảnh
                val imgLeft = if (imageBounds.width > 0) imageBounds.left else size.width * 0.1f
                val imgTop = if (imageBounds.height > 0) imageBounds.top else size.height * 0.2f
                val imgRight = if (imageBounds.width > 0) imageBounds.right else size.width * 0.9f
                val imgBottom = if (imageBounds.height > 0) imageBounds.bottom else size.height * 0.8f

                // Khung crop mặc định fit khít 100% vào tấm ảnh, không tràn ra ngoài vùng đen
                val actualCropRect = cropRect ?: Rect(
                    left = imgLeft,
                    top = imgTop,
                    right = imgRight,
                    bottom = imgBottom
                )

                // Ràng buộc tọa độ tuyệt đối nằm chặt bên trong biên của ảnh
                val left = actualCropRect.left.coerceIn(imgLeft, imgRight)
                val top = actualCropRect.top.coerceIn(imgTop, imgBottom)
                val right = actualCropRect.right.coerceIn(left, imgRight)
                val bottom = actualCropRect.bottom.coerceIn(top, imgBottom)
                
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