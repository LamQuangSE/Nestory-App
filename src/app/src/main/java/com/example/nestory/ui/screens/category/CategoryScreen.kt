package com.example.nestory.ui.screens.category

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.theme.GeneratedColor

@Composable
fun CategoryScreen(
    uiState: CategoryUiState,
    onEvent: (CategoryEvent) -> Unit,
    onBack: () -> Unit
) {
    val filteredCategories = uiState.categories.filter {
        it.name.contains(uiState.query, ignoreCase = true)
    }
    val selectedCategory = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }
    val deleteTarget = uiState.categories.firstOrNull { it.id == uiState.deleteTargetId }

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
                        if (uiState.mode == CategoryMode.Selection) {
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
                        onEvent = onEvent
                    )
                } else {
                    CategoryFormContent(
                        uiState = uiState,
                        onEvent = onEvent
                    )
                }
            }
        }

        if (uiState.isDeleteDialogVisible) {
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
    onEvent: (CategoryEvent) -> Unit
) {
    CategorySearchField(
        query = uiState.query,
        onQueryChange = { onEvent(CategoryEvent.OnSearchChanged(it)) }
    )
    Spacer(modifier = Modifier.height(15.dp))

    if (uiState.categories.isEmpty()) {
        EmptyCategoryCard()
        Spacer(modifier = Modifier.height(15.dp))
        CategoryOutlinedActionButton(
            text = "Tạo danh mục mới",
            onClick = { onEvent(CategoryEvent.OnAddClick) },
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        CategoryListFrame {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(
                    items = filteredCategories,
                    key = { _, category -> category.id }
                ) { index, category ->
                    CategoryListItem(
                        category = category,
                        isSelected = category.id == uiState.selectedCategoryId,
                        onClick = { onEvent(CategoryEvent.OnCategorySelected(category.id)) },
                        onDelete = { onEvent(CategoryEvent.OnDeleteClick(category.id)) }
                    )
                    if (index < filteredCategories.lastIndex) {
                        CategoryDivider()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
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
                enabled = selectedCategory != null,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        CategoryPrimaryActionButton(
            text = "Xác nhận danh mục",
            onClick = { onEvent(CategoryEvent.OnConfirmSelection) }
        )
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

@Composable
fun CategoryRoute(
    onBack: () -> Unit
) {
    var uiState by remember {
        mutableStateOf(
            CategoryUiState(
                categories = listOf(
                    CategoryUiModel("1", "Bảo hiểm", Color(0xFFFCA5A5)),
                    CategoryUiModel("2", "Hợp đồng", Color(0xFFFDBA74)),
                    CategoryUiModel("3", "Hóa đơn", Color(0xFFFDE68A)),
                    CategoryUiModel("4", "Giấy tờ cá nhân", Color(0xFFBEF264)),
                    CategoryUiModel("5", "Bảo hành", Color(0xFF86EFAC)),
                    CategoryUiModel("6", "Y tế", Color(0xFF6EE7B7))
                ),
                selectedCategoryId = "1"
            )
        )
    }

    val handleEvent: (CategoryEvent) -> Unit = { event ->
        uiState = when (event) {
            is CategoryEvent.OnSearchChanged -> uiState.copy(query = event.query)
            is CategoryEvent.OnCategorySelected -> uiState.copy(selectedCategoryId = event.categoryId)
            CategoryEvent.OnAddClick -> uiState.copy(
                mode = CategoryMode.Create,
                form = CategoryFormUiState(selectedColor = defaultCategoryColors().first())
            )

            CategoryEvent.OnEditClick -> {
                val selected = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }
                if (selected == null) {
                    uiState
                } else {
                    uiState.copy(
                        mode = CategoryMode.Edit,
                        form = CategoryFormUiState(
                            name = selected.name,
                            selectedColor = selected.color
                        )
                    )
                }
            }

            is CategoryEvent.OnDeleteClick -> uiState.copy(deleteTargetId = event.categoryId)
            CategoryEvent.OnDismissDeleteDialog -> uiState.copy(deleteTargetId = null)
            CategoryEvent.OnConfirmDelete -> {
                val deleteId = uiState.deleteTargetId
                if (deleteId == null) {
                    uiState
                } else {
                    val updated = uiState.categories.filterNot { it.id == deleteId }
                    uiState.copy(
                        categories = updated,
                        selectedCategoryId = if (uiState.selectedCategoryId == deleteId) {
                            updated.firstOrNull()?.id
                        } else {
                            uiState.selectedCategoryId
                        },
                        deleteTargetId = null
                    )
                }
            }

            is CategoryEvent.OnNameChanged -> uiState.copy(
                form = uiState.form.copy(name = event.value, nameError = null)
            )

            is CategoryEvent.OnColorSelected -> uiState.copy(
                form = uiState.form.copy(selectedColor = event.color, colorError = null)
            )

            CategoryEvent.OnSubmitForm -> submitCategoryForm(uiState)
            CategoryEvent.OnCancelForm -> uiState.copy(
                mode = CategoryMode.Selection,
                form = CategoryFormUiState()
            )

            CategoryEvent.OnConfirmSelection -> uiState
        }
    }

    CategoryScreen(
        uiState = uiState,
        onEvent = handleEvent,
        onBack = onBack
    )
}

private fun submitCategoryForm(uiState: CategoryUiState): CategoryUiState {
    val inputName = uiState.form.name.trim()
    val selectedColor = uiState.form.selectedColor
    val selectedId = uiState.selectedCategoryId
    val isNameDuplicate = uiState.categories.any {
        it.name.equals(inputName, ignoreCase = true) &&
            !(uiState.mode == CategoryMode.Edit && it.id == selectedId)
    }
    val isColorDuplicate = uiState.categories.any {
        it.color == selectedColor &&
            !(uiState.mode == CategoryMode.Edit && it.id == selectedId)
    }

    val nameError = when {
        inputName.isBlank() -> "Tên giấy tờ không được để trống"
        isNameDuplicate -> "Tên $inputName đã tồn tại"
        else -> null
    }
    val colorError = when {
        selectedColor == null -> "Màu sắc không được để trống"
        isColorDuplicate -> "Màu sắc đã tồn tại"
        else -> null
    }

    return when {
        nameError != null || colorError != null -> uiState.copy(
            form = uiState.form.copy(nameError = nameError, colorError = colorError)
        )

        uiState.mode == CategoryMode.Create && selectedColor != null -> {
            val newItem = CategoryUiModel(
                id = System.currentTimeMillis().toString(),
                name = inputName,
                color = selectedColor
            )
            uiState.copy(
                categories = uiState.categories + newItem,
                selectedCategoryId = newItem.id,
                mode = CategoryMode.Selection,
                form = CategoryFormUiState(),
                query = ""
            )
        }

        uiState.mode == CategoryMode.Edit && selectedColor != null && selectedId != null -> uiState.copy(
            categories = uiState.categories.map { category ->
                if (category.id == selectedId) {
                    category.copy(name = inputName, color = selectedColor)
                } else {
                    category
                }
            },
            mode = CategoryMode.Selection,
            form = CategoryFormUiState(),
            query = ""
        )

        else -> uiState
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
