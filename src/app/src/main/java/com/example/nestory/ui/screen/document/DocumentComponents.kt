package com.example.nestory.ui.screen.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(NestoryRadius.R10)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
            .padding(horizontal = NestorySpacing.S12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(AppIcons.IcSearch),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(NestorySpacing.S8))
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = NestoryTextStyles.Body11Semi.copy(
                    color = GeneratedColor.Figma000000,
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Tìm theo tên, danh mục hoặc ghi chú",
                            style = NestoryTextStyles.Body11Semi,
                            color = GeneratedColor.Figma919191
                        )
                    }
                    innerTextField()
                },
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S8))
        Image(
            painter = painterResource(AppIcons.DocumentConfig),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onFilterClick),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun DocumentListItem(
    document: DocumentUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(122.dp)
            .clip(NestoryRadius.R14)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R14)
            .clickable(onClick = onClick)
            .padding(NestorySpacing.S12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon or Thumbnail box
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(NestoryRadius.R10)
                .background(document.categoryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (document.attachmentUris.isNotEmpty()) {
                AsyncImage(
                    model = document.attachmentUris.first(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(AppIcons.FigmaDocument),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S16))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = document.name,
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = document.category,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.Figma919191
            )
            Text(
                text = document.containerPath,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.Figma919191
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = when (document.status) {
                    DocumentStatus.Active -> Color(0xFF137C23)
                    DocumentStatus.ExpiringSoon -> Color(0xFFEB6E00)
                    DocumentStatus.Expired -> Color(0xFFFF0000)
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (document.status) {
                        DocumentStatus.Active -> "Còn hiệu lực"
                        DocumentStatus.ExpiringSoon -> "Sắp hết hạn"
                        DocumentStatus.Expired -> "Đã hết hạn"
                    },
                    style = NestoryTextStyles.Body12Semi,
                    color = statusColor
                )
            }
        }
        Text(
            text = document.expiryDate,
            style = NestoryTextStyles.Body12Semi,
            color = GeneratedColor.Figma919191,
            modifier = Modifier.align(Alignment.Bottom)
        )
    }
}
