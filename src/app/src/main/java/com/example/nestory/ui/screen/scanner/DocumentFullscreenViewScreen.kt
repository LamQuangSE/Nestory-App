package com.example.nestory.ui.screen.scanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages

@Composable
fun DocumentFullscreenViewScreen(
    pageIndicator: String = "1/3",
    zoomText: String = "100%",
    rotationDegrees: Float = 0f,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    onZoomInClick: () -> Unit,
    onZoomOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Bar (Nền đen, chữ/icon trắng)
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
                    .clickable { onBackClick() },
                tint = Color.White
            )
            Text(
                text = pageIndicator,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
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
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = AppImages.ImgCccd),
                contentDescription = "Fullscreen Document",
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { rotationZ = rotationDegrees },
                contentScale = ContentScale.Fit
            )
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