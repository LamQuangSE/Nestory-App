package com.example.nestory.ui.screen.category

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
import com.example.nestory.ui.theme.GeneratedColor

@Composable
fun CategoryRoute(
    onBack: () -> Unit
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

    CategoryScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}

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