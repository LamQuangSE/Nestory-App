package com.example.nestory.ui.screen.documentkit

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.screen.document.FilterNavigationItem
import com.example.nestory.ui.screen.document.FilterSectionBlock
import com.example.nestory.ui.screen.document.NestoryCheckboxRow
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun KitFilterSelectionScreen(
    uiState: DocumentKitUiState,
    onBack: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onCategoryClick: () -> Unit,
    onFavoriteToggle: (Boolean?) -> Unit,
    onStatusToggle: (KitUsageStatus) -> Unit
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

                // Group 1: Navigation Filters (Loại hồ sơ)
                FilterNavigationItem(
                    label = "Loại hồ sơ ",
                    value = draft.selectedCategory ?: "Tất cả",
                    icon = AppIcons.IcFilterDocument,
                    onClick = onCategoryClick
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
                    KitUsageStatus.entries.forEach { status ->
                        NestoryCheckboxRow(
                            label = status.label,
                            isChecked = draft.usageStatuses.contains(status),
                            onCheckedChange = { onStatusToggle(status) }
                        )
                    }
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
