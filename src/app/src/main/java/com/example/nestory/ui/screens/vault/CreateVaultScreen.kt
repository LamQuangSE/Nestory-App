package com.example.nestory.ui.screens.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.BackTextButton
import com.example.nestory.ui.components.CreateVaultOptionRow
import com.example.nestory.ui.components.NestoryLogo
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun CreateVaultScreen(
    onBack: () -> Unit,
    onCreateVault: () -> Unit,
) {
    NestoryScreen(
        verticalPadding = NestorySpacing.S18,
        useStatusBarPadding = true,
    ) {
        BackTextButton(onClick = onBack)
        Spacer(modifier = Modifier.height(NestorySpacing.S20))
        NestoryLogo(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            showName = false,
            centered = true,
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        Text(
            text = "Tạo kho lưu trữ cục bộ",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Heading25Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        Text(
            text = "Nestory sẽ tạo một kho lưu trữ riêng trên thiết bị này để lưu tất cả giấy tờ của bạn.",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma717171,
            style = NestoryTextStyles.Body15Semi,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NestoryRadius.R16)
                .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R16)
                .padding(horizontal = NestorySpacing.S20, vertical = NestorySpacing.S16),
        ) {
            CreateVaultOptionRow(title = "Giấy tờ và file scan", iconRes = AppIcons.FigmaDocument)
            CreateVaultOptionRow(title = "Danh mục và vị trí lưu trữ", iconRes = AppIcons.FigmaCategory)
            CreateVaultOptionRow(title = "Nhắc nhở và ghi chú", iconRes = AppIcons.NestoryNote)
            CreateVaultOptionRow(title = "Cấu hình bảo mật", iconRes = AppIcons.FigmaShield)
        }
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Image(
                painter = painterResource(AppIcons.NestoryLockGray),
                contentDescription = null,
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(NestorySpacing.S14))
            Text(
                text = "Dữ liệu của bạn luôn được lưu cục bộ và bạn toàn quyền kiểm soát.",
                color = GeneratedColor.Figma717171,
                style = NestoryTextStyles.Body15Semi,
            )
        }
        Spacer(modifier = Modifier.height(NestorySpacing.S75))
        PrimaryActionButton(
            text = "Tạo kho lưu trữ",
            onClick = onCreateVault,
        )
    }
}
