package com.example.nestory.ui.screens.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.SafeIllustration
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import kotlinx.coroutines.delay

import androidx.compose.ui.platform.LocalContext
import com.example.nestory.data.filesystem.FileSystemManager

@Composable
fun WaitingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val fileSystemManager = remember { FileSystemManager(context) }
    var visibleSteps by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Step 1: Khởi tạo (creates files/ and cache/)
        delay(400)
        visibleSteps = 1
        
        // Step 2: Tạo không gian lưu trữ (creates shared_prefs/)
        delay(500)
        visibleSteps = 2
        
        // Step 3: Thiết lập cấu hình (creates databases/)
        // We run the actual file system creation here
        fileSystemManager.createVaultStructure()
        delay(500)
        visibleSteps = 3
        
        // Step 4: Hoàn tất
        delay(500)
        visibleSteps = 4
        
        delay(800)
        onComplete()
    }

    val pulse by rememberInfiniteTransition(label = "WaitingPulse").animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaitingVaultScale"
    )

    NestoryScreen(scrollable = false) {
        Spacer(modifier = Modifier.height(NestorySpacing.S40))
        SafeIllustration(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .scale(pulse),
            checked = true
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        Text(
            text = "Đang tạo kho lưu trữ...",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Heading25Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S10))
        Text(
            text = "Vui lòng đợi trong giây lát.",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body15Semi,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S30))
        Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            listOf(
                "Khởi tạo cơ sở dữ liệu",
                "Tạo không gian lưu trữ",
                "Thiết lập cấu hình",
                "Hoàn tất"
            ).forEachIndexed { index, label ->
                AnimatedVisibility(
                    visible = index < visibleSteps,
                    enter = fadeIn(tween(180)) + slideInVertically(
                        animationSpec = tween(220),
                        initialOffsetY = { it / 3 }
                    )
                ) {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(AppIcons.NestoryTickCircle),
                            contentDescription = null,
                            modifier = Modifier.width(28.dp).height(28.dp),
                            contentScale = ContentScale.Fit
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(NestorySpacing.S14))
                        Text(
                            text = label,
                            color = GeneratedColor.Figma000000,
                            style = NestoryTextStyles.Body14Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(NestorySpacing.S17))
            }
        }
    }
}
