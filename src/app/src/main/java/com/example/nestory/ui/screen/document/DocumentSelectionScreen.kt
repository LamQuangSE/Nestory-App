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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DocumentSelectionScreen(
    uiState: DocumentUiState,
    onAddDocument: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
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
                Spacer(modifier = Modifier.height(NestorySpacing.S20))
                if (isFilteringOrSearching) {
                    DocumentNotFoundState(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = NestorySpacing.S20)
                    )
                } else {
                    DocumentEmptyState(
                        onAddDocument = onAddDocument,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = NestorySpacing.S20)
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
                            onClick = { onDocumentClick(document.id) },
                            onToggleFavorite = { onToggleFavorite(document.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DocumentSelectionPreview() {
    DocumentSelectionScreen(
        uiState = DocumentUiState(
            documents = listOf(
                DocumentUiModel(
                    id = "1",
                    name = "Hợp đồng thuê nhà 2026",
                    category = "Hợp đồng, Pháp lý",
                    containerPath = "Tủ tài liệu > Ngăn 4",
                    containerId = 1L,
                    status = DocumentStatus.Active,
                    expiryDate = "20/08/2026",
                    categoryColor = Color(0xFF1855EE)
                ),
                DocumentUiModel(
                    id = "2",
                    name = "Giấy xác nhận sinh viên",
                    category = "Chứng từ",
                    containerPath = "Tủ tài liệu > Ngăn 2",
                    containerId = 2L,
                    status = DocumentStatus.ExpiringSoon,
                    expiryDate = "18/07/2026",
                    categoryColor = Color(0xFFEB6E00)
                )
            )
        ),
        onAddDocument = {},
        onDocumentClick = {},
        onFilterClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun DocumentEmptyPreview() {
    DocumentSelectionScreen(
        uiState = DocumentUiState(),
        onAddDocument = {},
        onDocumentClick = {},
        onFilterClick = {}
    )
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(NestoryRadius.R20)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R20)
            .padding(horizontal = NestorySpacing.S20, vertical = NestorySpacing.S24),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = AppImages.EmptyDocument),
                contentDescription = null,
                modifier = Modifier.size(width = 220.dp, height = 170.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S20))
            Text(
                text = "Chưa có giấy tờ nào",
                style = NestoryTextStyles.Title22Semi,
                color = GeneratedColor.Figma000000,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Thêm giấy tờ đầu tiên để bắt đầu\nquản lý trong Nestory",
                style = NestoryTextStyles.Body14Medium,
                color = GeneratedColor.Figma919191,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S24))
            PrimaryActionButton(
                text = "Scan tại đây",
                onClick = onAddDocument,
                leadingIcon = AppIcons.DocumentCamera,
            )
        }
    }
}
