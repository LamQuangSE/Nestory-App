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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun FilterCategoryScreen(
    uiState: DocumentUiState,
    onBack: () -> Unit,
    onCategorySelected: (String?) -> Unit // Đã đổi truyền ID dạng String
) {
    var searchQuery by remember { mutableStateOf("") }
    var localSelectedCategoryId by remember { mutableStateOf(uiState.draftFilter.selectedCategoryId) }
    
    // Lọc danh sách danh mục THẬT từ Database
    val filteredCategories = uiState.availableCategories.filter { 
        it.name.contains(searchQuery, ignoreCase = true) 
    }

    NestoryScreen(verticalPadding = 20.dp, useStatusBarPadding = true) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(AppIcons.IcBackwardArrow), contentDescription = null,
                    modifier = Modifier.size(20.dp).clickable { onBack() }
                )
                Text(
                    text = "Chọn danh mục", style = NestoryTextStyles.Title20Semi,
                    modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Image(
                    painter = painterResource(AppIcons.GridiconsCross), contentDescription = null,
                    modifier = Modifier.size(24.dp).clickable { onBack() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            DocumentSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, onFilterClick = {}, isFilterActive = false)
            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    SelectionListItem(
                        label = "Tất cả", 
                        icon = AppIcons.IcFilterDocument, 
                        isSelected = localSelectedCategoryId == null,
                        onClick = { localSelectedCategoryId = null }
                    )
                }
                items(filteredCategories) { category ->
                    SelectionListItem(
                        label = category.name,
                        categoryColor = category.color, // Truyền màu vào để vẽ chấm tròn
                        isSelected = localSelectedCategoryId == category.id,
                        onClick = { localSelectedCategoryId = category.id }
                    )
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clip(NestoryRadius.R10).border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                        .clickable { localSelectedCategoryId = null },
                    contentAlignment = Alignment.Center
                ) { Text("Đặt lại", style = NestoryTextStyles.Body15Semi, color = GeneratedColor.Figma000000) }
                
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clip(NestoryRadius.R10).background(GeneratedColor.Figma1a60e2)
                        .clickable { onCategorySelected(localSelectedCategoryId) },
                    contentAlignment = Alignment.Center
                ) { Text("Áp dụng", style = NestoryTextStyles.Body15Semi, color = GeneratedColor.FigmaFfffff) }
            }
        }
    }
}