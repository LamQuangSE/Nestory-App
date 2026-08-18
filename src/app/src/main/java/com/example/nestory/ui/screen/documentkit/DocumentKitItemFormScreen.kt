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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun DocumentKitItemFormScreen(
    onBackClick: () -> Unit,
    onSubmit: (name: String, description: String, note: String, requiredDocuments: Int?) -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialDescription: String = "",
    initialNote: String = "",
    initialRequiredDocuments: String = "",
    isEdit: Boolean = false,
    isSaving: Boolean = false,
    existingNames: List<String> = emptyList(),
    editLeaveRequested: Boolean = false,
    onEditBack: (name: String, description: String, note: String, requiredDocuments: Int?) -> Unit = { _, _, _, _ -> },
    linkedDocumentCount: Int = 0,
    linkedDocumentTitle: String? = null,
    onAddLinkedDocumentClick: () -> Unit = {},
    onLinkedDocumentClick: () -> Unit = {},
    onRemoveLinkedDocumentClick: () -> Unit = {},
    itemStatus: String? = null,
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var note by remember { mutableStateOf(initialNote) }
    var requiredDocs by remember { mutableStateOf(initialRequiredDocuments) }
    var showNameError by remember { mutableStateOf(false) }
    var showRequiredDocsError by remember { mutableStateOf(false) }
    var showDuplicateError by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val itemVisual = itemStatus?.let { kitStatusVisual(it) }

    fun checkDuplicate(name: String): Boolean =
        name.isNotBlank() && existingNames.any { it.equals(name.trim(), ignoreCase = true) }

    val hasContent = name.isNotBlank() ||
        description.isNotBlank() ||
        note.isNotBlank() ||
        requiredDocs.isNotBlank()

    BackHandler(enabled = isEdit || hasContent) {
        if (isEdit) {
            onEditBack(name, description, note, requiredDocs.toIntOrNull())
        } else {
            showDiscardDialog = true
        }
    }

    LaunchedEffect(editLeaveRequested) {
        if (editLeaveRequested) {
            when {
                isEdit -> onEditBack(name, description, note, requiredDocs.toIntOrNull())
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
            title = if (isEdit) "Chỉnh sửa Item" else "Tạo Item mới",
            onBack = {
                if (isEdit) {
                    onEditBack(name, description, note, requiredDocs.toIntOrNull())
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
            ItemNameCard(
                name = name,
                onNameChange = {
                    name = it
                    showNameError = false
                    showDuplicateError = false
                },
                showError = showNameError,
                showDuplicateError = showDuplicateError
            )

            ItemDescriptionNoteCard(
                description = description,
                onDescriptionChange = { description = it },
                note = note,
                onNoteChange = { note = it },
                itemVisual = itemVisual
            )

            if (isEdit) {
                ItemLinkedDocsCard(
                    linkedCount = linkedDocumentCount,
                    linkedDocumentTitle = if (linkedDocumentCount > 0) linkedDocumentTitle else null,
                    onAddClick = onAddLinkedDocumentClick,
                    onLinkedDocClick = onLinkedDocumentClick,
                    onRemoveClick = onRemoveLinkedDocumentClick,
                    itemVisual = itemVisual
                )
            } else {
                ItemRequiredDocsCard(
                    requiredDocs = requiredDocs,
                    onRequiredDocsChange = {
                        requiredDocs = it
                        showRequiredDocsError = false
                    },
                    showError = showRequiredDocsError
                )
            }
        }

        if (onDelete == null) {
            ItemPrimaryButton(
                text = "Tạo Item mới",
                isSaving = isSaving,
                onClick = {
                    if (checkDuplicate(name)) {
                        showDuplicateError = true
                    } else {
                        validateAndSubmit(
                            name = name,
                            requiredDocs = requiredDocs,
                            validateRequiredDocs = !isEdit,
                            onShowNameError = { showNameError = true },
                            onShowRequiredDocsError = { showRequiredDocsError = true },
                            onValid = { onSubmit(name, description, note, requiredDocs.toIntOrNull()) }
                        )
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = NestorySpacing.S10),
                horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
            ) {
                ItemDeleteButton(
                    onClick = { showDeleteConfirm = true },
                    isSaving = isSaving,
                    modifier = Modifier.weight(1f)
                )
                ItemPrimaryButton(
                    text = "Cập nhật Item",
                    isSaving = isSaving,
                    onClick = {
                        if (checkDuplicate(name)) {
                            showDuplicateError = true
                        } else {
                            validateAndSubmit(
                                name = name,
                                requiredDocs = requiredDocs,
                                validateRequiredDocs = !isEdit,
                                onShowNameError = { showNameError = true },
                                onShowRequiredDocsError = { showRequiredDocsError = true },
                                onValid = { onSubmit(name, description, note, requiredDocs.toIntOrNull()) }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showDiscardDialog) {
        ConfirmDiscardItemDialog(
            onConfirm = {
                showDiscardDialog = false
                onBackClick()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDeleteItemDialog(
            onConfirm = {
                showDeleteConfirm = false
                onDelete?.invoke()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

private fun validateAndSubmit(
    name: String,
    requiredDocs: String,
    validateRequiredDocs: Boolean,
    onShowNameError: () -> Unit,
    onShowRequiredDocsError: () -> Unit,
    onValid: () -> Unit,
) {
    val isNameValid = name.isNotBlank()
    val isRequiredDocsValid = !validateRequiredDocs || requiredDocs.toIntOrNull() != null
    if (!isNameValid) onShowNameError()
    if (validateRequiredDocs && !isRequiredDocsValid) onShowRequiredDocsError()
    if (isNameValid && isRequiredDocsValid) onValid()
}

@Composable
private fun ItemNameCard(
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
        KitRequiredLabel(text = "Tên Item")
        KitTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Nhập tên Item",
            isError = showError || showDuplicateError,
            testTag = "item_name_input"
        )
        if (showDuplicateError) {
            Text(
                text = "Tên item đã tồn tại trong bộ hồ sơ này",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        } else if (showError) {
            Text(
                text = "Tên item không được để trống",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
    }
}

@Composable
private fun ItemDescriptionNoteCard(
    description: String,
    onDescriptionChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    itemVisual: KitStatusVisual? = null,
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
            iconRes = AppIcons.KitTarget,
            iconBoxColor = itemVisual?.bgColor ?: GeneratedColor.FigmaF3eeff,
            iconTint = itemVisual?.iconTint ?: GeneratedColor.Figma522ec8
        )
        KitTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = "Nhập nội dung"
        )
        KitSectionHeader(
            title = "Ghi chú",
            iconRes = AppIcons.NestoryNote,
            iconBoxColor = itemVisual?.bgColor ?: GeneratedColor.FigmaF3eeff,
            iconTint = itemVisual?.iconTint ?: GeneratedColor.Figma522ec8
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
private fun ItemRequiredDocsCard(
    requiredDocs: String,
    onRequiredDocsChange: (String) -> Unit,
    showError: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S8, vertical = NestorySpacing.S8),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(GeneratedColor.FigmaF3eeff, RoundedCornerShape(NestorySpacing.S10))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = AppIcons.KitFile),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = GeneratedColor.Figma522ec8
                )
            }
            Spacer(modifier = Modifier.width(NestorySpacing.S10))
            Text(
                text = "Số giấy tờ cần liên kết",
                style = NestoryTextStyles.Body16Bold,
                color = GeneratedColor.Figma000000
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "*",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
        KitTextField(
            value = requiredDocs,
            onValueChange = onRequiredDocsChange,
            placeholder = "Nhập số lượng giấy tờ cần liên kết",
            height = 34.dp,
            keyboardType = KeyboardType.Number,
            isError = showError,
            testTag = "item_required_docs_input"
        )
        if (showError) {
            Text(
                text = "Số giấy tờ cần liên kết không được để trống",
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
    }
}

@Composable
private fun ItemLinkedDocsCard(
    linkedCount: Int,
    linkedDocumentTitle: String?,
    onAddClick: () -> Unit,
    onLinkedDocClick: () -> Unit,
    onRemoveClick: () -> Unit,
    itemVisual: KitStatusVisual? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(15.dp))
            .padding(horizontal = NestorySpacing.S8, vertical = NestorySpacing.S8),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        itemVisual?.bgColor ?: GeneratedColor.FigmaF3eeff,
                        RoundedCornerShape(NestorySpacing.S10),
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = AppIcons.KitFile),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = itemVisual?.iconTint ?: GeneratedColor.Figma522ec8
                )
            }
            Spacer(modifier = Modifier.width(NestorySpacing.S10))
            Text(
                text = "Giấy tờ đã liên kết ($linkedCount)",
                style = NestoryTextStyles.Body16Bold,
                color = GeneratedColor.Figma000000
            )
        }
        if (linkedCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(5.dp))
                    .clickable(onClick = onLinkedDocClick)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = linkedDocumentTitle?.ifBlank { "Giấy tờ đã liên kết" }
                        ?: "Giấy tờ đã liên kết",
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma000000,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = AppIcons.IcMoreInfor),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = GeneratedColor.Figma000000
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = AppIcons.IcTrash),
                    contentDescription = "Xóa giấy tờ liên kết",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onRemoveClick),
                    tint = GeneratedColor.FigmaEf4444
                )
            }
        }
        ItemAddLinkedDocumentButton(onClick = onAddClick)
    }
}

@Composable
private fun ItemAddLinkedDocumentButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .dashedBorder(color = GeneratedColor.Figma522ec8)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Thêm giấy tờ liên kết",
            style = NestoryTextStyles.Body15Medium,
            color = GeneratedColor.Figma522ec8
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    width: Dp = 1.dp,
    dash: Dp = 6.dp,
    gap: Dp = 6.dp,
): Modifier = this.drawBehind {
    val radius = 10.dp.toPx()
    drawRoundRect(
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), gap.toPx()))
        ),
        cornerRadius = CornerRadius(radius, radius)
    )
}

@Composable
private fun ItemPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(GeneratedColor.Figma522ec8, RoundedCornerShape(NestorySpacing.S10))
            .clickable(enabled = !isSaving, onClick = onClick),
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
                text = text,
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.FigmaFfffff
            )
        }
    }
}

@Composable
private fun ItemDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(GeneratedColor.FigmaFef2f2, RoundedCornerShape(NestorySpacing.S10))
            .border(1.dp, GeneratedColor.FigmaEf4444, RoundedCornerShape(NestorySpacing.S10))
            .clickable(enabled = !isSaving, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = AppIcons.IcTrash),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = GeneratedColor.FigmaEf4444
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Xóa Item",
            style = NestoryTextStyles.Body15Semi,
            color = GeneratedColor.FigmaEf4444
        )
    }
}
