package com.example.nestory.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun GlobalInputMonitor() {
    val state = LocalInputMonitor.current
    val isVisible = state.isVisible()

    if (isVisible) {
        // Box này phủ toàn màn hình để đảm bảo khung Card luôn ở chính giữa cửa sổ
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(bottom = 120.dp), // Đẩy lên trên một chút để không bị bàn phím che nếu bàn phím quá cao
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.98f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Đang nhập: ${state.label}",
                        style = NestoryTextStyles.Body12Semi,
                        color = GeneratedColor.Figma522ec8
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.text.ifEmpty { "..." },
                            style = NestoryTextStyles.Body18Semi.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            ),
                            color = if (state.text.isEmpty()) Color.Gray else Color.Black,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Hiệu ứng con trỏ nhấp nháy
                        val infiniteTransition = rememberInfiniteTransition(label = "cursor")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = keyframes {
                                    durationMillis = 800
                                    0.7f at 400
                                },
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "cursor_alpha"
                        )

                        Box(
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .width(2.dp)
                                .height(22.dp)
                                .alpha(alpha)
                                .background(GeneratedColor.Figma522ec8)
                        )
                    }
                }
            }
        }
    }
}
