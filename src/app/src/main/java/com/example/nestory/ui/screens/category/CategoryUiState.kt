package com.example.nestory.ui.screens.category

import androidx.compose.ui.graphics.Color

data class CategoryUiModel(
    val id: String,
    val name: String,
    val color: Color
)

enum class CategoryMode {
    Selection,
    Create,
    Edit
}

data class CategoryFormUiState(
    val name: String = "",
    val selectedColor: Color? = null,
    val nameError: String? = null,
    val colorError: String? = null
)

data class CategoryUiState(
    val categories: List<CategoryUiModel> = emptyList(),
    val query: String = "",
    val selectedCategoryId: String? = null,
    val mode: CategoryMode = CategoryMode.Selection,
    val form: CategoryFormUiState = CategoryFormUiState(),
    val deleteTargetId: String? = null
) {
    val isDeleteDialogVisible: Boolean
        get() = deleteTargetId != null
}

sealed interface CategoryEvent {
    data class OnSearchChanged(val query: String) : CategoryEvent
    data class OnCategorySelected(val categoryId: String) : CategoryEvent
    object OnAddClick : CategoryEvent
    object OnEditClick : CategoryEvent
    data class OnDeleteClick(val categoryId: String) : CategoryEvent
    object OnDismissDeleteDialog : CategoryEvent
    object OnConfirmDelete : CategoryEvent
    data class OnNameChanged(val value: String) : CategoryEvent
    data class OnColorSelected(val color: Color) : CategoryEvent
    object OnSubmitForm : CategoryEvent
    object OnCancelForm : CategoryEvent
    object OnConfirmSelection : CategoryEvent
}