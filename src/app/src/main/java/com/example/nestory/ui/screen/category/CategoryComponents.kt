package com.example.nestory.ui.screen.category

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryTextStyles

private val CategoryRadius = RoundedCornerShape(10.dp)

@Composable
fun CategoryHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Đã bọc Image vào trong Box và chuyển clickable ra ngoài để tối ưu vùng chạm
        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = AppIcons.IcBackwardArrow),
                contentDescription = "Trở về",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Title22Bold.copy(
                fontSize = 20.sp,
                lineHeight = 24.2.sp,
                fontWeight = FontWeight.W700
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategorySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        textStyle = NestoryTextStyles.Body13Medium.copy(
            color = GeneratedColor.Figma000000,
            fontSize = 13.sp,
            lineHeight = 15.73.sp,
            fontWeight = FontWeight.W500
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, CategoryRadius),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(55.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = AppIcons.IcSearch),
                        contentDescription = "Tìm kiếm",
                        modifier = Modifier.size(27.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Tìm danh mục",
                            color = GeneratedColor.Figma919191,
                            style = NestoryTextStyles.Body13Medium.copy(
                                fontSize = 13.sp,
                                lineHeight = 15.73.sp,
                                fontWeight = FontWeight.W500
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun CategoryListItem(
    category: CategoryUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(if (isSelected) GeneratedColor.FigmaF3f6ff else GeneratedColor.FigmaFfffff)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(45.dp)
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(29.dp)
                    .clip(CircleShape)
                    .background(category.color)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .padding(start = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.name,
                style = NestoryTextStyles.Body14Medium.copy(
                    fontSize = 14.sp,
                    lineHeight = 16.94.sp,
                    fontWeight = FontWeight.W600
                ),
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(width = 45.dp, height = 60.dp)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = AppIcons.IcTrash),
                    contentDescription = "Xóa danh mục",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun CategoryDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GeneratedColor.FigmaE5e7eb)
    )
}

@Composable
fun CategoryListFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, CategoryRadius)
    ) {
        content()
    }
}

@Composable
fun EmptyCategoryCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, CategoryRadius),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = AppImages.ImgEmptyCategory),
            contentDescription = null,
            modifier = Modifier.size(width = 282.dp, height = 210.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Chưa có danh mục nào",
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Title22Bold.copy(
                fontSize = 22.sp,
                lineHeight = 26.62.sp,
                fontWeight = FontWeight.W600
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Thêm danh mục đầu tiên để bắt đầu quản lý\ntrong Nestory",
            color = GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body13Medium.copy(
                fontSize = 13.sp,
                lineHeight = 15.73.sp,
                fontWeight = FontWeight.W600
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CategoryNameField(
    name: String,
    error: String?,
    onNameChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(if (error == null) 111.dp else 128.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Áp dụng buildAnnotatedString để tô đỏ dấu *
        Text(
            text = buildAnnotatedString {
                append("Tên giấy tờ ")
                withStyle(style = SpanStyle(color = GeneratedColor.FigmaCf1111)) {
                    append("*")
                }
            },
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Body16Semi.copy(
                fontSize = 16.sp,
                lineHeight = 19.36.sp,
                fontWeight = FontWeight.W600
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        BasicTextField(
            value = name,
            onValueChange = {
                if (it.length <= 50) onNameChanged(it)
            },
            singleLine = true,
            textStyle = NestoryTextStyles.Body15Medium.copy(
                color = GeneratedColor.Figma000000,
                fontSize = 15.sp,
                lineHeight = 18.15.sp,
                fontWeight = FontWeight.W600
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp)
                        .border(1.dp, GeneratedColor.FigmaE5e7eb, CategoryRadius)
                        .padding(horizontal = 17.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (name.isEmpty()) {
                            Text(
                                text = "Nhập tên giấy tờ",
                                color = GeneratedColor.Figma919191,
                                style = NestoryTextStyles.Body15Medium.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 18.15.sp,
                                    fontWeight = FontWeight.W600
                                )
                            )
                        }
                        innerTextField()
                    }
                    Text(
                        text = "${name.length}/50",
                        color = GeneratedColor.Figma919191,
                        style = NestoryTextStyles.Body10Semi.copy(
                            fontSize = 11.sp,
                            lineHeight = 13.31.sp,
                            fontWeight = FontWeight.W600
                        )
                    )
                }
            }
        )
        if (error != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = error,
                color = GeneratedColor.FigmaFf0000,
                style = NestoryTextStyles.Body13Medium.copy(
                    fontSize = 13.sp,
                    lineHeight = 15.73.sp,
                    fontWeight = FontWeight.W500
                )
            )
        }
    }
}

@Composable
fun CategoryColorPicker(
    colors: List<Color>,
    selectedColor: Color?,
    error: String?,
    onSelectColor: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(253.dp)
            .padding(horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(41.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Áp dụng buildAnnotatedString để tô đỏ dấu *
            Text(
                text = buildAnnotatedString {
                    append("Màu sắc ")
                    withStyle(style = SpanStyle(color = GeneratedColor.FigmaCf1111)) {
                        append("*")
                    }
                },
                color = GeneratedColor.Figma000000,
                style = NestoryTextStyles.Body16Semi.copy(
                    fontSize = 16.sp,
                    lineHeight = 19.36.sp,
                    fontWeight = FontWeight.W600
                )
            )
        }
        colors.chunked(6).forEach { rowColors ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (error == null) 70.67.dp else 65.33.dp)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowColors.forEach { color ->
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 0.dp,
                                color = if (isSelected) GeneratedColor.Figma1855ee else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelectColor(color) }
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = GeneratedColor.FigmaFf0000,
                style = NestoryTextStyles.Body13Medium.copy(
                    fontSize = 13.sp,
                    lineHeight = 15.73.sp,
                    fontWeight = FontWeight.W600
                )
            )
        }
    }
}

@Composable
fun CategoryOutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .border(
                width = 1.dp,
                color = if (enabled) GeneratedColor.Figma1855ee else GeneratedColor.FigmaE5e7eb,
                shape = CategoryRadius
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) GeneratedColor.Figma1855ee else GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body15Medium.copy(
                fontSize = 15.sp,
                lineHeight = 18.15.sp,
                fontWeight = FontWeight.W600
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategoryPrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 42.dp,
    textSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CategoryRadius)
            .background(GeneratedColor.Figma1855ee)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = GeneratedColor.FigmaFfffff,
            style = NestoryTextStyles.Body16Semi.copy(
                fontSize = textSize,
                lineHeight = if (textSize == 14.sp) 16.94.sp else 19.36.sp,
                fontWeight = FontWeight.W600
            )
        )
    }
}

@Composable
fun DeleteCategoryDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(367.dp)
            .height(225.dp)
            .clip(CategoryRadius)
            .background(GeneratedColor.FigmaFfffff)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(69.dp)
                .padding(start = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Xác nhận xóa danh mục",
                color = GeneratedColor.Figma000000,
                style = NestoryTextStyles.Body16Semi.copy(
                    fontSize = 20.sp,
                    lineHeight = 24.2.sp,
                    fontWeight = FontWeight.W600
                )
            )
        }
        CategoryDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(77.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Áp dụng buildAnnotatedString để tô màu đỏ và in đậm tên danh mục
            Text(
                text = buildAnnotatedString {
                    append("Bạn có chắc chắn muốn xóa danh mục ")
                    withStyle(
                        style = SpanStyle(
                            color = GeneratedColor.FigmaCf1111,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(categoryName)
                    }
                    append(" này không?")
                },
                color = GeneratedColor.Figma000000,
                style = NestoryTextStyles.Body15Medium.copy(
                    fontSize = 15.sp,
                    lineHeight = 18.15.sp,
                    fontWeight = FontWeight.W500
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(79.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(79.dp)
                    .padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                DialogSecondaryButton(text = "Có", onClick = onConfirm)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(79.dp)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                DialogPrimaryButton(text = "Không", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun DialogSecondaryButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 155.dp, height = 45.dp)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, CategoryRadius)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body15Medium.copy(
                fontSize = 15.sp,
                lineHeight = 18.15.sp,
                fontWeight = FontWeight.W600
            )
        )
    }
}

@Composable
private fun DialogPrimaryButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 155.dp, height = 45.dp)
            .clip(CategoryRadius)
            .background(GeneratedColor.Figma1855ee)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = GeneratedColor.FigmaFfffff,
            style = NestoryTextStyles.Body15Medium.copy(
                fontSize = 15.sp,
                lineHeight = 18.15.sp,
                fontWeight = FontWeight.W600
            )
        )
    }
}

fun defaultCategoryColors(): List<Color> = listOf(
    Color(0xFFFCA5A5),
    Color(0xFFFDBA74),
    Color(0xFFFDE68A),
    Color(0xFFBEF264),
    Color(0xFF86EFAC),
    Color(0xFF6EE7B7),
    Color(0xFF5EEAD4),
    Color(0xFF67E8F9),
    Color(0xFF7DD3FC),
    Color(0xFF93C5FD),
    Color(0xFFA5B4FC),
    Color(0xFFC4B5FD),
    Color(0xFFD8B4FE),
    Color(0xFFE9A8F2),
    Color(0xFFF9A8D4),
    Color(0xFFFBCFE8),
    Color(0xFFCBD5E1),
    Color(0xFFC4B5A5)
)