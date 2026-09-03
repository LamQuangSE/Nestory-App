package com.example.nestory.ui.screen.ocr

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.nestory.R
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.repository.CategoryRepositoryImpl
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.model.DocumentDraft
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.LocalInputMonitor
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.screen.category.CategoryDivider
import com.example.nestory.ui.screen.category.CategoryHeader
import com.example.nestory.ui.screen.category.CategoryOutlinedActionButton
import com.example.nestory.ui.screen.category.CategoryPrimaryActionButton
import com.example.nestory.ui.screen.category.CategoryRoute
import com.example.nestory.ui.screen.category.CategoryUiModel
import com.example.nestory.ui.screen.category.CategoryViewModel
import com.example.nestory.ui.screen.category.CategoryViewModelFactory
import com.example.nestory.ui.screen.container.ContainerRoute
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles
import com.example.nestory.ui.theme.categoryColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class OcrReviewSubScreen { Review, CategorySelection, ContainerSelection }
enum class DatePickerTarget { ExpiryDate }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrReviewScreen(
    draft: DocumentDraft,
    bitmaps: List<android.graphics.Bitmap>,
    containers: List<ContainerEntity>,
    onDraftChange: (DocumentDraft) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    fieldErrors: OcrFieldErrors = OcrFieldErrors(),
    isSaving: Boolean = false,
    categorySelectionContent: (@Composable (
        onBack: () -> Unit,
        onConfirmSelection: (CategoryUiModel) -> Unit,
    ) -> Unit)? = null,
) {
    var subScreen by remember { mutableStateOf(OcrReviewSubScreen.Review) }
    var datePickerTarget by remember { mutableStateOf<DatePickerTarget?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val selectedContainer = containers.firstOrNull { it.id == draft.containerId }
    val selectedCategoryName = draft.categoryName
        ?: draft.category?.toVietnameseLabel().orEmpty()
    val onCategorySelected: (CategoryUiModel) -> Unit = { category ->
        val matchedCategory = DocumentCategory.entries.find { it.toVietnameseLabel() == category.name }
        onDraftChange(
            draft.copy(
                category = matchedCategory,
                categoryId = category.id,
                categoryName = category.name,
            ),
        )
        subScreen = OcrReviewSubScreen.Review
    }

    BackHandler {
        when {
            showDiscardDialog -> showDiscardDialog = false
            datePickerTarget != null -> datePickerTarget = null
            subScreen != OcrReviewSubScreen.Review -> subScreen = OcrReviewSubScreen.Review
            else -> showDiscardDialog = true
        }
    }

    if (subScreen == OcrReviewSubScreen.CategorySelection) {
        val handleBack = { subScreen = OcrReviewSubScreen.Review }
        if (categorySelectionContent != null) {
            categorySelectionContent(handleBack, onCategorySelected)
        } else {
            CategoryRoute(
                onBack = handleBack,
                onConfirmSelection = onCategorySelected,
            )
        }
        return
    }

    if (subScreen == OcrReviewSubScreen.ContainerSelection) {
        ContainerRoute(
            onBack = { subScreen = OcrReviewSubScreen.Review },
            onConfirmSelection = { container ->
                onDraftChange(draft.copy(containerId = container.id))
                subScreen = OcrReviewSubScreen.Review
            },
            selectionOnly = true,
        )
        return
    }

    if (datePickerTarget != null) {
        OcrDatePickerDialog(
            initialDate = draft.expiryDate,
            onConfirm = { selected ->
                onDraftChange(draft.copy(expiryDate = selected))
                datePickerTarget = null
            },
            onDismiss = { datePickerTarget = null },
        )
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

            if (bitmaps.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bitmaps) { bitmap ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(150.dp)
                                .clip(NestoryRadius.R15)
                                .background(GeneratedColor.FigmaFfffff)
                                .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R15),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Document Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
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
                    errorText = when {
                        fieldErrors.title -> "Vui lòng nhập tên giấy tờ"
                        fieldErrors.titleDuplicate -> "Tên giấy tờ đã tồn tại"
                        else -> null
                    },
                    required = true,
                )

                ReviewField(
                    label = "Danh mục",
                    value = selectedCategoryName,
                    hint = "Chọn danh mục",
                    onClick = { subScreen = OcrReviewSubScreen.CategorySelection },
                    errorText = if (fieldErrors.category) "Vui lòng chọn danh mục" else null,
                    required = true,
                )
            }

            ReviewSection(
                title = "Thời hạn",
                icon = AppIcons.DocumentDeadline
            ) {
                ReviewField(
                    label = "Ngày hết hạn",
                    value = draft.expiryDate.orEmpty(),
                    hint = "Chọn ngày hết hạn",
                    onClick = { datePickerTarget = DatePickerTarget.ExpiryDate },
                    errorText = if (fieldErrors.expiryDate) "Vui lòng chọn ngày hết hạn" else null,
                    trailingIcon = AppIcons.KitCalendar,
                    required = true,
                )
            }

            ReviewSection(
                title = "Vị trí lưu trữ",
                icon = AppIcons.DocumentStorage
            ) {
                ReviewField(
                    label = "Nơi lưu trữ hiện tại",
                    value = selectedContainer?.name.orEmpty(),
                    hint = "Chọn container",
                    onClick = { subScreen = OcrReviewSubScreen.ContainerSelection },
                    errorText = if (fieldErrors.container) "Vui lòng chọn nơi lưu trữ" else null,
                    required = true,
                )
            }
            

            PrimaryActionButton(
                text = if (isSaving) "Đang lưu..." else "Lưu giấy tờ",
                onClick = onSave,
                enabled = !isSaving,
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
    errorText: String? = null,
    trailingIcon: Int? = null,
    required: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    val monitor = LocalInputMonitor.current
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (required) {
                    withStyle(SpanStyle(color = Color.Red)) { append(" *") }
                }
            },
            style = NestoryTextStyles.Body12Semi,
            color = GeneratedColor.Figma000000
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .testTag("ReviewField:$label")
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaFfffff)
                .border(
                    1.dp,
                    if (errorText != null) Color.Red else GeneratedColor.FigmaE5e7eb,
                    NestoryRadius.R10,
                )
                .then(
                    when {
                        onClick != null -> Modifier.clickable(onClick = onClick)
                        onValueChange != null -> Modifier.clickable { focusRequester.requestFocus() }
                        else -> Modifier
                    }
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (onValueChange != null && onClick == null) {
                BasicTextField(
                    value = value,
                    onValueChange = {
                        onValueChange(it)
                        monitor.update(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (it.isFocused) monitor.show(value, label)
                            else monitor.hide()
                        },
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
            
            val icon = trailingIcon ?: if (onClick != null) AppIcons.LsiconDownFilled else null
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
                style = NestoryTextStyles.Body10Semi,
                color = Color.Red,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
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
        initialSelectedDateMillis = initialDate?.let { dateStr ->
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.parse(dateStr)?.time
            } catch (e: Exception) {
                null
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Date(millis)
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        onConfirm(formatter.format(date))
                    } else {
                        onDismiss()
                    }
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

@Composable
fun DocumentCategorySelectionScreen(
    selectedCategoryName: String?,
    onBack: () -> Unit,
    onConfirmSelection: (CategoryUiModel) -> Unit,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val repository = remember { CategoryRepositoryImpl(db.categoryDao()) }
    val factory = remember { CategoryViewModelFactory(repository) }
    val viewModel: CategoryViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    val presetCategories = remember { DocumentCategory.entries }

    val userCategories = uiState.categories.filter { category ->
        presetCategories.none { it.toVietnameseLabel().equals(category.name, ignoreCase = true) }
    }

    val rows = presetCategories.map { category ->
        category.toVietnameseLabel() to category.categoryColor
    } + userCategories.map { it.name to it.color }

    var selected by remember { mutableStateOf(selectedCategoryName) }
    var isConfirming by remember { mutableStateOf(false) }
    var showCreateFlow by remember { mutableStateOf(false) }

    // "Tạo danh mục mới" opens the real Category creation flow (the same
    // CategoryScreen/CategoryViewModel used by the main Category screen). After the
    // category is actually created there, it is returned and auto-selected here.
    if (showCreateFlow) {
        CategoryRoute(
            onBack = { showCreateFlow = false },
            startInCreateMode = true,
            selectionOnly = true,
            onCreated = { category ->
                showCreateFlow = false
                onConfirmSelection(category)
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeneratedColor.FigmaFfffff)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        CategoryHeader(title = "Chọn danh mục", onBack = onBack)
        Spacer(modifier = Modifier.height(15.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(10.dp))
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(rows) { index, (label, color) ->
                    val isSelected = label == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(if (isSelected) GeneratedColor.FigmaF3f6ff else Color.Transparent)
                            .clickable { selected = label }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(29.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = label,
                            style = NestoryTextStyles.Body15Medium,
                            color = GeneratedColor.Figma000000,
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = AppIcons.KitCheckCircle),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    if (index < rows.lastIndex) {
                        CategoryDivider()
                    }
                }
            }
        }

        CategoryOutlinedActionButton(
            text = "Tạo danh mục mới",
            onClick = { showCreateFlow = true },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(15.dp))
        CategoryPrimaryActionButton(
            text = "Xác nhận danh mục",
            onClick = {
                if (isConfirming) return@CategoryPrimaryActionButton
                val label = selected
                val selectedUser = userCategories.firstOrNull { it.name == label }
                val selectedPreset = presetCategories.firstOrNull { it.toVietnameseLabel() == label }
                when {
                    selectedUser != null -> onConfirmSelection(selectedUser)
                    selectedPreset != null -> {
                        isConfirming = true
                        viewModel.ensurePresetCategory(
                            name = selectedPreset.toVietnameseLabel(),
                        ) { model ->
                            isConfirming = false
                            onConfirmSelection(model)
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}
