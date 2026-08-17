package com.example.nestory.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryLogo
import com.example.nestory.ui.components.SectionHeader
import com.example.nestory.ui.screen.documentkit.KitProgressRing
import com.example.nestory.ui.screen.documentkit.kitCategoryVisual
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun HomeDashboardScreen(
    uiState: HomeDashboardUiState,
    onOpenDocuments: () -> Unit,
    onOpenKits: () -> Unit,
    onKitClick: (Long) -> Unit = {},
    onAddDocument: () -> Unit,
    onRecentDocumentClick: (Long) -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = GeneratedColor.FigmaFfffff
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NestorySpacing.S20) // Updated to S20
                .padding(top = NestorySpacing.S40, bottom = 24.dp)
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
                SectionHeader(title = "Cần chú ý", action = "Xem tất cả", onAction = onOpenDocuments)
                Spacer(modifier = Modifier.height(NestorySpacing.S16))
                if (uiState.attentionDocuments.isEmpty()) {
                    EmptyStateLine()
                } else {
                    AttentionSection(
                        documents = uiState.attentionDocuments,
                        onDocumentClick = onRecentDocumentClick,
                    )
                }
            }
            Spacer(modifier = Modifier.height(NestorySpacing.S24))
            SectionHeader(title = "Gần đây", action = "Xem tất cả", onAction = onOpenDocuments)
            Spacer(modifier = Modifier.height(NestorySpacing.S14))
            RecentSection(
                documents = uiState.recentDocuments,
                onDocumentClick = onRecentDocumentClick,
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S24))
            SectionHeader(title = "Hồ sơ của bạn", action = "Xem tất cả", onAction = onOpenKits)
            Spacer(modifier = Modifier.height(NestorySpacing.S14))
            KitSection(
                kits = uiState.documentKits,
                onKitClick = onKitClick,
            )
        }
    }
}

@Composable
private fun AttentionSection(
    documents: List<RecentDocumentUi>,
    onDocumentClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NestorySpacing.S12)
    ) {
        documents.forEach { document ->
            AttentionDocumentRow(
                document = document,
                onClick = { onDocumentClick(document.id) }
            )
        }
    }
}

@Composable
private fun AttentionDocumentRow(
    document: RecentDocumentUi,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = NestorySpacing.S4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaF3f6ff)
                .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R10),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(AppIcons.DocumentFileScan),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S12))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = document.title,
                style = NestoryTextStyles.Body14Semi,
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hết hạn: ${document.expiryDate}",
                style = NestoryTextStyles.Body11Semi,
                color = GeneratedColor.Figma919191,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val daysRemaining = document.daysRemaining
            if (daysRemaining != null) {
                Text(
                    text = "Còn $daysRemaining ngày",
                    style = NestoryTextStyles.Body11Semi,
                    color = GeneratedColor.FigmaFf0000,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S8))
        Image(
            painter = painterResource(AppIcons.LsiconRightFilled),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun RecentSection(
    documents: List<RecentDocumentUi>,
    onDocumentClick: (Long) -> Unit,
) {
    if (documents.isEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
        ) {
            EmptyRecentCard(Modifier.weight(1f))
            EmptyRecentCard(Modifier.weight(1f))
            EmptyRecentCard(Modifier.weight(1f))
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
        ) {
            documents.forEach { document ->
                RecentDocumentCard(
                    document = document,
                    onClick = { onDocumentClick(document.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KitSection(
    kits: List<HomeDocumentKitUi>,
    onKitClick: (Long) -> Unit,
) {
    if (kits.isEmpty()) {
        SectionCard {
            EmptyStateLine()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NestorySpacing.S12)
        ) {
            kits.forEach { kit ->
                KitRow(
                    kit = kit,
                    onClick = { onKitClick(kit.id) },
                )
            }
        }
    }
}

@Composable
private fun KitRow(
    kit: HomeDocumentKitUi,
    onClick: () -> Unit,
) {
    val categoryVisual = kitCategoryVisual(kit.category)
    val remaining = kit.totalItems - kit.readyItems
    val remainingText = if (remaining <= 0) "Đã hoàn thành"
    else "Còn $remaining mục chưa hoàn thành"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(NestoryRadius.R14)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R14)
            .clickable(onClick = onClick)
            .padding(horizontal = NestorySpacing.S12, vertical = NestorySpacing.S10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 72.dp)
                .clip(RoundedCornerShape(NestorySpacing.S10))
                .background(categoryVisual.boxColor)
                .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), RoundedCornerShape(NestorySpacing.S10)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = categoryVisual.iconRes),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = categoryVisual.iconTint
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S12))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = kit.name,
                style = NestoryTextStyles.Body14Semi,
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${kit.totalItems} mục yêu cầu",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.Figma919191,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = remainingText,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.Figma919191,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S10))
        KitProgressRing(percent = kit.progressPercent)
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NestoryLogo()
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
private fun RecentDocumentCard(
    document: RecentDocumentUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(115.dp) // Slightly increased height for better preview
            .clip(NestoryRadius.R14)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R14)
            .padding(NestorySpacing.S8)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaEdebff)
                .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R10),
            contentAlignment = Alignment.Center
        ) {
            val firstAttachment = document.attachmentUris.firstOrNull()
            if (firstAttachment != null) {
                if (firstAttachment.endsWith(".pdf", ignoreCase = true)) {
                    Image(
                        painter = painterResource(AppIcons.DocumentFileScan),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = firstAttachment,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(AppIcons.FigmaDocument)
                    )
                }
            } else {
                Text(
                    text = document.categoryLabel.take(1),
                    style = NestoryTextStyles.Body14Semi,
                    color = GeneratedColor.Figma522ec8
                )
            }
        }
        Spacer(modifier = Modifier.height(NestorySpacing.S8))
        Text(
            text = document.title,
            style = NestoryTextStyles.Body12Semi,
            color = GeneratedColor.Figma000000,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S4))
        Text(
            text = document.categoryLabel,
            style = NestoryTextStyles.Body11Semi,
            color = GeneratedColor.Figma919191,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
