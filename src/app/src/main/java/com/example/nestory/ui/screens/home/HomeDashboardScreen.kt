package com.example.nestory.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryLogo
import com.example.nestory.ui.components.SectionHeader
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun HomeDashboardScreen(
    onOpenAll: () -> Unit,
    onAddDocument: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = GeneratedColor.FigmaFfffff
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = NestorySpacing.S24)
                    .padding(top = NestorySpacing.S40, bottom = 96.dp)
            ) {
                Header()
                Spacer(modifier = Modifier.height(NestorySpacing.S30))
                Text(
                    text = "Chào bạn!",
                    color = GeneratedColor.Figma000000,
                    style = NestoryTextStyles.Body18Semi,
                    fontWeight = FontWeight.W600
                )
                Spacer(modifier = Modifier.height(NestorySpacing.S6))
                Text(
                    text = "Đây là những thông tin quan trọng của bạn.",
                    color = GeneratedColor.Figma919191,
                    style = NestoryTextStyles.Body13Semi
                )
                Spacer(modifier = Modifier.height(NestorySpacing.S24))
                SectionCard {
                    SectionHeader(title = "Cần chú ý", action = "Xem tất cả", onAction = onOpenAll)
                    Spacer(modifier = Modifier.height(NestorySpacing.S16))
                    EmptyStateLine()
                }
                Spacer(modifier = Modifier.height(NestorySpacing.S24))
                SectionHeader(title = "Gần đây", action = "Xem tất cả", onAction = onOpenAll)
                Spacer(modifier = Modifier.height(NestorySpacing.S14))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
                ) {
                    EmptyRecentCard(Modifier.weight(1f))
                    EmptyRecentCard(Modifier.weight(1f))
                    EmptyRecentCard(Modifier.weight(1f))
                    EmptyRecentCard(Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(NestorySpacing.S24))
                SectionHeader(title = "Container của bạn", action = "Xem tất cả", onAction = onOpenAll)
                Spacer(modifier = Modifier.height(NestorySpacing.S14))
                SectionCard {
                    EmptyStateLine()
                }
            }
            BottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                onAddDocument = onAddDocument
            )
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NestoryLogo()
        Image(
            painter = painterResource(AppIcons.NestoryNotification),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NestoryRadius.R14,
        colors = CardDefaults.cardColors(containerColor = GeneratedColor.FigmaFfffff),
        border = BorderStroke(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f))
    ) {
        Column(modifier = Modifier.padding(NestorySpacing.S16), content = content)
    }
}

@Composable
private fun EmptyRecentCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(106.dp)
            .clip(NestoryRadius.R14)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R14)
            .padding(NestorySpacing.S8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaFfffff)
                .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R10)
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S8))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(8.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaE5e7eb.copy(alpha = 0.7f))
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S6))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.54f)
                .height(7.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaE5e7eb.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun EmptyStateLine() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaFfffff)
                .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R10)
        )
        Spacer(modifier = Modifier.width(NestorySpacing.S14))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(10.dp)
                    .clip(NestoryRadius.R10)
                    .background(GeneratedColor.FigmaE5e7eb.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S8))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(8.dp)
                    .clip(NestoryRadius.R10)
                    .background(GeneratedColor.FigmaE5e7eb.copy(alpha = 0.55f))
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S14))
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(GeneratedColor.FigmaE5e7eb.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    onAddDocument: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb)
            .padding(horizontal = NestorySpacing.S16),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(AppIcons.FigmaNavHome, "Trang chủ", true)
        NavItem(AppIcons.FigmaNavDocument, "Giấy tờ", false)
        Column(
            modifier = Modifier.clickable(onClick = onAddDocument),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GeneratedColor.FigmaFfffff),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(AppIcons.FigmaNavAdd),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = "Scan",
                color = GeneratedColor.Figma1a60e2,
                style = NestoryTextStyles.Body12Bold,
                fontWeight = FontWeight.W600
            )
        }
        NavItem(AppIcons.FigmaNavFolder, "Bộ hồ sơ", false)
        NavItem(AppIcons.FigmaNavSettings, "Cài đặt", false)
    }
}

@Composable
private fun NavItem(
    @androidx.annotation.DrawableRes icon: Int,
    label: String,
    selected: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = label,
            color = if (selected) GeneratedColor.Figma1a60e2 else GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body12Bold,
            fontWeight = FontWeight.W600,
            textAlign = TextAlign.Center
        )
    }
}
