package com.example.nestory.ui.screen.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier,
    isFilterActive: Boolean = false

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
        
        // Thêm cục chấm báo hiệu đang có bộ lọc Active
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onFilterClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(AppIcons.DocumentConfig),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
            if (isFilterActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
            }
        }
    }
}

@Composable
fun DocumentListItem(
    document: DocumentUiModel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(NestoryRadius.R14)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb.copy(alpha = 0.72f), NestoryRadius.R14)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail section - Show image content directly
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(NestoryRadius.R10)
                .background(document.categoryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            val firstAttachment = document.attachmentUris.firstOrNull()
            if (firstAttachment != null) {
                if (firstAttachment.endsWith(".pdf", ignoreCase = true)) {
                    Image(
                        painter = painterResource(AppIcons.DocumentFileScan),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = firstAttachment,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(AppIcons.FigmaDocument)
                    )
                }
            } else {
                Image(
                    painter = painterResource(AppIcons.FigmaDocument),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = document.name,
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = document.category,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.Figma919191
            )
            Text(
                text = document.containerPath,
                style = NestoryTextStyles.Body11Semi,
                color = GeneratedColor.Figma919191,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
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

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                painter = painterResource(
                    id = if (document.isFavorite) AppIcons.KitStarred else AppIcons.KitUnstarred
                ),
                contentDescription = if (document.isFavorite) "Bỏ đánh dấu yêu thích" else "Đánh dấu yêu thích",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onToggleFavorite),
                tint = if (document.isFavorite) GeneratedColor.Figma1a60e2 else GeneratedColor.Figma919191
            )
        }
    }
}

@Composable
fun NestoryCheckboxRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clickable { onCheckedChange() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Image(
                    painter = painterResource(AppIcons.NestoryTickCircle),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(2.dp, Color(0xFFD9D9D9), RoundedCornerShape(2.dp))
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = NestoryTextStyles.Body14Semi.copy(fontWeight = FontWeight.W400),
            color = GeneratedColor.Figma000000
        )
    }
}

@Composable
fun SelectionListItem(
    label: String,
    icon: Int? = null,           // Cho phép null
    categoryColor: Color? = null, // Thêm màu sắc danh mục
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(NestoryRadius.R10)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hộp chứa Icon hoặc Chấm màu
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = if (categoryColor != null) Color.Transparent else GeneratedColor.FigmaEdebff, 
                    shape = NestoryRadius.R10
                ),
            contentAlignment = Alignment.Center
        ) {
            if (categoryColor != null) {
                // Nếu là Danh mục -> Hiển thị chấm màu giống CategoryListItem của bạn
                Box(
                    modifier = Modifier
                        .size(29.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
            } else if (icon != null) {
                // Nếu là Container -> Hiển thị Icon
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(GeneratedColor.Figma1a60e2)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = NestoryTextStyles.Body16Semi,
            color = GeneratedColor.Figma000000,
            modifier = Modifier.weight(1f)
        )
        
        if (isSelected) {
            Image(
                painter = painterResource(AppIcons.NestoryTickCircle),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(4.dp, GeneratedColor.FigmaD0cfd1, CircleShape)
            )
        }
    }
}

@Composable
fun SelectionRadioItem(
    label: String,
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(NestoryRadius.R10)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon bên trái
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(GeneratedColor.FigmaF3f6ff, NestoryRadius.R10),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(GeneratedColor.Figma1a60e2)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        
        // Tên
        Text(
            text = label,
            style = NestoryTextStyles.Body16Semi,
            color = GeneratedColor.Figma000000,
            modifier = Modifier.weight(1f)
        )
        
        // Radio Button bên phải
        if (isSelected) {
            Image(
                painter = painterResource(AppIcons.NestoryTickCircle),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(1.5.dp, GeneratedColor.FigmaD0cfd1, CircleShape)
            )
        }
    }
}