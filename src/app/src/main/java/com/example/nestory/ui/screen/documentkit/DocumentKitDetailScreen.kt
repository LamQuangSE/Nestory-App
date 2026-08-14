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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestory.relation.KitWithItems
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import java.util.Locale

@Composable
fun DocumentKitDetailScreen(
    state: KitProgressUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewAllItemsClick: () -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeneratedColor.FigmaFfffff)
            .statusBarsPadding()
            .padding(horizontal = NestorySpacing.S12)
            .padding(vertical = 7.dp)
    ) {
        KitTopBar(
            title = "Chi tiết kit",
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

        when {
            state.isLoading -> KitDetailLoadingState(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            state.error != null -> KitDetailErrorState(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            state.kit == null -> KitDetailErrorState(
                message = "Không tìm thấy bộ hồ sơ",
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            else -> KitDetailContent(
                state = state,
                onViewAllItemsClick = onViewAllItemsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun KitDetailContent(
    state: KitProgressUiState,
    onViewAllItemsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val kit = state.kit!!
    val linkedCount = state.items.count { it.linkedDocumentId != null }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        KitDetailHeaderCard(kit = kit)

        KitProgressCard(
            progress = state.progressPercent,
            readyCount = state.readyCount,
            total = state.totalItems
        )

        KitDistributionCard(
            distribution = state.statusDistribution,
            total = state.totalItems,
            onViewAllItemsClick = onViewAllItemsClick
        )

        KitLinkedDocumentsCard(count = linkedCount)

        KitDetailInfoCard(
            description = kit.kit.description.orEmpty(),
            note = kit.kit.note.orEmpty()
        )
    }
}

@Composable
private fun KitDetailLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = GeneratedColor.Figma522ec8,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun KitDetailErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(NestorySpacing.S24), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = NestoryTextStyles.Body14Medium,
                color = GeneratedColor.Figma919191,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S10))
            Box(
                modifier = Modifier
                    .background(GeneratedColor.FigmaF3eeff, RoundedCornerShape(NestorySpacing.S10))
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Thử lại",
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma522ec8
                )
            }
        }
    }
}

@Composable
private fun KitDetailHeaderCard(kit: KitWithItems) {
    val categoryVisual = kitCategoryVisual(kit.kit.category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(14.dp))
            .padding(horizontal = NestorySpacing.S10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        Box(
            modifier = Modifier
                .size(width = 77.dp, height = 100.dp)
                .clip(RoundedCornerShape(NestorySpacing.S10))
                .background(categoryVisual.boxColor, RoundedCornerShape(NestorySpacing.S10))
                .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(NestorySpacing.S10)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = categoryVisual.iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = categoryVisual.iconTint
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
        ) {
            Text(
                text = kit.kit.name,
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ngày sử dụng: ",
                    style = NestoryTextStyles.Body12Semi,
                    color = GeneratedColor.Figma000000
                )
                Text(
                    text = kit.kit.targetCompletionDate,
                    style = NestoryTextStyles.Body12Semi.copy(fontWeight = FontWeight.W400),
                    color = GeneratedColor.Figma000000
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    painter = painterResource(id = AppIcons.KitCalendar),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = GeneratedColor.Figma000000
                )
            }
        }
    }
}

@Composable
private fun KitProgressCard(
    progress: Int,
    readyCount: Int,
    total: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S10, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tiến độ hoàn thành",
                style = NestoryTextStyles.Body16Bold,
                color = GeneratedColor.Figma000000
            )
            Text(
                text = "$progress%",
                style = NestoryTextStyles.Body20Bold.copy(fontWeight = FontWeight.W600),
                color = GeneratedColor.Figma000000
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(19.dp)
                .background(GeneratedColor.FigmaE5e7eb, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0, 100) / 100f)
                    .height(19.dp)
                    .background(GeneratedColor.Figma522ec8, RoundedCornerShape(5.dp))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$readyCount/$total mục đã được hoàn thành",
                style = NestoryTextStyles.Body11Semi.copy(fontWeight = FontWeight.W500),
                color = GeneratedColor.Figma000000
            )
        }
    }
}

@Composable
private fun KitDistributionCard(
    distribution: List<Pair<String, Int>>,
    total: Int,
    onViewAllItemsClick: () -> Unit,
) {
    val visible = distribution.filter { entry ->
        entry.second > 0 || entry.first != KitItemStatus.EXPIRED
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(NestorySpacing.S8),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        KitSectionHeader(
            title = "Phân bố trạng thái kit items",
            iconRes = AppIcons.KitCategory
        )
        visible.forEach { (status, count) ->
            val visual = kitStatusVisual(status)
            val percent = if (total == 0) 0f else count.toFloat() / total * 100f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
            ) {
                Icon(
                    painter = painterResource(id = visual.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = visual.iconTint
                )
                Text(
                    text = visual.label,
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma000000,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count",
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma000000
                )
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = String.format(Locale.ROOT, "%.1f%%", percent),
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma000000,
                    modifier = Modifier.width(48.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewAllItemsClick)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Xem tất cả",
                style = NestoryTextStyles.Body14Medium,
                color = GeneratedColor.Figma000000
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = AppIcons.LsiconRightFilled),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = GeneratedColor.Figma000000
            )
        }
    }
}

@Composable
private fun KitLinkedDocumentsCard(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S10, vertical = NestorySpacing.S6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(NestorySpacing.S10))
                .background(GeneratedColor.FigmaF3eeff, RoundedCornerShape(NestorySpacing.S10))
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = AppIcons.KitFile),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = GeneratedColor.Figma522ec8
            )
        }
        Text(
            text = "Giấy tờ đã liên kết",
            style = NestoryTextStyles.Body14Medium,
            color = GeneratedColor.Figma000000,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$count",
            style = NestoryTextStyles.Body14Medium,
            color = GeneratedColor.Figma000000
        )
    }
}

@Composable
private fun KitDetailInfoCard(
    description: String,
    note: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S10, vertical = NestorySpacing.S6),
        verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        KitSectionHeader(
            title = "Mục đích",
            iconRes = AppIcons.KitTarget
        )
        KitDetailField(value = description)
        KitSectionHeader(
            title = "Ghi chú",
            iconRes = AppIcons.NestoryNote
        )
        KitDetailField(value = note)
    }
}

@Composable
private fun KitDetailField(value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(5.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = value.ifBlank { "Nhập nội dung" },
            style = NestoryTextStyles.Body14Medium,
            color = if (value.isBlank()) GeneratedColor.Figma919191 else GeneratedColor.Figma000000
        )
    }
}
