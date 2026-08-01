package com.example.nestory.ui.screens.unlock

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.components.NestoryLogo
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import kotlinx.coroutines.delay

@Composable
fun UnlockFingerprintScreen(
    onCancel: () -> Unit,
    onUsePin: () -> Unit,
    onUnlocked: () -> Unit
) {
    var authenticating by remember { mutableStateOf(false) }
    val pulse by rememberInfiniteTransition(label = "FingerprintPulse").animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(760),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FingerprintScale"
    )

    LaunchedEffect(authenticating) {
        if (authenticating) {
            delay(520)
            onUnlocked()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = GeneratedColor.Figma000000.copy(alpha = 0.64f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.34f)
                    .background(GeneratedColor.FigmaFfffff)
                    .padding(horizontal = NestorySpacing.S24, vertical = NestorySpacing.S40),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(NestorySpacing.S40))
                NestoryLogo(centered = true)
                Spacer(modifier = Modifier.height(NestorySpacing.S40))
                Text(
                    text = "Mở khoá kho lưu trữ",
                    color = GeneratedColor.Figma000000,
                    style = NestoryTextStyles.Heading25Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(NestorySpacing.S14))
                Text(
                    text = "Sử dụng xác thực thiết bị để truy cập giấy tờ của bạn.",
                    color = GeneratedColor.Figma919191,
                    style = NestoryTextStyles.Body13Semi,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = NestorySpacing.S10, vertical = NestorySpacing.S40)
                    .fillMaxWidth()
                    .clip(NestoryRadius.R20)
                    .background(GeneratedColor.FigmaFfffff)
                    .padding(top = NestorySpacing.S30),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Mở khóa Nestory",
                    color = GeneratedColor.Figma000000,
                    style = NestoryTextStyles.Heading25Semi,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(NestorySpacing.S14))
                Text(
                    text = "Xác nhận danh tính để mở kho\nlưu trữ của bạn",
                    color = GeneratedColor.Figma919191,
                    style = NestoryTextStyles.Body13Semi,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(NestorySpacing.S30))
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(GeneratedColor.FigmaFfffff)
                        .border(2.dp, GeneratedColor.FigmaDbeafe, CircleShape)
                        .clickable { authenticating = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(AppIcons.FigmaFingerprint),
                        contentDescription = null,
                        modifier = Modifier
                            .size(62.dp)
                            .scale(if (authenticating) pulse else 1f),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(GeneratedColor.Figma1a60e2)
                    )
                }
                Spacer(modifier = Modifier.height(NestorySpacing.S20))
                Text(
                    text = if (authenticating) "Đang xác minh..." else "Chạm vào cảm biến vân tay",
                    color = GeneratedColor.Figma919191,
                    style = NestoryTextStyles.Body13Semi,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(NestorySpacing.S40))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .border(0.5.dp, GeneratedColor.FigmaE5e7eb),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Hủy",
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onCancel),
                        color = GeneratedColor.Figma1a60e2,
                        style = NestoryTextStyles.Body15Semi,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .background(GeneratedColor.FigmaE5e7eb)
                            .size(width = 1.dp, height = 42.dp)
                    )
                    Text(
                        text = "Dùng mã pin",
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onUsePin),
                        color = GeneratedColor.Figma1a60e2,
                        style = NestoryTextStyles.Body15Semi,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
