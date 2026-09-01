package com.example.nestory.ui.screen.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun FilterSelectionScreen(
    uiState: DocumentUiState,
    onBack: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onCategoryClick: () -> Unit,
    onContainerClick: () -> Unit,
    onFavoriteToggle: (Boolean?) -> Unit,
    onStatusToggle: (DocumentStatus) -> Unit
) {
    val draft = uiState.draftFilter

    NestoryScreen(verticalPadding = 20.dp, useStatusBarPadding = true) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().height(65.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bộ lọc",
                    style = NestoryTextStyles.Title24Semi,
                    color = GeneratedColor.Figma000000,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.size(24.dp).clickable { onBack() }) {
                    Image(
                        painter = painterResource(AppIcons.GridiconsCross),
                        contentDescription = "Close",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(modifier = Modifier.weight(1f).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                
                // Group 1: Navigation Filters (Danh mục & Container)
                FilterNavigationItem(
                    label = "Danh mục ",
                    value = uiState.availableCategories.find { it.id == draft.selectedCategoryId }?.name ?: "Tất cả",
                    icon = AppIcons.IcFilterDocument,
                    onClick = onCategoryClick
                )
                FilterNavigationItem(
                    label = "Container",
                    value = uiState.availableContainers.find { it.id == draft.selectedContainerId }?.fullPath ?: "Tất cả",
                    icon = AppIcons.IcFilterCalendar,
                    onClick = onContainerClick
                )

                // Group 2: Yêu thích (Radio-like behavior with Checkbox UI)
                FilterSectionBlock(title = "Yêu thích") {
                    NestoryCheckboxRow(
                        label = "Có",
                        isChecked = draft.isFavorite == true,
                        onCheckedChange = { if (draft.isFavorite == true) onFavoriteToggle(null) else onFavoriteToggle(true) }
                    )
                    NestoryCheckboxRow(
                        label = "Không",
                        isChecked = draft.isFavorite == false,
                        onCheckedChange = { if (draft.isFavorite == false) onFavoriteToggle(null) else onFavoriteToggle(false) }
                    )
                }

                // Group 3: Trạng thái (Multi-selection)
                FilterSectionBlock(title = "Trạng thái\n") {
                    NestoryCheckboxRow(
                        label = "Còn hiệu lực",
                        isChecked = draft.statuses.contains(DocumentStatus.Active),
                        onCheckedChange = { onStatusToggle(DocumentStatus.Active) }
                    )
                    NestoryCheckboxRow(
                        label = "Sắp hết hạn (30 ngày)",
                        isChecked = draft.statuses.contains(DocumentStatus.ExpiringSoon),
                        onCheckedChange = { onStatusToggle(DocumentStatus.ExpiringSoon) }
                    )
                    NestoryCheckboxRow(
                        label = "Đã hết hạn",
                        isChecked = draft.statuses.contains(DocumentStatus.Expired),
                        onCheckedChange = { onStatusToggle(DocumentStatus.Expired) }
                    )
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                        .clickable { onReset() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Đặt lại", style = NestoryTextStyles.Body15Semi, color = GeneratedColor.Figma000000)
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .background(GeneratedColor.Figma1a60e2, NestoryRadius.R10)
                        .clickable { onApply() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Áp dụng", style = NestoryTextStyles.Body15Semi, color = GeneratedColor.FigmaFfffff)
                }
            }
        }
    }
}

@Composable
internal fun FilterNavigationItem(label: String, value: String, icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(76.dp).clip(NestoryRadius.R10)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
            .clickable { onClick() }.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).background(GeneratedColor.FigmaEdebff, NestoryRadius.R10),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = NestoryTextStyles.Body16Semi, color = GeneratedColor.Figma000000)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = NestoryTextStyles.Body14Semi, color = GeneratedColor.Figma000000)
        }
        Image(
            painter = painterResource(AppIcons.IcBackwardArrow), contentDescription = null,
            modifier = Modifier.size(12.dp, 24.dp).graphicsLayer(rotationZ = 180f),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(GeneratedColor.Figma919191)
        )
    }
}

@Composable
internal fun FilterSectionBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(NestoryRadius.R10)
            .background(GeneratedColor.FigmaFfffff).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(text = title, style = NestoryTextStyles.Body16Semi, color = GeneratedColor.Figma000000)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}