package com.example.nestory.ui.screens.document

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun FilterSelectionScreen(
    onBack: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    NestoryScreen(
        verticalPadding = 20.dp, // As per Frame 82 padding
        useStatusBarPadding = true
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Frame (Node 261:46)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clickable { onBack() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Image(
                        painter = painterResource(AppIcons.IcBackwardArrow),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bộ lọc",
                    style = NestoryTextStyles.Title24Semi,
                    color = GeneratedColor.Figma000000
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp)) // Gap from blueprint
            
            // Filter Items Frame (Node 220:501)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FilterItem(
                    label = "Danh mục ",
                    value = "Tất cả",
                    icon = AppIcons.FigmaCategory
                )
                FilterItem(
                    label = "Container / Nơi lưu trữ",
                    value = "Tất cả",
                    icon = AppIcons.DocumentStorage
                )
                FilterItem(
                    label = "Yêu thích",
                    value = "Tất cả",
                    icon = AppIcons.DocumentStarred
                )
                FilterItem(
                    label = "Sắp hết hạn",
                    value = "Tất cả",
                    icon = AppIcons.DocumentFilterExpiring
                )
                FilterItem(
                    label = "Đã hết hạn",
                    value = "Tất cả",
                    icon = AppIcons.DocumentFilterExpired
                )
            }
            
            // Buttons Frame (Node 261:14)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 8.dp), // Adjusting for total layout height
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(NestoryRadius.R10)
                        .background(GeneratedColor.FigmaEdebff)
                        .clickable { onReset() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đặt lại",
                        style = NestoryTextStyles.Body15Semi,
                        color = GeneratedColor.Figma6d28d9
                    )
                }
                
                // Apply Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(NestoryRadius.R10)
                        .background(GeneratedColor.Figma1a60e2)
                        .clickable { onApply() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Áp dụng",
                        style = NestoryTextStyles.Body15Semi,
                        color = GeneratedColor.FigmaFfffff
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterItem(
    label: String,
    value: String,
    icon: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(NestoryRadius.R15)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R15)
            .clickable { /* Handle selection */ }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container (Node 228:18)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaEdebff),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.width(15.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = NestoryTextStyles.Body16Semi,
                color = GeneratedColor.Figma000000
            )
            Text(
                text = value,
                style = NestoryTextStyles.Body14Semi,
                color = GeneratedColor.Figma1a60e2
            )
        }
        
        // Chevron/Arrow (Node 228:37) - Rotated to point right
        Image(
            painter = painterResource(AppIcons.IcBackwardArrow),
            contentDescription = null,
            modifier = Modifier
                .size(width = 12.dp, height = 24.dp)
                .graphicsLayer(rotationZ = 180f),
            contentScale = ContentScale.Fit,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(GeneratedColor.Figma919191)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterSelectionPreview() {
    FilterSelectionScreen(onBack = {}, onApply = {}, onReset = {})
}
