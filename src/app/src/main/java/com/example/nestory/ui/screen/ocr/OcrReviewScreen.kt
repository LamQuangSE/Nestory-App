package com.example.nestory.ui.screen.ocr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
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
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showExpiryPicker by remember { mutableStateOf(false) }
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
                        .clickable { showDiscardDialog = true }
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
                    errorText = if (fieldErrors.title) "Vui lòng nhập tên giấy tờ" else null,
                )

                // Danh mục field
                val categoryError = fieldErrors.category
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
                            hint = "Chọn hoặc nhập danh mục",
                            error = categoryError,
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
                    if (categoryError) {
                        FieldErrorText(text = "Vui lòng chọn danh mục")
                    }
                }
            }

            ReviewSection(
                title = "Thời hạn",
                icon = AppIcons.DocumentDeadline
            ) {
                ReviewDateField(
                    label = "Ngày hết hạn",
                    value = draft.expiryDate,
                    hint = "DD/MM/YYYY",
                    onClick = { showExpiryPicker = true },
                    error = fieldErrors.expiryDate,
                )
            }

            ReviewSection(
                title = "Vị trí lưu trữ",
                icon = AppIcons.DocumentStorage
            ) {
                val containerError = fieldErrors.container
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
                            error = containerError,
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
                    if (containerError) {
                        FieldErrorText(text = "Vui lòng chọn nơi lưu trữ")
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

    if (showDiscardDialog) {
        OcrDiscardDialog(
            onConfirm = {
                showDiscardDialog = false
                onBack()
            },
            onDismiss = { showDiscardDialog = false },
        )
    }

    if (showExpiryPicker) {
        OcrDatePickerDialog(
            initialDate = draft.expiryDate,
            onConfirm = { selected ->
                onDraftChange(draft.copy(expiryDate = selected))
                showExpiryPicker = false
            },
            onDismiss = { showExpiryPicker = false },
        )
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
    onValueChange: (String) -> Unit,
    errorText: String? = null,
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
                .border(
                    1.dp,
                    if (errorText != null) GeneratedColor.FigmaFf0000 else GeneratedColor.FigmaE5e7eb,
                    NestoryRadius.R10,
                )
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
        if (errorText != null) {
            FieldErrorText(text = errorText)
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
            .border(
                1.dp,
                if (error) GeneratedColor.FigmaFf0000 else GeneratedColor.FigmaE5e7eb,
                NestoryRadius.R10,
            )
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

@Composable
private fun ReviewDateField(
    label: String,
    value: String?,
    hint: String,
    onClick: () -> Unit,
    error: Boolean = false,
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
                .border(
                    1.dp,
                    if (error) GeneratedColor.FigmaFf0000 else GeneratedColor.FigmaE5e7eb,
                    NestoryRadius.R10,
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.orEmpty().ifEmpty { hint },
                style = NestoryTextStyles.Body14Medium,
                color = if (value.isNullOrEmpty()) GeneratedColor.Figma919191 else GeneratedColor.Figma000000
            )
            Image(
                painter = painterResource(id = AppIcons.KitCalendar),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(18.dp),
                contentScale = ContentScale.Fit,
            )
        }
        if (error) {
            FieldErrorText(text = "Vui lòng chọn ngày hết hạn")
        }
    }
}

@Composable
private fun FieldErrorText(text: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = text,
        style = NestoryTextStyles.Body12Semi,
        color = GeneratedColor.FigmaFf0000,
    )
}

@Composable
private fun OcrDiscardDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GeneratedColor.FigmaFfffff, RoundedCornerShape(10.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, top = 22.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Huỷ lưu giấy tờ",
                    style = NestoryTextStyles.Body20Bold.copy(fontWeight = FontWeight.SemiBold),
                    color = GeneratedColor.Figma000000
                )
                Text(
                    text = "Bạn có muốn thoát khỏi quá trình lưu giấy tờ không?",
                    style = NestoryTextStyles.Body15Medium.copy(fontSize = 17.sp, fontWeight = FontWeight.W600),
                    color = GeneratedColor.Figma000000
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GeneratedColor.FigmaE5e7eb)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 17.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DialogActionButton(
                    text = "Tiếp tục",
                    containerColor = GeneratedColor.FigmaFfffff,
                    borderColor = GeneratedColor.FigmaE5e7eb,
                    textColor = GeneratedColor.Figma919191,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                DialogActionButton(
                    text = "Thoát",
                    containerColor = GeneratedColor.Figma1855ee,
                    borderColor = null,
                    textColor = GeneratedColor.FigmaFfffff,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    text: String,
    containerColor: Color,
    borderColor: Color?,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(45.dp)
            .background(containerColor, RoundedCornerShape(10.dp))
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = NestoryTextStyles.Body15Semi,
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrDatePickerDialog(
    initialDate: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.let {
            DocumentStatusCalculator.parseExpirationDate(it)
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli()
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) onConfirm(millisToReviewDate(millis)) else onDismiss()
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

private val reviewDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun millisToReviewDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(reviewDateFormatter)

internal fun categoryLabel(category: DocumentCategory): String = when (category) {
    DocumentCategory.IDENTITY -> "Nhân thân"
    DocumentCategory.EDUCATION -> "Học vấn"
    DocumentCategory.FINANCE -> "Tài chính"
    DocumentCategory.PROPERTY -> "Bất động sản"
    DocumentCategory.VEHICLE -> "Phương tiện"
    DocumentCategory.HEALTH -> "Sức khỏe"
    DocumentCategory.OTHER -> "Khác"
}