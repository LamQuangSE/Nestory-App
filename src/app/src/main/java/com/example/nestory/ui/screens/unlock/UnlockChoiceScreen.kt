package com.example.nestory.ui.screens.unlock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryLogo
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.components.SafeIllustration
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun UnlockChoiceScreen(
    onFingerprint: () -> Unit,
    onPin: () -> Unit
) {
    NestoryScreen {
        Spacer(modifier = Modifier.height(NestorySpacing.S40))
        NestoryLogo(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            centered = true
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S30))
        Text(
            text = "Mở khoá kho lưu trữ",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Title21Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        Text(
            text = "Sử dụng xác thực thiết bị để truy cập giấy tờ của bạn.",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body18Semi,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        SafeIllustration(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            compact = false
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        PrimaryActionButton(
            text = "Mở khóa bằng sinh trắc học",
            leadingIcon = AppIcons.FigmaFingerprint,
            onClick = onFingerprint
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S17))
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(GeneratedColor.FigmaE5e7eb)
            )
            Text(
                text = "hoặc",
                modifier = Modifier.width(NestorySpacing.S75),
                color = GeneratedColor.Figma717171,
                style = NestoryTextStyles.Body17Medium,
                textAlign = TextAlign.Center
            )
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(GeneratedColor.FigmaE5e7eb)
            )
        }
        Spacer(modifier = Modifier.height(NestorySpacing.S17))
        Text(
            text = "Sử dụng mã PIN",
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onPin),
            color = GeneratedColor.Figma1a60e2,
            style = NestoryTextStyles.Body17Medium,
            textAlign = TextAlign.Center
        )
    }
}
