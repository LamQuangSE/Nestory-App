package com.example.nestory.ui.screen.ocr

import androidx.activity.compose.BackHandler
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
import com.example.nestory.ui.screen.category.CategoryRoute
import com.example.nestory.ui.screen.container.ContainerRoute
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OcrReviewSubScreen { Review, CategorySelection, ContainerSelection }
enum class DatePickerTarget { ExpiryDate }

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
    var subScreen by remember { mutableStateOf(OcrReviewSubScreen.Review) }
    var datePickerTarget by remember { mutableStateOf<DatePickerTarget?>(null) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var containerError by remember { mutableStateOf<String?>(null) }
    
    val datePickerState = rememberDatePickerState()
    val selectedContainer = containers.firstOrNull { it.id == draft.containerId }

    BackHandler(enabled = subScreen != OcrReviewSubScreen.Review) {
        subScreen = OcrReviewSubScreen.Review
    }

    if (subScreen == OcrReviewSubScreen.CategorySelection) {
        CategoryRoute(
            onBack = { subScreen = OcrReviewSubScreen.Review },
            onConfirmSelection = { category ->
                val matchedEnum = DocumentCategory.entries.find { categoryLabel(it) == category.name }
                    ?: DocumentCategory.OTHER
                onDraftChange(draft.copy(category = matchedEnum))
                subScreen = OcrReviewSubScreen.Review
            }
        )
        return
    }

    if (subScreen == OcrReviewSubScreen.ContainerSelection) {
        ContainerRoute(
            onBack = { subScreen = OcrReviewSubScreen.Review },
            onConfirmSelection = { container ->
                onDraftChange(draft.copy(containerId = container.id))
                containerError = null
                subScreen = OcrReviewSubScreen.Review
            }
        )
        return
    }

    if (datePickerTarget != null) {
        DatePickerDialog(
            onDismissRequest = { datePickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dateString = formatter.format(date)
                        onDraftChange(draft.copy(expiryDate = dateString))
                    }
                    datePickerTarget = null
                }) {
                    Text("Xác nhận", color = GeneratedColor.Figma1a60e2)
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerTarget = null }) {
                    Text("Hủy", color = GeneratedColor.Figma919191)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                    onValueChange = { 
                        onDraftChange(draft.copy(title = it))
                        if (it.isNotBlank()) nameError = null
                    },
                    error = nameError
                )

                ReviewField(
                    label = "Danh mục",
                    value = draft.category?.let { categoryLabel(it) } ?: "",
                    hint = "Chọn danh mục",
                    onClick = { subScreen = OcrReviewSubScreen.CategorySelection }
                )
            }

            // Section 2: Thời hạn
            ReviewSection(
                title = "Thời hạn",
                icon = AppIcons.DocumentDeadline
            ) {
                ReviewField(
                    label = "Ngày hết hạn",
                    value = draft.expiryDate.orEmpty(),
                    hint = "Chọn ngày hết hạn",
                    onClick = { datePickerTarget = DatePickerTarget.ExpiryDate }
                )
            }

            // Section 3: Vị trí lưu trữ
            ReviewSection(
                title = "Vị trí lưu trữ",
                icon = AppIcons.DocumentStorage
            ) {
                ReviewField(
                    label = "Nơi lưu trữ hiện tại",
                    value = selectedContainer?.name.orEmpty(),
                    hint = "Chọn container",
                    onClick = { subScreen = OcrReviewSubScreen.ContainerSelection },
                    error = containerError
                )
            }
            
            // Section 4: Thông tin bổ sung
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
                onClick = {
                    var hasError = false
                    if (draft.title.isBlank()) {
                        nameError = "Vui lòng nhập tên giấy tờ"
                        hasError = true
                    }
                    if (draft.containerId == null) {
                        containerError = "Vui lòng chọn vị trí lưu trữ"
                        hasError = true
                    }
                    
                    if (!hasError) {
                        onSave()
                    }
                },
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
    onValueChange: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    error: String? = null
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
                    width = 1.dp, 
                    color = if (error != null) Color.Red else GeneratedColor.FigmaE5e7eb, 
                    shape = NestoryRadius.R10
                )
                .then(
                    if (onClick != null) Modifier.clickable { onClick() }
                    else Modifier
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (onValueChange != null && onClick == null) {
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
            } else {
                Text(
                    text = value.ifEmpty { hint },
                    style = NestoryTextStyles.Body14Medium,
                    color = if (value.isEmpty()) GeneratedColor.Figma919191 else GeneratedColor.Figma000000
                )
            }
        }
        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                style = NestoryTextStyles.Body10Semi,
                color = Color.Red,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
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
