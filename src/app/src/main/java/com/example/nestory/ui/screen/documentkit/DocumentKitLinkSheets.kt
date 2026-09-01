package com.example.nestory.ui.screen.documentkit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun KitLinkSourceSheet(
    onDismiss: () -> Unit,
    onScanClick: () -> Unit,
    onPickSavedClick: () -> Unit,
) {
    KitBottomSheet(onDismiss = onDismiss) {
        Text(
            text = "Chọn nguồn giấy tờ",
            style = NestoryTextStyles.Body16Bold,
            color = GeneratedColor.Figma000000,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp)
        )
        KitSheetOptionRow(
            iconRes = AppIcons.KitCameraOutline,
            text = "Quét tài liệu",
            containerColor = GeneratedColor.FigmaF3eeff,
            iconTint = GeneratedColor.Figma522ec8,
            onClick = onScanClick
        )
        KitSheetDivider()
        KitSheetOptionRow(
            iconRes = AppIcons.WeuiFolderOutlined,
            text = "Từ giấy tờ đã lưu trong Nestory",
            containerColor = GeneratedColor.FigmaF3eeff,
            iconTint = GeneratedColor.Figma522ec8,
            onClick = onPickSavedClick
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun KitLinkDocumentPickerSheet(
    documents: List<DocumentEntity>,
    onDismiss: () -> Unit,
    onSelect: (DocumentEntity) -> Unit,
) {
    KitBottomSheet(onDismiss = onDismiss) {
        Text(
            text = "Chọn giấy tờ",
            style = NestoryTextStyles.Body16Bold,
            color = GeneratedColor.Figma000000,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp)
        )
        if (documents.isEmpty()) {
            Text(
                text = "Chưa có giấy tờ nào được lưu trong Nestory",
                style = NestoryTextStyles.Body14Medium,
                color = GeneratedColor.Figma919191,
                modifier = Modifier.padding(20.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
            ) {
                items(documents, key = { it.id }) { document ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { onSelect(document) })
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(GeneratedColor.FigmaF3eeff, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = AppIcons.KitFile),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = GeneratedColor.Figma522ec8
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = document.title.ifBlank { "Giấy tờ không tên" },
                                style = NestoryTextStyles.Body14Medium,
                                color = GeneratedColor.Figma000000,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = document.expirationDate?.let { "Ngày hết hạn: $it" }
                                    ?: "Không có ngày hết hạn",
                                style = NestoryTextStyles.Body12Semi,
                                color = GeneratedColor.Figma919191
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun KitBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GeneratedColor.Figma000000.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        color = GeneratedColor.FigmaFfffff,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .navigationBarsPadding(),
                content = content
            )
        }
    }
}

@Composable
private fun KitSheetOptionRow(
    iconRes: Int,
    text: String,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    textColor: Color = GeneratedColor.Figma000000,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(containerColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = NestoryTextStyles.Body15Semi,
            color = textColor
        )
    }
}

@Composable
private fun KitSheetDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GeneratedColor.FigmaE5e7eb)
    )
}
