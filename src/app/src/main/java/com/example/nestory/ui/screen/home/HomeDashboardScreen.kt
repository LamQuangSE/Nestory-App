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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryLogo
import com.example.nestory.ui.components.SectionHeader
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun HomeDashboardScreen(
    uiState: HomeDashboardUiState,
    onOpenAll: () -> Unit,
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
                SectionHeader(title = "Cần chú ý", action = "Xem tất cả", onAction = onOpenAll)
                Spacer(modifier = Modifier.height(NestorySpacing.S16))
                EmptyStateLine()
            }
            Spacer(modifier = Modifier.height(NestorySpacing.S24))
            SectionHeader(title = "Gần đây", action = "Xem tất cả", onAction = onOpenAll)
            Spacer(modifier = Modifier.height(NestorySpacing.S14))
            RecentSection(
                documents = uiState.recentDocuments,
                onDocumentClick = onRecentDocumentClick,
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S24))
            SectionHeader(title = "Container của bạn", action = "Xem tất cả", onAction = onOpenAll)
            Spacer(modifier = Modifier.height(NestorySpacing.S14))
            ContainerSection(
                containers = uiState.rootContainers,
                onContainerClick = onOpenAll,
            )
        }
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
private fun ContainerSection(
    containers: List<ContainerEntity>,
    onContainerClick: () -> Unit,
) {
    SectionCard {
        if (containers.isEmpty()) {
            EmptyStateLine()
        } else {
            containers.forEach { container ->
                ContainerRow(
                    container = container,
                    onClick = onContainerClick,
                )
            }
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
            text = document.expiryDate,
            style = NestoryTextStyles.Body11Semi,
            color = GeneratedColor.Figma919191,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ContainerRow(
    container: ContainerEntity,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaF3f6ff)
                .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R10),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(AppIcons.FigmaNavFolder),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S14))
        Text(
            text = container.name,
            style = NestoryTextStyles.Body14Semi,
            color = GeneratedColor.Figma000000,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
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