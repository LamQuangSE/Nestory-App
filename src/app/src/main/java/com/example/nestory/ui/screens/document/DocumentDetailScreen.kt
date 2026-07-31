package com.example.nestory.ui.screens.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    onDelete: () -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }
    
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
                    if (isEditMode) isEditMode = false else onBack()
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
                        value = document.name,
                        isEditMode = isEditMode,
                        hint = "Nhập tên giấy tờ "
                    )
                    DetailField(
                        label = "Danh mục", 
                        value = document.category,
                        isEditMode = isEditMode,
                        hint = "Chọn hoặc nhập danh mục"
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
                        hint = "Chọn ngày hết hạn "
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
                        onCancel = { isEditMode = false },
                        onSave = { isEditMode = false },
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
    hint: String = ""
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
            Text(
                text = if (isEditMode) hint else value,
                style = NestoryTextStyles.Body14Medium,
                color = if (isEditMode) GeneratedColor.Figma919191 else GeneratedColor.Figma000000
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
