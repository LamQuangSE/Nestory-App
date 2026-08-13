package com.example.nestory.ui.screen.documentkit

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun DocumentKitItemDetailScreen(
    item: KitItemEntity,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    linkedDocumentTitle: String? = null,
    onLinkedDocumentClick: () -> Unit = {},
) {
    val visual = kitStatusVisual(item.status)
    val linkedCount = if (item.linkedDocumentId == null) 0 else 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeneratedColor.FigmaFfffff)
            .statusBarsPadding()
            .padding(horizontal = NestorySpacing.S12)
            .padding(vertical = 7.dp)
    ) {
        KitTopBar(
            title = "Chi tiết Item",
            onBack = onBackClick,
            trailingContent = {
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = AppIcons.DocumentEdit),
                        contentDescription = "Sửa",
                        modifier = Modifier.size(20.dp),
                        tint = GeneratedColor.Figma000000
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            KitItemHeaderCard(item = item, visual = visual)

            KitItemInfoCard(
                description = item.description.orEmpty(),
                note = item.note.orEmpty(),
                visual = visual
            )

            KitItemLinkedDocsCard(
                linkedCount = linkedCount,
                linkedDocumentTitle = if (linkedCount > 0) linkedDocumentTitle else null,
                visual = visual,
                onLinkedDocumentClick = onLinkedDocumentClick
            )
        }
    }
}

@Composable
private fun KitItemHeaderCard(
    item: KitItemEntity,
    visual: KitStatusVisual,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(14.dp))
            .padding(horizontal = NestorySpacing.S10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        Box(
            modifier = Modifier
                .size(width = 77.dp, height = 100.dp)
                .clip(RoundedCornerShape(NestorySpacing.S10))
                .background(visual.bgColor, RoundedCornerShape(NestorySpacing.S10))
                .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(NestorySpacing.S10))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = visual.iconRes),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = visual.iconTint
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
        ) {
            Text(
                text = item.name?.ifBlank { "Item chưa đặt tên" } ?: "Item chưa đặt tên",
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            KitStatusChip(status = item.status)
        }
    }
}

@Composable
private fun KitItemInfoCard(
    description: String,
    note: String,
    visual: KitStatusVisual,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S10, vertical = NestorySpacing.S6),
        verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        KitSectionHeader(
            title = "Mô tả",
            iconRes = AppIcons.KitAlignLeft,
            iconBoxColor = visual.bgColor,
            iconTint = visual.iconTint
        )
        KitItemField(value = description)
        KitSectionHeader(
            title = "Ghi chú",
            iconRes = AppIcons.NestoryNote,
            iconBoxColor = visual.bgColor,
            iconTint = visual.iconTint
        )
        KitItemField(value = note, height = 65.dp)
    }
}

@Composable
private fun KitItemLinkedDocsCard(
    linkedCount: Int,
    linkedDocumentTitle: String?,
    visual: KitStatusVisual,
    onLinkedDocumentClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S8, vertical = NestorySpacing.S8),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        KitSectionHeader(
            title = "Giấy tờ đã liên kết ($linkedCount)",
            iconRes = AppIcons.KitFile,
            iconBoxColor = visual.bgColor,
            iconTint = visual.iconTint
        )
        if (linkedCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(5.dp))
                    .clickable(onClick = onLinkedDocumentClick)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = linkedDocumentTitle?.ifBlank { "Giấy tờ đã liên kết" }
                        ?: "Giấy tờ đã liên kết",
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma000000,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = AppIcons.IcMoreInfor),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = GeneratedColor.Figma000000
                )
            }
        }
    }
}

@Composable
private fun KitItemField(
    value: String,
    height: androidx.compose.ui.unit.Dp = 31.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(5.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = value.ifBlank { "Nhập nội dung" },
            style = NestoryTextStyles.Body14Medium,
            color = if (value.isBlank()) GeneratedColor.Figma919191 else GeneratedColor.Figma000000,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
