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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestory.R
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.model.DocumentDraft
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.screen.document.DocumentStatusCalculator
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrReviewScreen(
    draft: DocumentDraft,
    containers: List<ContainerEntity>,
    onDraftChange: (DocumentDraft) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    fieldErrors: OcrFieldErrors = OcrFieldErrors(),
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

            ReviewSection(
                title = "Thông tin chính",
                icon = AppIcons.DocumentMainInfo
            ) {
                ReviewField(
                    label = "Tên giấy tờ",
                    value = draft.title,
                    hint = "Nhập tên giấy tờ",
                    onValueChange = { onDraftChange(draft.copy(title = it)) },
                    error = if (fieldErrors.title) "Tên giấy tờ không được để trống" else null,
                )

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
                            value = draft.category?.let { categoryLabel(it) } ?: "Chưa xác định",
                            hint = "Chọn hoặc nhập danh mục",
                            error = fieldErrors.category,
                        )
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
                    if (fieldErrors.category) {
                        Text(
                            text = "Vui lòng chọn danh mục",
                            style = NestoryTextStyles.Body12Semi,
                            color = GeneratedColor.FigmaFf0000,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            ReviewSection(
                title = "Thời hạn",
                icon = AppIcons.DocumentDeadline
            ) {
                ReviewDateField(
                    label = "Ngày phát hành",
                    value = draft.issueDate.orEmpty(),
                    hint = "DD/MM/YYYY",
                    onValueChange = { onDraftChange(draft.copy(issueDate = it)) },
                    error = if (fieldErrors.issueDate) "Ngày phát hành không đúng định dạng DD/MM/YYYY" else null,
                )
                ReviewDateField(
                    label = "Ngày hết hạn",
                    value = draft.expiryDate.orEmpty(),
                    hint = "DD/MM/YYYY",
                    onValueChange = { onDraftChange(draft.copy(expiryDate = it)) },
                    error = if (fieldErrors.expiryDate) "Ngày hết hạn không đúng định dạng DD/MM/YYYY" else null,
                )
            }

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
                            hint = "Chọn container",
                            error = fieldErrors.container,
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
                    if (fieldErrors.container) {
                        Text(
                            text = "Vui lòng chọn container để lưu giấy tờ",
                            style = NestoryTextStyles.Body12Semi,
                            color = GeneratedColor.FigmaFf0000,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
            
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
private fun ReviewDateField(
    label: String,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
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
                .border(1.dp, if (error != null) GeneratedColor.FigmaFf0000 else GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                .clickable { showDatePicker = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifEmpty { hint },
                    style = NestoryTextStyles.Body14Medium,
                    color = if (value.isEmpty()) GeneratedColor.Figma919191 else GeneratedColor.Figma000000,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    painter = painterResource(id = AppIcons.KitCalendar),
                    contentDescription = "Chọn ngày",
                    modifier = Modifier.size(15.dp),
                    tint = GeneratedColor.Figma000000
                )
            }
        }
        error?.let { message ->
            Text(
                text = message,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }

    if (showDatePicker) {
        OcrDatePickerDialog(
            initialDate = value,
            onConfirm = { selected ->
                onValueChange(selected)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrDatePickerDialog(
    initialDate: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parseOcrDateToMillis(initialDate)
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) onConfirm(millisToOcrDate(millis)) else onDismiss()
                }
            ) {
                Text(text = "Chọn", color = GeneratedColor.Figma522ec8)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Huỷ", color = GeneratedColor.Figma522ec8)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private val ocrDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun parseOcrDateToMillis(date: String): Long? =
    DocumentStatusCalculator.parseExpirationDate(date)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

private fun millisToOcrDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(ocrDateFormatter)

@Composable
private fun ReviewField(
    label: String,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
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
                .border(1.dp, if (error != null) GeneratedColor.FigmaFf0000 else GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
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
        error?.let { message ->
            Text(
                text = message,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun ReviewReadOnlyField(
    value: String,
    hint: String,
    error: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(NestoryRadius.R10)
            .background(GeneratedColor.FigmaF3f6ff)
            .border(1.dp, if (error) GeneratedColor.FigmaFf0000 else GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
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