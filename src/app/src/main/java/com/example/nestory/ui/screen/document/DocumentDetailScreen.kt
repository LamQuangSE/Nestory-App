package com.example.nestory.ui.screen.document

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.ui.screen.category.CategoryRoute
import com.example.nestory.ui.screen.category.CategoryUiModel
import com.example.nestory.ui.screen.container.ContainerRoute
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DocumentDetailSubScreen { Detail, CategorySelection, ContainerSelection }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    document: DocumentUiModel,
    onBack: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }
    var subScreen by remember { mutableStateOf(DocumentDetailSubScreen.Detail) }
    
    // Editable state
    var editedName by remember(document.name) { mutableStateOf(document.name) }
    var editedCategory by remember(document.category) { mutableStateOf(document.category) }
    var editedExpiryDate by remember(document.expiryDate) { mutableStateOf(document.expiryDate) }
    var editedContainerPath by remember(document.containerPath) { mutableStateOf(document.containerPath) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    BackHandler(enabled = subScreen != DocumentDetailSubScreen.Detail) {
        subScreen = DocumentDetailSubScreen.Detail
    }

    if (subScreen == DocumentDetailSubScreen.CategorySelection) {
        CategoryRoute(
            onBack = { subScreen = DocumentDetailSubScreen.Detail },
            onConfirmSelection = { category ->
                editedCategory = category.name
                subScreen = DocumentDetailSubScreen.Detail
            }
        )
        return
    }

    if (subScreen == DocumentDetailSubScreen.ContainerSelection) {
        ContainerRoute(
            onBack = { subScreen = DocumentDetailSubScreen.Detail },
            onConfirmSelection = { container ->
                // Simulate updating container path
                editedContainerPath = container.name
                subScreen = DocumentDetailSubScreen.Detail
            }
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
                        editedExpiryDate = formatter.format(date)
                    }
                    showDatePicker = false
                }) {
                    Text("Xác nhận", color = GeneratedColor.Figma1a60e2)
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
                    if (isEditMode) {
                        isEditMode = false
                        // Reset edited values
                        editedName = document.name
                        editedCategory = document.category
                        editedExpiryDate = document.expiryDate
                        editedContainerPath = document.containerPath
                    } else {
                        onBack()
                    }
                },
                onEditToggle = { isEditMode = true }
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
                        value = if (isEditMode) editedName else document.name,
                        isEditMode = isEditMode,
                        hint = "Nhập tên giấy tờ ",
                        onValueChange = { editedName = it }
                    )
                    DetailField(
                        label = "Danh mục", 
                        value = if (isEditMode) editedCategory else document.category,
                        isEditMode = isEditMode,
                        hint = "Chọn danh mục",
                        onClick = { subScreen = DocumentDetailSubScreen.CategorySelection }
                    )
                }

                // Section 2: Thời hạn
                DetailSection(
                    title = "Thời hạn",
                    icon = AppIcons.DocumentDeadline
                ) {
                    DetailField(
                        label = "Ngày hết hạn", 
                        value = if (isEditMode) editedExpiryDate else document.expiryDate,
                        isEditMode = isEditMode,
                        hint = "Chọn ngày hết hạn ",
                        onClick = { showDatePicker = true }
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
                        value = if (isEditMode) editedContainerPath else "Ngăn 4", 
                        isEditMode = isEditMode,
                        hint = "Chọn container",
                        onClick = { subScreen = DocumentDetailSubScreen.ContainerSelection }
                    )
                    if (!isEditMode) {
                        DetailField(label = "Đường dẫn nơi lưu trữ", value = document.containerPath)
                    }
                }

                if (isEditMode) {
                    // Section 4: Tùy chọn (Only in Edit Mode)
                    DetailSection(
                        title = "Tùy chọn",
                        icon = AppIcons.DocumentConfig
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Đánh dấu là yêu thích ",
                                style = NestoryTextStyles.Body12Semi,
                                color = GeneratedColor.Figma000000
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(AppIcons.DocumentStarred),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    EditActions(
                        onCancel = { 
                            isEditMode = false 
                            editedName = document.name
                            editedCategory = document.category
                            editedExpiryDate = document.expiryDate
                            editedContainerPath = document.containerPath
                        },
                        onSave = { 
                            // In a real app, we would update the document here via ViewModel
                            isEditMode = false 
                        },
                        onDelete = {
                            isEditMode = false
                            onDelete()
                        }
                    )
                } else {
                    // Section 4: Tệp scan (Only in View Mode)
                    DetailSection(
                        title = "Tệp scan",
                        icon = AppIcons.DocumentFileScan
                    ) {
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
                    }
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
    onEditToggle: () -> Unit
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
            // Edit Button (Node 272:165)
            Image(
                painter = painterResource(AppIcons.DocumentEdit),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onEditToggle() },
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(10.dp))
            // Star Button
            Image(
                painter = painterResource(AppIcons.DocumentStarred),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
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
    onValueChange: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null
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
                .then(
                    if (isEditMode && onClick != null) Modifier.clickable { onClick() }
                    else Modifier
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isEditMode && onValueChange != null && onClick == null) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = NestoryTextStyles.Body14Medium.copy(color = GeneratedColor.Figma000000),
                    modifier = Modifier.fillMaxWidth(),
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
                    text = if (isEditMode && value.isEmpty()) hint else value,
                    style = NestoryTextStyles.Body14Medium,
                    color = if (isEditMode && value.isEmpty()) GeneratedColor.Figma919191 else GeneratedColor.Figma000000
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
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(NestoryRadius.R10)
                    .background(GeneratedColor.FigmaEdebff)
                    .clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hủy",
                    style = NestoryTextStyles.Body15Semi,
                    color = GeneratedColor.Figma6d28d9
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(NestoryRadius.R10)
                    .background(GeneratedColor.Figma1a60e2)
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
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Xóa",
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DocumentDetailViewPreview() {
    DocumentDetailScreen(
        document = DocumentUiModel(
            id = "1",
            name = "Hợp đồng thuê nhà 2026",
            category = "Hợp đồng, Pháp lý",
            containerPath = "Tủ tài liệu > Ngăn 4",
            status = DocumentStatus.Active,
            expiryDate = "20/08/2026",
            categoryColor = Color(0xFF1855EE)
        ),
        onBack = {}
    )
}
