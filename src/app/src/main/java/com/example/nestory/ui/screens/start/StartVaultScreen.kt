package com.example.nestory.ui.screens.start

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.nestory.ui.components.SmallFeatureRow
import com.example.nestory.ui.components.NestoryLogo
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun StartVaultScreen(
    onCreateVault: () -> Unit
) {
    NestoryScreen {
        Spacer(modifier = Modifier.height(NestorySpacing.S40))
        NestoryLogo(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            showName = true,
            centered = true
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S30))
        Text(
            text = "Kho lưu trữ riêng tư của bạn",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Title21Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        Text(
            text = "Lưu trữ, quản lý và bảo vệ tất cả giấy tờ quan trọng ngay trên thiết bị của bạn",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body15Semi,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S30))
        SmallFeatureRow(
            title = "Lưu trữ cục bộ & riêng tư",
            description = "Dữ liệu chỉ nằm trên thiết bị của bạn",
            iconRes = AppIcons.FigmaStorage,
            accent = GeneratedColor.Figma1a60e2
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        SmallFeatureRow(
            title = "Bảo mật & An toàn",
            description = "Bảo vệ bằng khóa ứng dụng và xác thực thiết bị",
            iconRes = AppIcons.FigmaLocation,
            accent = GeneratedColor.Figma1a60e2
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        SmallFeatureRow(
            title = "Luôn sẵn sàng",
            description = "Truy cập mọi lúc, mọi nơi khi bạn cần",
            iconRes = AppIcons.FigmaHeadset,
            accent = GeneratedColor.Figma1a60e2
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        PrimaryActionButton(
            text = "Bắt đầu",
            onClick = onCreateVault
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S10))
        Text(
            text = "Tìm hiểu thêm",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma1a60e2,
            style = NestoryTextStyles.Body17Medium,
            textAlign = TextAlign.Center
        )
    }
}
