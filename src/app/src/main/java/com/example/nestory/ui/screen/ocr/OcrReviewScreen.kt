package com.example.nestory.ui.screen.ocr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.nestory.R
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.model.DocumentDraft
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles

/**
 * Auto-fill review form. Displays the OCR-derived [DocumentDraft], lets the
 * user correct fields and pick a container, then saves.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrReviewScreen(
    draft: DocumentDraft,
    containers: List<ContainerEntity>,
    onDraftChange: (DocumentDraft) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var containerMenuOpen by remember { mutableStateOf(false) }
    val selectedContainer = containers.firstOrNull { it.id == draft.containerId }

    NestoryScreen(
        scrollable = true,
        verticalPadding = 20.dp,
        useStatusBarPadding = true,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gridicons_cross),
                    contentDescription = "Close",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onBack() }
                        .size(24.dp),
                    tint = GeneratedColor.Figma000000
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Xem lại thông tin giấy tờ",
                    style = NestoryTextStyles.Title22Semi,
                    color = GeneratedColor.Figma000000,
                )
                Text(
                    text = "Kiểm tra và chỉnh sửa thông tin được nhận dạng trước khi lưu.",
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma919191,
                )
            }

            // Section 1: Thông tin chính
            ReviewSection(
                title = "Thông tin chính",
                icon = AppIcons.DocumentMainInfo
            ) {
                ReviewField(
                    label = "Tên giấy tờ",
                    value = draft.title,
                    hint = "Nhập tên giấy tờ",
                    onValueChange = { onDraftChange(draft.copy(title = it)) }
                )

                // Danh mục field
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "Danh mục",
                        style = NestoryTextStyles.Body12Semi,
                        color = GeneratedColor.Figma000000
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryMenuOpen = true },
                    ) {
                        ReviewReadOnlyField(
                            value = draft.category?.toVietnameseLabel() ?: "Chưa xác định",
                            hint = "Chọn hoặc nhập danh mục"
                        )
                        DropdownMenu(
                            expanded = categoryMenuOpen,
                            onDismissRequest = { categoryMenuOpen = false },
                        ) {
                            DocumentCategory.entries.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(text = category.toVietnameseLabel()) },
                                    onClick = {
                                        onDraftChange(draft.copy(category = category))
                                        categoryMenuOpen = false
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(text = "Chưa xác định") },
                                onClick = {
                                    onDraftChange(draft.copy(category = null))
                                    categoryMenuOpen = false
                                },
                            )
                        }
                    }
                }
            }

            // Section 2: Thời hạn
            ReviewSection(
                title = "Thời hạn",
                icon = AppIcons.DocumentDeadline
            ) {
                ReviewField(
                    label = "Ngày phát hành",
                    value = draft.issueDate.orEmpty(),
                    hint = "DD/MM/YYYY",
                    onValueChange = { onDraftChange(draft.copy(issueDate = it)) }
                )
                ReviewField(
                    label = "Ngày hết hạn",
                    value = draft.expiryDate.orEmpty(),
                    hint = "DD/MM/YYYY",
                    onValueChange = { onDraftChange(draft.copy(expiryDate = it)) }
                )
            }

            // Section 3: Vị trí lưu trữ
            ReviewSection(
                title = "Vị trí lưu trữ",
                icon = AppIcons.DocumentStorage
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "Nơi lưu trữ hiện tại",
                        style = NestoryTextStyles.Body12Semi,
                        color = GeneratedColor.Figma000000
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { containerMenuOpen = true },
                    ) {
                        ReviewReadOnlyField(
                            value = selectedContainer?.name.orEmpty(),
                            hint = "Chọn container"
                        )
                        DropdownMenu(
                            expanded = containerMenuOpen,
                            onDismissRequest = { containerMenuOpen = false },
                        ) {
                            containers.forEach { container ->
                                DropdownMenuItem(
                                    text = { Text(text = container.name) },
                                    onClick = {
                                        onDraftChange(draft.copy(containerId = container.id))
                                        containerMenuOpen = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
            
            // Other fields (Holder Name, etc.) could be in another section or main info
            ReviewSection(
                title = "Thông tin bổ sung",
                icon = AppIcons.NestoryNote
            ) {
                ReviewField(
                    label = "Tên chủ sở hữu",
                    value = draft.holderName.orEmpty(),
                    hint = "Tên chủ sở hữu",
                    onValueChange = { onDraftChange(draft.copy(holderName = it)) }
                )
                ReviewField(
                    label = "Số giấy tờ",
                    value = draft.documentNumber.orEmpty(),
                    hint = "Số giấy tờ",
                    onValueChange = { onDraftChange(draft.copy(documentNumber = it)) }
                )
            }

            PrimaryActionButton(
                text = "Lưu giấy tờ",
                onClick = onSave,
            )
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    icon: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NestoryRadius.R15)
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R15)
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(NestoryRadius.R10)
                    .background(GeneratedColor.FigmaEdebff),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = NestoryTextStyles.Body16Bold,
                color = GeneratedColor.Figma000000
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        content()
    }
}

@Composable
private fun ReviewField(
    label: String,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = NestoryTextStyles.Body12Semi,
            color = GeneratedColor.Figma000000
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaF3f6ff)
                .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = NestoryTextStyles.Body14Medium,
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            style = NestoryTextStyles.Body14Medium,
                            color = GeneratedColor.Figma919191
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun ReviewReadOnlyField(
    value: String,
    hint: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(NestoryRadius.R10)
            .background(GeneratedColor.FigmaF3f6ff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = value.ifEmpty { hint },
            style = NestoryTextStyles.Body14Medium,
            color = if (value.isEmpty()) GeneratedColor.Figma919191 else GeneratedColor.Figma000000
        )
    }
}

internal fun categoryLabel(category: DocumentCategory): String = when (category) {
    DocumentCategory.IDENTITY -> "Nhân thân"
    DocumentCategory.EDUCATION -> "Học vấn"
    DocumentCategory.FINANCE -> "Tài chính"
    DocumentCategory.PROPERTY -> "Bất động sản"
    DocumentCategory.VEHICLE -> "Phương tiện"
    DocumentCategory.HEALTH -> "Sức khỏe"
    DocumentCategory.OTHER -> "Khác"
}

