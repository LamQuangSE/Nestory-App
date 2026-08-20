package com.example.nestory.ui.screen.documentkit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.screen.document.DocumentStatusCalculator
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import com.example.nestory.ui.components.LocalInputMonitor
import androidx.compose.ui.focus.onFocusChanged
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

val KitCategoryOptions = listOf(
    "Hồ sơ thuê nhà",
    "Hồ sơ nhập học / du học",
    "Hồ sơ du lịch",
    "Hồ sơ du học",
    "Khác",
)

@Composable
fun DocumentKitCreateScreen(
    onBackClick: () -> Unit,
    onSubmit: (name: String, date: String, category: String?, description: String?, note: String?) -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialDate: String = "",
    initialCategory: String? = null,
    initialDescription: String = "",
    initialNote: String = "",
    submitLabel: String = "Tạo bộ hồ sơ mới",
    isEdit: Boolean = false,
    isSaving: Boolean = false,
    existingNames: List<String> = emptyList(),
    editLeaveRequested: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onEditBack: (name: String, date: String, category: String?, description: String?, note: String?) -> Unit = { _, _, _, _, _ -> },
) {
    var name by remember { mutableStateOf(initialName) }
    var date by remember { mutableStateOf(initialDate) }
    var category by remember { mutableStateOf(initialCategory ?: "Khác") }
    var description by remember { mutableStateOf(initialDescription) }
    var note by remember { mutableStateOf(initialNote) }
    var showNameError by remember { mutableStateOf(false) }
    var showDateError by remember { mutableStateOf(false) }
    var showDuplicateError by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val hasContent = name.isNotBlank() ||
        date.isNotBlank() ||
        category != "Khác" ||
        description.isNotBlank() ||
        note.isNotBlank()

    val validateAndSubmit: () -> Unit = {
        val isNameValid = name.isNotBlank()
        val isDateValid = date.isNotBlank() && DocumentStatusCalculator.parseExpirationDate(date) != null
        val isDuplicate = name.isNotBlank() &&
            existingNames.any { it.equals(name.trim(), ignoreCase = true) }
        showNameError = !isNameValid
        showDateError = !isDateValid
        showDuplicateError = isDuplicate
        if (isNameValid && isDateValid && !isDuplicate) {
            onSubmit(name, date, category, description, note)
        }
    }

    BackHandler(enabled = isEdit || hasContent) {
        if (isEdit) {
            onEditBack(name, date, category, description, note)
        } else {
            showDiscardDialog = true
        }
    }

    LaunchedEffect(editLeaveRequested) {
        if (editLeaveRequested) {
            when {
                isEdit -> onEditBack(name, date, category, description, note)
                hasContent -> showDiscardDialog = true
                else -> onBackClick()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeneratedColor.FigmaFfffff)
            .statusBarsPadding()
            .padding(horizontal = NestorySpacing.S12)
            .padding(vertical = 7.dp)
    ) {
        KitTopBar(
            title = if (isEdit) "Chỉnh sửa bộ hồ sơ" else "Tạo bộ hồ sơ mới",
            onBack = {
                if (isEdit) {
                    onEditBack(name, date, category, description, note)
                } else if (!hasContent) {
                    onBackClick()
                } else {
                    showDiscardDialog = true
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            KitNameCard(
                name = name,
                onNameChange = {
                    name = it
                    showNameError = false
                    showDuplicateError = false
                },
                showError = showNameError,
                showDuplicateError = showDuplicateError
            )

            KitDateCard(
                date = date,
                onDateChange = {
                    date = it
                    showDateError = false
                },
                showError = showDateError
            )

            KitCategoryCard(
                selected = category,
                onSelect = { category = it }
            )

            KitDescriptionNoteCard(
                description = description,
                onDescriptionChange = { description = it },
                note = note,
                onNoteChange = { note = it }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NestorySpacing.S10)
        ) {
            if (isEdit && onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .border(1.dp, GeneratedColor.FigmaFf0000, RoundedCornerShape(NestorySpacing.S10))
                            .clickable(enabled = !isSaving) { showDeleteConfirm = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Xóa Kit",
                            style = NestoryTextStyles.Body15Semi,
                            color = GeneratedColor.FigmaFf0000
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(GeneratedColor.Figma522ec8, RoundedCornerShape(NestorySpacing.S10))
                            .clickable(enabled = !isSaving, onClick = validateAndSubmit),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = GeneratedColor.FigmaFfffff
                            )
                        } else {
                            Text(
                                text = "Cập nhật Kit",
                                style = NestoryTextStyles.Body15Semi,
                                color = GeneratedColor.FigmaFfffff
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(GeneratedColor.Figma522ec8, RoundedCornerShape(NestorySpacing.S10))
                        .clickable(enabled = !isSaving, onClick = validateAndSubmit),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = GeneratedColor.FigmaFfffff
                        )
                    } else {
                        Text(
                            text = submitLabel,
                            style = NestoryTextStyles.Body15Semi,
                            color = GeneratedColor.FigmaFfffff
                        )
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        ConfirmDiscardKitDialog(
            onConfirm = {
                showDiscardDialog = false
                onBackClick()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDeleteKitDialog(
            onConfirm = {
                showDeleteConfirm = false
                onDelete?.invoke()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun KitNameCard(
    name: String,
    onNameChange: (String) -> Unit,
    showError: Boolean,
    showDuplicateError: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(14.dp))
            .padding(horizontal = NestorySpacing.S10, vertical = NestorySpacing.S10),
        verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        KitRequiredLabel(text = "Tên bộ hồ sơ")
        KitTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Nhập tên bộ hồ sơ",
            isError = showError || showDuplicateError,
            testTag = "kit_name_input"
        )
        if (showDuplicateError) {
            Text(
                text = "Tên bộ hồ sơ đã tồn tại",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        } else if (showError) {
            Text(
                text = "Tên bộ hồ sơ không được để trống",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
    }
}

@Composable
private fun KitDateCard(
    date: String,
    onDateChange: (String) -> Unit,
    showError: Boolean,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S10, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        KitSectionHeader(
            title = "Ngày sử dụng",
            iconRes = AppIcons.KitCalendar
        )
        Spacer(modifier = Modifier.height(5.dp))
        KitRequiredLabel(text = "Ngày sử dụng", isRequired = true)
        KitTextField(
            value = date,
            onValueChange = {},
            placeholder = "Chọn ngày bộ hồ sơ cần phải hoàn thành",
            readOnly = true,
            onTap = { showDatePicker = true },
            isError = showError,
            trailingIcon = {
                Icon(
                    painter = painterResource(id = AppIcons.KitCalendar),
                    contentDescription = "Chọn ngày",
                    modifier = Modifier.size(15.dp),
                    tint = GeneratedColor.Figma000000
                )
            }
        )
        if (showError) {
            Text(
                text = "Vui lòng chọn ngày sử dụng",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
    }

    if (showDatePicker) {
        KitDatePickerDialog(
            initialDate = date,
            onConfirm = { selected ->
                onDateChange(selected)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun KitDatePickerDialog(
    initialDate: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parseDateToMillis(initialDate)
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) onConfirm(millisToDate(millis)) else onDismiss()
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

private val kitDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun parseDateToMillis(date: String): Long? =
    DocumentStatusCalculator.parseExpirationDate(date)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

private fun millisToDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(kitDateFormatter)

@Composable
private fun KitCategoryCard(
    selected: String?,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S10, vertical = NestorySpacing.S6),
        verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        KitSectionHeader(
            title = "Loại hồ sơ",
            iconRes = AppIcons.KitAlignLeft
        )
        KitCategoryOptions.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(option) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = GeneratedColor.Figma522ec8
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = option,
                    style = NestoryTextStyles.Body15Medium,
                    color = GeneratedColor.Figma000000,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun KitDescriptionNoteCard(
    description: String,
    onDescriptionChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S10, vertical = NestorySpacing.S6),
        verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        KitSectionHeader(
            title = "Mục đích",
            iconRes = AppIcons.KitTarget
        )
        KitTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = "Nhập nội dung"
        )
        KitSectionHeader(
            title = "Ghi chú",
            iconRes = AppIcons.NestoryNote
        )
        KitTextField(
            value = note,
            onValueChange = onNoteChange,
            placeholder = "Nhập nội dung",
            height = 65.dp
        )
    }
}

@Composable
internal fun KitRequiredLabel(
    text: String,
    isRequired: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = NestoryTextStyles.Body12Semi,
            color = GeneratedColor.Figma000000
        )
        if (isRequired) {
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "*",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
    }
}

@Composable
internal fun KitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    height: androidx.compose.ui.unit.Dp = 31.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    onTap: (() -> Unit)? = null,
    isError: Boolean = false,
    testTag: String? = null,
    useMonitor: Boolean = true
) {
    val monitor = LocalInputMonitor.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .border(1.dp, if (isError) GeneratedColor.FigmaFf0000 else GeneratedColor.FigmaE5e7eb, RoundedCornerShape(5.dp))
            .padding(horizontal = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.TextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    if (useMonitor) monitor.update(it)
                },
                readOnly = readOnly,
                placeholder = {
                    Text(
                        text = placeholder,
                        style = NestoryTextStyles.Body14Medium,
                        color = GeneratedColor.Figma919191,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                    .onFocusChanged { 
                        if (it.isFocused && useMonitor && !readOnly) {
                            monitor.show(value, placeholder)
                        } else if (!it.isFocused) {
                            monitor.hide()
                        }
                    },
                textStyle = NestoryTextStyles.Body14Medium.copy(color = GeneratedColor.Figma000000),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                )
            )
            trailingIcon?.invoke()
        }
        if (onTap != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onTap)
            )
        }
    }
}
