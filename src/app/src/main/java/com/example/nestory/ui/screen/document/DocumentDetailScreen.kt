package com.example.nestory.ui.screen.document

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun DocumentDetailScreen(
    document: DocumentUiModel,
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onSave: (name: String, categoryLabel: String, expiryDate: String) -> Unit = { _, _, _ -> },
    onToggleFavorite: () -> Unit = {},
    isEditMode: Boolean = false,
    onEditModeChange: (Boolean) -> Unit = {},
    editLeaveRequested: Boolean = false,
    onEditLeaveComplete: () -> Unit = {},
    onEditLeaveDismiss: () -> Unit = {},
    readOnly: Boolean = false,
) {
    var editedName by remember { mutableStateOf(document.name) }
    var editedCategory by remember { mutableStateOf(document.category) }
    var editedExpiry by remember { mutableStateOf(document.expiryDate) }
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val resetEditState: () -> Unit = {
        editedName = document.name
        editedCategory = document.category
        editedExpiry = document.expiryDate
    }

    BackHandler(enabled = isEditMode) {
        showEditConfirmDialog = true
    }

    LaunchedEffect(editLeaveRequested) {
        if (editLeaveRequested && isEditMode) {
            showEditConfirmDialog = true
        }
    }

    NestoryScreen(
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
        scrollable = true
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(
                title = if (isEditMode) "Chỉnh sửa giấy tờ" else "Thông tin chính",
                isEditMode = isEditMode,
                isFavorite = document.isFavorite,
                onBack = {
                    if (isEditMode) showEditConfirmDialog = true else onBack()
                },
                onEditToggle = {
                    if (!readOnly) {
                        resetEditState()
                        onEditModeChange(true)
                    }
                },
                showEditButton = !readOnly,
                onToggleFavorite = onToggleFavorite,
                favoriteEnabled = !readOnly
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(7.dp))
                
                // Section 1: Thông tin chính
                DetailSection(
                    title = "Thông tin chính",
                    icon = AppIcons.DocumentMainInfo
                ) {
                    DetailField(
                        label = "Tên giấy tờ ",
                        value = document.name,
                        isEditMode = isEditMode,
                        hint = "Nhập tên giấy tờ ",
                        editableValue = if (isEditMode) editedName else null,
                        onValueChange = { editedName = it }
                    )
                    DetailField(
                        label = "Danh mục",
                        value = document.category,
                        isEditMode = isEditMode,
                        hint = "Chọn hoặc nhập danh mục",
                        editableValue = if (isEditMode) editedCategory else null,
                        onValueChange = { editedCategory = it }
                    )
                }

                // Section 2: Thời hạn
                DetailSection(
                    title = "Thời hạn",
                    icon = AppIcons.DocumentDeadline
                ) {
                    DetailField(
                        label = "Ngày hết hạn",
                        value = document.expiryDate,
                        isEditMode = isEditMode,
                        hint = "Chọn ngày hết hạn ",
                        editableValue = if (isEditMode) editedExpiry else null,
                        onValueChange = { editedExpiry = it }
                    )
                    if (!isEditMode) {
                        DetailStatusField(label = "Trạng thái", status = document.status)
                    }
                }

                // Section 3: Vị trí lưu trữ
                DetailSection(
                    title = "Vị trí lưu trữ",
                    icon = AppIcons.DocumentStorage
                ) {
                    DetailField(
                        label = "Nơi lưu trữ hiện tại",
                        value = "Ngăn 4",
                        isEditMode = isEditMode,
                        hint = "Chọn container"
                    )
                    if (!isEditMode) {
                        DetailField(label = "Đường dẫn nơi lưu trữ", value = document.containerPath)
                    }
                }

                if (isEditMode) {
                    EditActions(
                        onSave = {
                            onSave(editedName, editedCategory, editedExpiry)
                            onEditModeChange(false)
                        },
                        onDelete = { showDeleteConfirmDialog = true }
                    )
                } else {
                    // Section 4: Tệp scan (Only in View Mode)
                    DetailSection(
                        title = "Tệp scan",
                        icon = AppIcons.DocumentFileScan
                    ) {
                        if (document.attachmentUris.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(NestoryRadius.R10)
                                    .background(GeneratedColor.FigmaF3f6ff)
                                    .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có file scan",
                                    style = NestoryTextStyles.Body12Semi,
                                    color = GeneratedColor.Figma919191
                                )
                            }
                        } else {
                            document.attachmentUris.forEach { fileUri ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(45.dp)
                                        .clip(NestoryRadius.R10)
                                        .background(GeneratedColor.FigmaF3f6ff)
                                        .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(AppIcons.DocumentFileScan),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = attachmentFileName(fileUri),
                                        style = NestoryTextStyles.Body14Medium,
                                        color = GeneratedColor.Figma000000,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showEditConfirmDialog) {
        ConfirmEditDocumentDialog(
            onConfirm = {
                showEditConfirmDialog = false
                onSave(editedName, editedCategory, editedExpiry)
                onEditModeChange(false)
                if (editLeaveRequested) onEditLeaveComplete()
            },
            onDismiss = {
                showEditConfirmDialog = false
                if (editLeaveRequested) onEditLeaveDismiss()
            }
        )
    }

    if (showDeleteConfirmDialog) {
        ConfirmDeleteDocumentDialog(
            onConfirm = {
                showDeleteConfirmDialog = false
                onDelete()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}

@Composable
private fun DetailHeader(
    title: String,
    isEditMode: Boolean,
    onBack: () -> Unit,
    onEditToggle: () -> Unit,
    showEditButton: Boolean = true,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    favoriteEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(AppIcons.IcBackwardArrow),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        }
        
        if (isEditMode) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = NestoryTextStyles.Heading25Bold,
                color = GeneratedColor.Figma000000
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
            if (showEditButton) {
                // Edit Button (Node 272:165)
                Image(
                    painter = painterResource(AppIcons.DocumentEdit),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onEditToggle() },
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            // Star Button
            Image(
                painter = painterResource(
                    if (isFavorite) AppIcons.DocumentStarred else AppIcons.KitUnstarred
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = favoriteEnabled) { onToggleFavorite() },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun DetailSection(
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
private fun DetailField(
    label: String,
    value: String,
    isEditMode: Boolean = false,
    hint: String = "",
    editableValue: String? = null,
    onValueChange: (String) -> Unit = {},
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = NestoryTextStyles.Body12Semi,
            color = if (isEditMode) GeneratedColor.Figma000000 else GeneratedColor.Figma919191
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
            if (isEditMode && editableValue != null) {
                BasicTextField(
                    value = editableValue,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = NestoryTextStyles.Body14Medium.copy(
                        color = GeneratedColor.Figma000000
                    ),
                    decorationBox = { innerTextField ->
                        if (editableValue.isEmpty()) {
                            Text(
                                text = hint,
                                style = NestoryTextStyles.Body14Medium,
                                color = GeneratedColor.Figma919191
                            )
                        }
                        innerTextField()
                    }
                )
            } else {
                Text(
                    text = if (isEditMode) hint else value,
                    style = NestoryTextStyles.Body14Medium,
                    color = if (isEditMode) GeneratedColor.Figma919191 else GeneratedColor.Figma000000
                )
            }
        }
    }
}

@Composable
private fun DetailStatusField(
    label: String,
    status: DocumentStatus
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = NestoryTextStyles.Body12Semi,
            color = GeneratedColor.Figma919191
        )
        Spacer(modifier = Modifier.height(6.dp))
        val statusColor = when (status) {
            DocumentStatus.Active -> Color(0xFF137C23)
            DocumentStatus.ExpiringSoon -> Color(0xFFEB6E00)
            DocumentStatus.Expired -> Color(0xFFFF0000)
        }
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
                text = when (status) {
                    DocumentStatus.Active -> "Còn hiệu lực"
                    DocumentStatus.ExpiringSoon -> "Sắp hết hạn"
                    DocumentStatus.Expired -> "Đã hết hạn"
                },
                style = NestoryTextStyles.Body14Semi,
                color = statusColor
            )
        }
    }
}

@Composable
private fun EditActions(
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .border(1.dp, GeneratedColor.FigmaFf0000, RoundedCornerShape(NestorySpacing.S10))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Xóa",
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(GeneratedColor.Figma522ec8, RoundedCornerShape(NestorySpacing.S10))
                .clickable { onSave() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lưu",
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.FigmaFfffff
            )
        }
    }
}

internal fun attachmentFileName(filePath: String): String =
    filePath.substringAfterLast('/').ifBlank { filePath }

@Preview(showBackground = true)
@Composable
fun DocumentDetailViewPreview() {
    DocumentDetailScreen(
        document = DocumentUiModel(
            id = "1",
            name = "Hợp đồng thuê nhà 2026",
            category = "Hợp đồng, Pháp lý",
            containerPath = "Tủ tài liệu > Ngăn 4",
            containerId = 1L,
            status = DocumentStatus.Active,
            expiryDate = "20/08/2026",
            categoryColor = Color(0xFF1855EE),
            isFavorite = false
        ),
        onBack = {}
    )
}