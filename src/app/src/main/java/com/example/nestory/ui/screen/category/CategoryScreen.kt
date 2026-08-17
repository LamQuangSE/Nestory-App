package com.example.nestory.ui.screen.category

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.CategoryRepositoryImpl
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.categoryColor
import com.example.nestory.ui.theme.isPredefinedCategoryName

@Composable
fun CategoryRoute(
    onBack: () -> Unit,
    onConfirmSelection: ((CategoryUiModel) -> Unit)? = null,
    selectionOnly: Boolean = false,
    startInCreateMode: Boolean = false,
    onCreated: ((CategoryUiModel) -> Unit)? = null,
    allowCreate: Boolean = true,
    showPresetCategories: Boolean = false,
    initialSelectedName: String? = null
) {
    val context = LocalContext.current
    
    // Khởi tạo Repository thông qua Manual DI
    val repository = remember {
        val database = AppDatabase.getDatabase(context)
        CategoryRepositoryImpl(database.categoryDao())
    }
    
    // Khởi tạo ViewModel thông qua Factory
    val viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(repository)
    )
    
    // Lắng nghe UiState từ ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Khi được mở từ Create Document để tạo danh mục mới, vào thẳng form tạo
    // (cùng CategoryScreen/ViewModel với màn hình Category chính).
    LaunchedEffect(Unit) {
        if (startInCreateMode) {
            viewModel.onEvent(CategoryEvent.OnAddClick)
        }
    }

    // Khi mở để chọn danh mục cho giấy tờ đang chỉnh sửa, tự động chọn trước
    // danh mục hiện tại của giấy tờ để hiển thị dấu ✓ ngay khi mở selector.
    // Hiệu ứng này chạy lại khi danh sách được tải từ DB để chuyển từ row preset
    // tạm sang row thật, nhưng không bao giờ ghi đè lựa chọn của người dùng.
    LaunchedEffect(uiState.categories, initialSelectedName) {
        val name = initialSelectedName?.trim()
        if (name.isNullOrEmpty()) return@LaunchedEffect
        val all = buildPresetCategories(uiState.categories) + uiState.categories
        val match = all.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: return@LaunchedEffect
        val current = all.firstOrNull { it.id == uiState.selectedCategoryId }
        val keepCurrent = current != null && !current.name.equals(name, ignoreCase = true)
        if (!keepCurrent && uiState.selectedCategoryId != match.id) {
            viewModel.onEvent(CategoryEvent.OnCategorySelected(match.id))
        }
    }

    CategoryScreen(
        uiState = uiState,
        onEvent = { event ->
            when {
                event == CategoryEvent.OnSubmitForm && onCreated != null ->
                    viewModel.submitForm(onCreated)
                event == CategoryEvent.OnConfirmSelection && onConfirmSelection != null -> {
                    val selected = uiState.categories.find { it.id == uiState.selectedCategoryId }
                    if (selected != null) {
                        onConfirmSelection(selected)
                    } else {
                        val preset = buildPresetCategories(uiState.categories)
                            .firstOrNull { it.id == uiState.selectedCategoryId }
                        if (preset != null) {
                            viewModel.ensurePresetCategory(preset.name) {
                                onConfirmSelection(it)
                            }
                        }
                    }
                }
                event == CategoryEvent.OnAddClick && !allowCreate -> Unit
                else -> viewModel.onEvent(event)
            }
        },
        onBack = onBack,
        selectionOnly = selectionOnly,
        allowCreate = allowCreate,
        showPresetCategories = showPresetCategories,
        exitOnFormBack = startInCreateMode
    )
}

@Composable
fun CategoryScreen(
    uiState: CategoryUiState,
    onEvent: (CategoryEvent) -> Unit,
    onBack: () -> Unit,
    selectionOnly: Boolean = false,
    allowCreate: Boolean = true,
    showPresetCategories: Boolean = false,
    exitOnFormBack: Boolean = false
) {
    val displayCategories = if (showPresetCategories) {
        buildPresetCategories(uiState.categories) + uiState.categories
    } else {
        uiState.categories
    }
    val filteredCategories = displayCategories.filter {
        it.name.contains(uiState.query, ignoreCase = true)
    }
    val selectedCategory = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }
    val deleteTarget = uiState.categories.firstOrNull { it.id == uiState.deleteTargetId }
    val hasAnyCategory = displayCategories.isNotEmpty()

    BackHandler {
        when {
            uiState.isDeleteDialogVisible -> onEvent(CategoryEvent.OnDismissDeleteDialog)
            uiState.mode == CategoryMode.Selection -> onBack()
            exitOnFormBack -> onBack()
            else -> onEvent(CategoryEvent.OnCancelForm)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GeneratedColor.FigmaFfffff
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                CategoryHeader(
                    title = when (uiState.mode) {
                        CategoryMode.Selection -> "Chọn danh mục"
                        CategoryMode.Create -> "Tạo danh mục mới"
                        CategoryMode.Edit -> "Chỉnh sửa danh mục"
                    },
                    onBack = {
                        if (uiState.mode == CategoryMode.Selection || exitOnFormBack) {
                            onBack()
                        } else {
                            onEvent(CategoryEvent.OnCancelForm)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(15.dp))
                if (uiState.mode == CategoryMode.Selection) {
                    CategorySelectionContent(
                        uiState = uiState,
                        filteredCategories = filteredCategories,
                        selectedCategory = selectedCategory,
                        hasAnyCategory = hasAnyCategory,
                        onEvent = onEvent,
                        selectionOnly = selectionOnly,
                        allowCreate = allowCreate
                    )
                } else {
                    CategoryFormContent(
                        uiState = uiState,
                        onEvent = onEvent
                    )
                }
            }
        }

        if (uiState.isDeleteDialogVisible && !selectionOnly) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GeneratedColor.Figma000000.copy(alpha = 0.62f))
                    .padding(top = 296.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                DeleteCategoryDialog(
                    categoryName = deleteTarget?.name.orEmpty(),
                    onConfirm = { onEvent(CategoryEvent.OnConfirmDelete) },
                    onDismiss = { onEvent(CategoryEvent.OnDismissDeleteDialog) }
                )
            }
        }
    }
}

@Composable
private fun CategorySelectionContent(
    uiState: CategoryUiState,
    filteredCategories: List<CategoryUiModel>,
    selectedCategory: CategoryUiModel?,
    hasAnyCategory: Boolean,
    onEvent: (CategoryEvent) -> Unit,
    selectionOnly: Boolean = false,
    allowCreate: Boolean = true
) {
    CategorySearchField(
        query = uiState.query,
        onQueryChange = { onEvent(CategoryEvent.OnSearchChanged(it)) }
    )
    Spacer(modifier = Modifier.height(15.dp))

    if (!hasAnyCategory) {
        EmptyCategoryCard()
        if (allowCreate) {
            Spacer(modifier = Modifier.height(15.dp))
            CategoryOutlinedActionButton(
                text = "Tạo danh mục mới",
                onClick = { onEvent(CategoryEvent.OnAddClick) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        CategoryListFrame {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(
                    items = filteredCategories,
                    key = { _, category -> category.id }
                ) { index, category ->
                    val isPredefined = isPredefinedCategoryName(category.name)
                    CategoryListItem(
                        category = category,
                        isSelected = category.id == uiState.selectedCategoryId,
                        onClick = { onEvent(CategoryEvent.OnCategorySelected(category.id)) },
                        onDelete = if (selectionOnly || isPredefined) null else { { onEvent(CategoryEvent.OnDeleteClick(category.id)) } }
                    )
                    if (index < filteredCategories.lastIndex) {
                        CategoryDivider()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (selectionOnly && !allowCreate) {
            // Selection-only picker without any Category management actions
            // (used by Edit Document: change assignment only).
        } else if (selectionOnly) {
            CategoryOutlinedActionButton(
                text = "Tạo danh mục mới",
                onClick = { onEvent(CategoryEvent.OnAddClick) },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CategoryOutlinedActionButton(
                    text = "Tạo danh mục mới",
                    onClick = { onEvent(CategoryEvent.OnAddClick) },
                    modifier = Modifier.weight(1f)
                )
                CategoryOutlinedActionButton(
                    text = "Chỉnh sửa danh mục",
                    onClick = { onEvent(CategoryEvent.OnEditClick) },
                    enabled = selectedCategory != null && !isPredefinedCategoryName(selectedCategory.name),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (selectionOnly) {
            CategoryPrimaryActionButton(
                text = "Xác nhận danh mục",
                onClick = { onEvent(CategoryEvent.OnConfirmSelection) }
            )
        }
    }
}

@Composable
private fun CategoryFormContent(
    uiState: CategoryUiState,
    onEvent: (CategoryEvent) -> Unit
) {
    CategoryNameField(
        name = uiState.form.name,
        error = uiState.form.nameError,
        onNameChanged = { onEvent(CategoryEvent.OnNameChanged(it)) }
    )
    Spacer(modifier = Modifier.height(15.dp))
    CategoryColorPicker(
        colors = defaultCategoryColors(),
        selectedColor = uiState.form.selectedColor,
        error = uiState.form.colorError,
        onSelectColor = { onEvent(CategoryEvent.OnColorSelected(it)) }
    )
    Spacer(modifier = Modifier.height(15.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        CategoryPrimaryActionButton(
            text = if (uiState.mode == CategoryMode.Create) "Tạo danh mục mới" else "Lưu",
            onClick = { onEvent(CategoryEvent.OnSubmitForm) },
            height = 50.dp,
            textSize = if (uiState.mode == CategoryMode.Create) 14.sp else 16.sp
        )
    }
}

internal const val PRESET_CATEGORY_ID_PREFIX = "preset_"

internal fun buildPresetCategories(userCategories: List<CategoryUiModel>): List<CategoryUiModel> {
    val existingNames = userCategories.map { it.name.trim().lowercase() }.toSet()
    return DocumentCategory.entries
        .mapNotNull { preset ->
            val label = preset.toVietnameseLabel()
            if (label.lowercase() in existingNames) return@mapNotNull null
            CategoryUiModel(
                id = "$PRESET_CATEGORY_ID_PREFIX${preset.name}",
                name = label,
                color = preset.categoryColor
            )
        }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    CategoryScreen(
        uiState = CategoryUiState(
            categories = listOf(
                CategoryUiModel("1", "Bảo hiểm", Color(0xFFFCA5A5)),
                CategoryUiModel("2", "Hợp đồng", Color(0xFFFDBA74))
            ),
            selectedCategoryId = "1"
        ),
        onEvent = {},
        onBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenEmptyPreview() {
    CategoryScreen(
        uiState = CategoryUiState(categories = emptyList()),
        onEvent = {},
        onBack = {}
    )
}
