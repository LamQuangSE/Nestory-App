package com.example.nestory.ui.screen.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun FilterContainerScreen(
    uiState: DocumentUiState,
    onBack: () -> Unit,
    onContainerSelected: (Long?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Stack lưu lịch sử đường dẫn Container (null = Root)
    var parentStack by remember { mutableStateOf(listOf<Long?>()) }
    var currentParentId by remember { mutableStateOf<Long?>(null) }
    
    var localSelectedContainer by remember { mutableStateOf(uiState.draftFilter.selectedContainerId) }
    
    val currentLevelContainers = uiState.availableContainers.filter { 
        it.parentId == currentParentId && it.name.contains(searchQuery, ignoreCase = true) 
    }

    val selectedContainerModel = uiState.availableContainers.find { it.id == localSelectedContainer }
    val canViewDetails = selectedContainerModel?.hasChildren == true

    val handleBack = {
        if (parentStack.isNotEmpty()) {
            currentParentId = parentStack.last()
            parentStack = parentStack.dropLast(1)
            localSelectedContainer = null
        } else {
            onBack()
        }
    }

    NestoryScreen(verticalPadding = 20.dp, useStatusBarPadding = true) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            val headerTitle = if (currentParentId == null) "Chọn Container" else uiState.availableContainers.find { it.id == currentParentId }?.fullPath ?: "Chọn Container"
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(AppIcons.IcBackwardArrow), contentDescription = null, modifier = Modifier.size(20.dp).clickable { handleBack() })
                Text(text = headerTitle, style = NestoryTextStyles.Title20Semi, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Image(painter = painterResource(AppIcons.GridiconsCross), contentDescription = null, modifier = Modifier.size(24.dp).clickable { onBack() })
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Header Folder Info (Chỉ hiện khi ở trong Folder)
            if (currentParentId != null) {
                val parentModel = uiState.availableContainers.find { it.id == currentParentId }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(60.dp).background(GeneratedColor.FigmaF3f6ff, NestoryRadius.R10), contentAlignment = Alignment.Center) {
                        Image(painter = painterResource(AppIcons.IcFilterFolder), contentDescription = null, modifier = Modifier.size(32.dp), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(GeneratedColor.Figma1a60e2))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = parentModel?.name ?: "", style = NestoryTextStyles.Body16Semi, color = GeneratedColor.Figma000000)
                        Text(text = "${parentModel?.childFolderCount ?: 0} folder con", style = NestoryTextStyles.Body14Semi.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W400), color = GeneratedColor.Figma919191)
                        Text(text = "${parentModel?.documentCount ?: 0} tài liệu", style = NestoryTextStyles.Body14Semi.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W400), color = GeneratedColor.Figma919191)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Folder:", style = NestoryTextStyles.Body16Semi, color = GeneratedColor.Figma000000)
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                DocumentSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, onFilterClick = {}, isFilterActive = false)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // List
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (currentParentId == null) {
                    item {
                        SelectionRadioItem(
                            label = "Tất cả", icon = AppIcons.IcFilterCalendar, 
                            isSelected = localSelectedContainer == null,
                            onClick = { localSelectedContainer = null }
                        )
                    }
                }
                items(currentLevelContainers) { container ->
                    SelectionRadioItem(
                        label = container.name, icon = AppIcons.IcFilterFolder,
                        isSelected = localSelectedContainer == container.id,
                        onClick = { localSelectedContainer = container.id }
                    )
                }
            }

            // Bottom Buttons
            Row(modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // 1. Nút Chi tiết
                val detailBg = if (canViewDetails) Color(0xFF6155F5) else GeneratedColor.FigmaF3f6ff
                val detailTextColor = if (canViewDetails) Color.White else GeneratedColor.FigmaC2c2c4
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(NestoryRadius.R10).background(detailBg)
                        .clickable(enabled = canViewDetails) {
                            parentStack = parentStack + currentParentId
                            currentParentId = localSelectedContainer
                            localSelectedContainer = null
                        },
                    contentAlignment = Alignment.Center
                ) { Text("Chi tiết", style = NestoryTextStyles.Body15Semi, color = detailTextColor) }

                // 2. Nút Đặt lại (Chỉ ở Root)
                if (currentParentId == null) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().clip(NestoryRadius.R10).border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                            .clickable { localSelectedContainer = null },
                        contentAlignment = Alignment.Center
                    ) { Text("Đặt lại", style = NestoryTextStyles.Body15Semi, color = GeneratedColor.Figma000000) }
                }

                // 3. Nút Áp dụng
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(NestoryRadius.R10).background(GeneratedColor.Figma1a60e2)
                        .clickable { 
                            // Nếu đang trong Folder mà chọn "Tất cả" (null), thì Áp dụng chính là chọn cái Parent hiện tại
                            val appliedId = if (localSelectedContainer == null) currentParentId else localSelectedContainer
                            onContainerSelected(appliedId) 
                        },
                    contentAlignment = Alignment.Center
                ) { Text("Áp dụng", style = NestoryTextStyles.Body15Semi, color = GeneratedColor.FigmaFfffff) }
            }
        }
    }
}