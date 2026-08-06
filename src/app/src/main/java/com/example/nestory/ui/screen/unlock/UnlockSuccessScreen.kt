package com.example.nestory.ui.screen.unlock

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import kotlinx.coroutines.delay

@Composable
fun UnlockSuccessScreen(
    onLoaded: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1450)
        onLoaded()
    }

    val rotation by rememberInfiniteTransition(label = "SuccessLoading").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SuccessLoadingRotation"
    )

    NestoryScreen(scrollable = false) {
        Spacer(modifier = Modifier.height(NestorySpacing.S40))
        Image(
            painter = painterResource(AppImages.FigmaLogoSuccess),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(138.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S20))
        Text(
            text = "Mở khoá thành công!",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Heading30,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        Text(
            text = "Chào mừng bạn quay trở lại.",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma717171,
            style = NestoryTextStyles.Body20Semi,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(72.dp))
        Image(
            painter = painterResource(AppIcons.FigmaLoadingRing),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .graphicsLayer { rotationZ = rotation }
                .height(150.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        Text(
            text = "Đang tải dữ liệu...",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma919191,
            style = NestoryTextStyles.Title21Semi,
            textAlign = TextAlign.Center
        )
    }
}
