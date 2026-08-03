package com.example.nestory.ui.screens.ocr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nestory.data.entity.ContainerEntity
import com.example.nestory.data.model.DocumentCategory
import com.example.nestory.data.model.DocumentDraft
import com.example.nestory.ui.components.BackTextButton
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.screens.container.ContainerFormField
import com.example.nestory.ui.theme.GeneratedColor
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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackTextButton(onClick = onBack)
            }

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

            LabeledTextField(
                label = "Tên giấy tờ",
                value = draft.title,
                placeholder = "Nhập tên giấy tờ",
            ) { onDraftChange(draft.copy(title = it)) }

            LabeledTextField(
                label = "Số giấy tờ",
                value = draft.documentNumber.orEmpty(),
                placeholder = "Số giấy tờ",
            ) { onDraftChange(draft.copy(documentNumber = it)) }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Danh mục",
                    style = NestoryTextStyles.Body16Bold,
                    color = GeneratedColor.Figma000000,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { categoryMenuOpen = true },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        OutlinedTextField(
                            value = draft.category?.let { categoryLabel(it) } ?: "Chưa xác định",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            textStyle = NestoryTextStyles.Body15Medium,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = GeneratedColor.Figma1855ee,
                                unfocusedIndicatorColor = GeneratedColor.FigmaE5e7eb,
                            ),
                        )
                    }
                    DropdownMenu(
                        expanded = categoryMenuOpen,
                        onDismissRequest = { categoryMenuOpen = false },
                    ) {
                        DocumentCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = categoryLabel(category)) },
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

            LabeledTextField(
                label = "Ngày phát hành",
                value = draft.issueDate.orEmpty(),
                placeholder = "DD/MM/YYYY",
            ) { onDraftChange(draft.copy(issueDate = it)) }

            LabeledTextField(
                label = "Ngày hết hạn",
                value = draft.expiryDate.orEmpty(),
                placeholder = "DD/MM/YYYY",
            ) { onDraftChange(draft.copy(expiryDate = it)) }

            LabeledTextField(
                label = "Tên chủ sở hữu",
                value = draft.holderName.orEmpty(),
                placeholder = "Tên chủ sở hữu",
            ) { onDraftChange(draft.copy(holderName = it)) }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Container",
                    style = NestoryTextStyles.Body16Bold,
                    color = GeneratedColor.Figma000000,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { containerMenuOpen = true },
                ) {
                    ContainerFormField(
                        value = selectedContainer?.name.orEmpty(),
                        onValueChange = {},
                        placeholder = "Chọn container",
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

            PrimaryActionButton(
                text = "Lưu giấy tờ",
                onClick = onSave,
            )
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = NestoryTextStyles.Body16Bold,
            color = GeneratedColor.Figma000000,
        )
        ContainerFormField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
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

