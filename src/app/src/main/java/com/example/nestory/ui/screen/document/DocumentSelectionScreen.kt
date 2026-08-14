package com.example.nestory.ui.screen.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun DocumentSelectionScreen(
    uiState: DocumentUiState,
    onAddDocument: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
) {
    NestoryScreen(
        verticalPadding = 0.dp,
        useStatusBarPadding = true
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(NestorySpacing.S20))
            
            DocumentSearchBar(
                query = uiState.searchQuery,
                isFilterActive = uiState.activeFilter.isActive,
                onQueryChange = onSearchQueryChange,
                onFilterClick = onFilterClick,
            )

            if (uiState.documents.isEmpty()) {
                val isFilteringOrSearching = uiState.searchQuery.isNotBlank() || uiState.activeFilter.isActive
                if (isFilteringOrSearching) {
                    DocumentNotFoundState(modifier = Modifier.weight(1f))
                } else {
                    DocumentEmptyState(
                        onAddDocument = onAddDocument,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(NestorySpacing.S20))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(NestorySpacing.S20)
                ) {
                    items(uiState.documents) { document ->
                        DocumentListItem(
                            document = document,
                            onClick = { onDocumentClick(document.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentNotFoundState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = NestorySpacing.S20),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(AppImages.EmptyDocument), // Tạm dùng ảnh fallback
            contentDescription = null,
            modifier = Modifier.size(212.dp, 217.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        Text(
            text = "Không có giấy tờ phù hợp",
            style = NestoryTextStyles.Title22Semi,
            color = GeneratedColor.Figma000000,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Hãy kiểm tra lại từ khóa hoặc thử thay đổi các tiêu chí bộ lọc",
            style = NestoryTextStyles.Body13Semi,
            color = GeneratedColor.Figma919191,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(278.dp)
        )
    }
}

@Composable
fun DocumentEmptyState(
    onAddDocument: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Giữ nguyên phần DocumentEmptyState cũ của bạn
}