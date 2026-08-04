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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun DocumentSelectionScreen(
    uiState: DocumentUiState,
    onAddDocument: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    NestoryScreen(
        verticalPadding = 0.dp,
        useStatusBarPadding = true
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(NestorySpacing.S20))
            
            DocumentSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { /* Handle search */ },
                onFilterClick = onFilterClick,
                // No horizontal padding needed here as NestoryScreen handles it
            )

            if (uiState.documents.isEmpty()) {
                DocumentEmptyState(
                    onAddDocument = onAddDocument,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.height(NestorySpacing.S20))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        bottom = 24.dp
                    ),
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
                    status = DocumentStatus.Active,
                    expiryDate = "20/08/2026",
                    categoryColor = Color(0xFF1855EE)
                ),
                DocumentUiModel(
                    id = "2",
                    name = "Giấy xác nhận sinh viên",
                    category = "Chứng từ",
                    containerPath = "Tủ tài liệu > Ngăn 2",
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
fun DocumentEmptyState(
    onAddDocument: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = NestorySpacing.S20),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Image node 243:128
        Box(
            modifier = Modifier.size(212.dp, 217.dp),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for ChatGPT Image
            Image(
                painter = painterResource(AppIcons.FigmaDocument), // Fallback
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        Text(
            text = "Chưa có giấy tờ nào",
            style = NestoryTextStyles.Title22Semi,
            color = GeneratedColor.Figma000000,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        Text(
            text = "Thêm giấy tờ đầu tiên để bắt đầu quản lý trong Nestory",
            style = NestoryTextStyles.Body13Semi,
            color = GeneratedColor.Figma919191,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        // "Scan tại đây" button (Frame 39 in Figma)
        Box(
            modifier = Modifier
                .width(162.dp)
                .height(50.dp)
                .padding(horizontal = 10.dp) // Simulated padding from Frame 39
                .align(Alignment.CenterHorizontally)
        ) {
            // Simple visual for the button
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .clickable { onAddDocument() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Camera icon
                Image(
                    painter = painterResource(AppIcons.DocumentCamera),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(GeneratedColor.Figma1a60e2)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan tại đây",
                    style = NestoryTextStyles.Body13Semi,
                    color = GeneratedColor.Figma1a60e2
                )
            }
        }
    }
}
