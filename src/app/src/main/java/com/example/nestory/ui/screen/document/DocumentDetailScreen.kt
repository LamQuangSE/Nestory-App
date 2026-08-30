package com.example.nestory.ui.screen.document

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nestory.data.filesystem.FileOpener
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.screen.category.CategoryRoute
import com.example.nestory.ui.screen.container.ContainerRoute
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import com.example.nestory.ui.components.LocalInputMonitor
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private enum class DocumentDetailSubScreen { Main, CategorySelection, ContainerSelection }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    document: DocumentUiModel,
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onSave: (name: String, categoryLabel: String, expiryDate: String, containerId: Long?, pdfFileName: String?) -> Unit = { _, _, _, _, _ -> },
    onOpenPdf: (String) -> Unit = {},
    onEditScan: (String) -> Unit = {},
    isEditMode: Boolean = false,
    onEditModeChange: (Boolean) -> Unit = {},
    isSaving: Boolean = false,
    existingTitles: List<String> = emptyList(),
    editLeaveRequested: Boolean = false,
    onEditLeaveComplete: () -> Unit = {},
    onEditLeaveDismiss: () -> Unit = {},
    readOnly: Boolean = false,
    onReminderClick: () -> Unit = {},
    resolveContainerPath: (containerId: Long) -> String = { "" },
) {
    val context = LocalContext.current
    var subScreen by remember { mutableStateOf(DocumentDetailSubScreen.Main) }
    var editedName by remember { mutableStateOf(document.name) }
    var editedCategory by remember { mutableStateOf(document.category) }
    var editedExpiry by remember { mutableStateOf(document.expiryDate) }
    var editedContainerId by remember { mutableStateOf<Long?>(document.containerId) }
    var editedContainerPath by remember { mutableStateOf(document.containerPath) }
    val pdfFileUri = document.attachmentUris.firstOrNull { it.endsWith(".pdf", ignoreCase = true) }
    val hasPdf = pdfFileUri != null
    var editedFileName by remember(document.id) { mutableStateOf(pdfFileUri?.let { pdfBaseName(it) }.orEmpty()) }

    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDuplicateTitleError by remember { mutableStateOf(false) }
    var showNameRequiredError by remember { mutableStateOf(false) }
    var showFileNameRequiredError by remember { mutableStateOf(false) }

    val duplicateTitle = editedName.isNotBlank() &&
        existingTitles.any { it.equals(editedName.trim(), ignoreCase = true) }

    val canSave = editedName.isNotBlank() && !duplicateTitle &&
        (!hasPdf || editedFileName.isNotBlank())

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = editedExpiry.takeUnless { it == "Chưa có hạn" }?.let { dateStr ->
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.parse(dateStr)?.time
            } catch (e: Exception) {
                null
            }
        }
    )

    val resetEditState: () -> Unit = {
        editedName = document.name
        editedCategory = document.category
        editedExpiry = document.expiryDate
        editedContainerId = document.containerId
        editedContainerPath = document.containerPath
        editedFileName = pdfFileUri?.let { pdfBaseName(it) }.orEmpty()
        showNameRequiredError = false
        showDuplicateTitleError = false
        showFileNameRequiredError = false
    }

    BackHandler {
        when {
            showEditConfirmDialog -> {
                showEditConfirmDialog = false
                if (editLeaveRequested) onEditLeaveDismiss()
            }
            showDeleteConfirmDialog -> showDeleteConfirmDialog = false
            showDatePicker -> showDatePicker = false
            subScreen != DocumentDetailSubScreen.Main -> subScreen = DocumentDetailSubScreen.Main
            isEditMode -> showEditConfirmDialog = true
            else -> onBack()
        }
    }

    LaunchedEffect(editLeaveRequested) {
        if (editLeaveRequested && isEditMode) {
            showEditConfirmDialog = true
        }
    }

    // The edit-leave confirmation must stay reachable even while the Category or
    // Container selection UI is open on top of the edit form. Without this, tapping
    // another bottom-navigation destination while a selection is pending would be
    // silently ignored because the early returns below skip the dialog.
    if (showEditConfirmDialog) {
        ConfirmEditDocumentDialog(
            onConfirm = {
                showEditConfirmDialog = false
                if (canSave) {
                    onSave(editedName, editedCategory, editedExpiry, editedContainerId, editedFileName)
                    onEditModeChange(false)
                    if (editLeaveRequested) onEditLeaveComplete()
                } else {
                    showNameRequiredError = editedName.isBlank()
                    showDuplicateTitleError = duplicateTitle
                    showFileNameRequiredError = hasPdf && editedFileName.isBlank()
                    if (editLeaveRequested) onEditLeaveDismiss()
                }
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

    if (subScreen == DocumentDetailSubScreen.CategorySelection) {
        CategoryRoute(
            onBack = { subScreen = DocumentDetailSubScreen.Main },
            onConfirmSelection = { category ->
                editedCategory = category.name
                subScreen = DocumentDetailSubScreen.Main
            },
            selectionOnly = true,
            allowCreate = false,
            showPresetCategories = true,
            initialSelectedName = editedCategory
        )
        return
    }

    if (subScreen == DocumentDetailSubScreen.ContainerSelection) {
        ContainerRoute(
            onBack = { subScreen = DocumentDetailSubScreen.Main },
            onConfirmSelection = { container ->
                editedContainerId = container.id
                editedContainerPath = resolveContainerPath(container.id).ifBlank { container.name }
                subScreen = DocumentDetailSubScreen.Main
            },
            selectionOnly = true,
            allowCreate = false
        )
        return
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        editedExpiry = formatter.format(date)
                    }
                    showDatePicker = false
                }) {
                    Text("Xác nhận", color = GeneratedColor.Figma522ec8)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = GeneratedColor.Figma919191)
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
                onReminderClick = onReminderClick,
                showReminderButton = !readOnly
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
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
                        onValueChange = {
                            editedName = it
                            showDuplicateTitleError = false
                            showNameRequiredError = false
                        },
                        required = true,
                        errorText = if (isEditMode && showNameRequiredError) "Vui lòng nhập tên giấy tờ" else null,
                    )
                    if (isEditMode && showDuplicateTitleError) {
                        Text(
                            text = "Tên giấy tờ đã tồn tại",
                            style = NestoryTextStyles.Body12Semi,
                            color = GeneratedColor.FigmaFf0000,
                            modifier = Modifier.padding(top = 0.dp, bottom = 12.dp)
                        )
                    }
                    DetailField(
                        label = "Danh mục",
                        value = if (isEditMode) editedCategory else document.category,
                        isEditMode = isEditMode,
                        hint = "Chọn danh mục",
                        onClick = if (isEditMode) { { subScreen = DocumentDetailSubScreen.CategorySelection } } else null,
                        required = true,
                    )
                }

                // Section 2: Thời hạn
                DetailSection(
                    title = "Thời hạn",
                    icon = AppIcons.DocumentDeadline
                ) {
                    DetailField(
                        label = "Ngày hết hạn",
                        value = if (isEditMode) editedExpiry else document.expiryDate,
                        isEditMode = isEditMode,
                        hint = "Chọn ngày hết hạn ",
                        onClick = if (isEditMode) { { showDatePicker = true } } else null,
                        trailingIcon = if (isEditMode) AppIcons.KitCalendar else null,
                        required = true,
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
                        value = if (isEditMode) editedContainerPath else currentContainerName(document.containerPath),
                        isEditMode = isEditMode,
                        hint = "Chọn container",
                        onClick = if (isEditMode) { { subScreen = DocumentDetailSubScreen.ContainerSelection } } else null,
                        required = true,
                    )
                    if (!isEditMode) {
                        DetailField(label = "Đường dẫn nơi lưu trữ", value = document.containerPath)
                    }
                }

                // Section 4: Tệp scan (shown in both view and edit mode)
                DetailSection(
                    title = "Tệp scan",
                    icon = AppIcons.DocumentFileScan
                ) {
                        // The PDF is the actual scanned file; fall back to all
                        // attachments for legacy image-only documents.
                        val scannedFiles = document.attachmentUris
                            .filter { it.endsWith(".pdf", ignoreCase = true) }
                            .ifEmpty { document.attachmentUris }
                        if (scannedFiles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(NestoryRadius.R10)
                                    .background(GeneratedColor.FigmaFfffff)
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
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                scannedFiles.forEach { fileUri ->
                                    val fileName = attachmentFileName(fileUri)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 500.dp)
                                    .clip(NestoryRadius.R10)
                                    .background(GeneratedColor.FigmaFfffff)
                                    .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                                            .clickable {
                                            if (fileUri.endsWith(".pdf", ignoreCase = true)) {
                                                if (isEditMode) onEditScan(fileUri) else onOpenPdf(fileUri)
                                            } else {
                                                FileOpener.open(context, fileUri)
                                            }
                                        },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (fileUri.endsWith(".pdf", ignoreCase = true)) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Image(
                                                    painter = painterResource(AppIcons.DocumentFileScan),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(48.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = fileName,
                                                    style = NestoryTextStyles.Body12Semi,
                                                    color = GeneratedColor.Figma000000
                                                )
                                            }
                                        } else {
                                            AsyncImage(
                                                model = fileUri,
                                                contentDescription = "Scanned document",
                                                modifier = Modifier.fillMaxWidth(),
                                                contentScale = ContentScale.FillWidth,
                                                error = painterResource(AppIcons.FigmaDocument)
                                            )
                                            Text(
                                                text = fileName,
                                                style = NestoryTextStyles.Body12Semi,
                                                color = GeneratedColor.Figma000000,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .background(GeneratedColor.FigmaFfffff.copy(alpha = 0.92f))
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }

                if (isEditMode) {
                    EditActions(
                        isSaving = isSaving,
                        onSave = {
                            if (!isSaving) {
                                if (canSave) {
                                    onSave(editedName, editedCategory, editedExpiry, editedContainerId, editedFileName)
                                    onEditModeChange(false)
                                } else {
                                    showNameRequiredError = editedName.isBlank()
                                    showDuplicateTitleError = duplicateTitle
                                    showFileNameRequiredError = hasPdf && editedFileName.isBlank()
                                }
                            }
                        },
                        onDelete = { if (!isSaving) showDeleteConfirmDialog = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DetailHeader(
    title: String,
    isEditMode: Boolean,
    onBack: () -> Unit,
    onEditToggle: () -> Unit,
    showEditButton: Boolean = true,
    onReminderClick: () -> Unit = {},
    showReminderButton: Boolean = true,
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
            if (showReminderButton) {
                // Reminder (bell) button
                Image(
                    painter = painterResource(AppIcons.NestoryNotification),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onReminderClick() },
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
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
    onClick: (() -> Unit)? = null,
    trailingIcon: Int? = null,
    required: Boolean = false,
    errorText: String? = null,
    useMonitor: Boolean = true
) {
    val monitor = LocalInputMonitor.current
    val focusRequester = remember { FocusRequester() }

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (required && isEditMode) {
                    withStyle(SpanStyle(color = Color.Red)) { append(" *") }
                }
            },
            style = NestoryTextStyles.Body12Semi,
            color = if (isEditMode) GeneratedColor.Figma000000 else GeneratedColor.Figma919191
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaFfffff)
                .border(
                    1.dp,
                    if (errorText != null) Color.Red else GeneratedColor.FigmaE5e7eb,
                    NestoryRadius.R10
                )
                .then(
                    when {
                        onClick != null -> Modifier.clickable { onClick() }
                        isEditMode && editableValue != null -> Modifier.clickable {
                            focusRequester.requestFocus()
                        }
                        else -> Modifier
                    }
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isEditMode && editableValue != null && onClick == null) {
                BasicTextField(
                    value = editableValue,
                    onValueChange = {
                        onValueChange(it)
                        if (useMonitor) monitor.update(it)
                    },
                    singleLine = true,
                    textStyle = NestoryTextStyles.Body14Medium.copy(
                        color = GeneratedColor.Figma000000
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { 
                            if (it.isFocused && useMonitor) {
                                monitor.show(editableValue, label)
                            } else if (!it.isFocused) {
                                monitor.hide()
                            }
                        },
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
                    text = if (isEditMode && value.isEmpty()) hint else value,
                    style = NestoryTextStyles.Body14Medium,
                    color = if (isEditMode && value.isEmpty()) GeneratedColor.Figma919191 else GeneratedColor.Figma000000
                )
            }
            
            val icon = trailingIcon ?: if (onClick != null && isEditMode) AppIcons.LsiconDownFilled else null
            if (icon != null) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        if (errorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000,
                modifier = Modifier.padding(start = 4.dp)
            )
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
                .background(GeneratedColor.FigmaFfffff)
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
    isSaving: Boolean = false,
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
                .clickable(enabled = !isSaving) { onDelete() },
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
                .clickable(enabled = !isSaving) { onSave() },
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
                    text = "Lưu",
                    style = NestoryTextStyles.Body15Semi,
                    color = GeneratedColor.FigmaFfffff
                )
            }
        }
    }
}

internal fun attachmentFileName(filePath: String): String =
    filePath.substringAfterLast('/').ifBlank { filePath }

internal fun pdfBaseName(filePath: String): String =
    attachmentFileName(filePath).removeSuffix(".pdf").removeSuffix(".PDF")

internal fun currentContainerName(containerPath: String): String =
    containerPath.substringAfterLast(" > ").ifBlank { containerPath }

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
