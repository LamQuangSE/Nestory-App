package com.example.nestory.ui.screen.category

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.CategoryEntity
import com.example.nestory.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    // Trạng thái cục bộ (Query tìm kiếm, Text input, Màu đang chọn...)
    private val _internalState = MutableStateFlow(CategoryUiState())

    // Gộp dữ liệu từ Database (realtime) với Trạng thái cục bộ để tạo ra UiState cuối cùng
    val uiState: StateFlow<CategoryUiState> = combine(
        repository.getAllCategories(),
        _internalState
    ) { entities, state ->
        val categories = entities.map {
            CategoryUiModel(
                id = it.id,
                name = it.name,
                color = Color(it.colorValue.toULong()) // Phục hồi Color từ Long
            )
        }
        state.copy(categories = categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryUiState()
    )

    fun onEvent(event: CategoryEvent) {
        when (event) {
            is CategoryEvent.OnSearchChanged -> {
                _internalState.update { it.copy(query = event.query) }
            }
            is CategoryEvent.OnCategorySelected -> {
                _internalState.update { it.copy(selectedCategoryId = event.categoryId) }
            }
            CategoryEvent.OnAddClick -> {
                _internalState.update {
                    it.copy(
                        mode = CategoryMode.Create,
                        form = CategoryFormUiState(selectedColor = defaultCategoryColors().first())
                    )
                }
            }
            CategoryEvent.OnEditClick -> {
                val currentState = uiState.value
                val selected = currentState.categories.firstOrNull { it.id == currentState.selectedCategoryId }
                if (selected != null) {
                    _internalState.update {
                        it.copy(
                            mode = CategoryMode.Edit,
                            form = CategoryFormUiState(
                                name = selected.name,
                                selectedColor = selected.color
                            )
                        )
                    }
                }
            }
            is CategoryEvent.OnDeleteClick -> {
                _internalState.update { it.copy(deleteTargetId = event.categoryId) }
            }
            CategoryEvent.OnDismissDeleteDialog -> {
                _internalState.update { it.copy(deleteTargetId = null) }
            }
            CategoryEvent.OnConfirmDelete -> {
                val deleteId = uiState.value.deleteTargetId
                if (deleteId != null) {
                    viewModelScope.launch {
                        repository.deleteCategory(deleteId)
                    }
                    _internalState.update {
                        it.copy(
                            selectedCategoryId = if (it.selectedCategoryId == deleteId) null else it.selectedCategoryId,
                            deleteTargetId = null
                        )
                    }
                }
            }
            is CategoryEvent.OnNameChanged -> {
                _internalState.update {
                    it.copy(form = it.form.copy(name = event.value, nameError = null))
                }
            }
            is CategoryEvent.OnColorSelected -> {
                _internalState.update {
                    it.copy(form = it.form.copy(selectedColor = event.color, colorError = null))
                }
            }
            CategoryEvent.OnSubmitForm -> {
                submitForm()
            }
            CategoryEvent.OnCancelForm -> {
                _internalState.update {
                    it.copy(
                        mode = CategoryMode.Selection,
                        form = CategoryFormUiState()
                    )
                }
            }
            CategoryEvent.OnConfirmSelection -> {
                // Xử lý logic khi bấm Xác nhận danh mục (ví dụ: pop back stack kèm data)
            }
        }
    }

    private fun submitForm() {
        val currentState = uiState.value
        val inputName = currentState.form.name.trim()
        val selectedColor = currentState.form.selectedColor
        val selectedId = currentState.selectedCategoryId

        val isNameDuplicate = currentState.categories.any {
            it.name.equals(inputName, ignoreCase = true) &&
                    !(currentState.mode == CategoryMode.Edit && it.id == selectedId)
        }
        val isColorDuplicate = currentState.categories.any {
            it.color == selectedColor &&
                    !(currentState.mode == CategoryMode.Edit && it.id == selectedId)
        }

        val nameError = when {
            inputName.isBlank() -> "Tên giấy tờ không được để trống"
            isNameDuplicate -> "Tên '$inputName' đã tồn tại"
            else -> null
        }
        val colorError = when {
            selectedColor == null -> "Màu sắc không được để trống"
            isColorDuplicate -> "Màu sắc đã tồn tại"
            else -> null
        }

        if (nameError != null || colorError != null) {
            _internalState.update {
                it.copy(form = it.form.copy(nameError = nameError, colorError = colorError))
            }
            return
        }

        viewModelScope.launch {
            if (currentState.mode == CategoryMode.Create && selectedColor != null) {
                val newEntity = CategoryEntity(
                    id = System.currentTimeMillis().toString(),
                    name = inputName,
                    colorValue = selectedColor.value.toLong() // Ép kiểu về Long để lưu Database
                )
                repository.insertCategory(newEntity)
                
                _internalState.update {
                    it.copy(
                        selectedCategoryId = newEntity.id,
                        mode = CategoryMode.Selection,
                        form = CategoryFormUiState(),
                        query = ""
                    )
                }
            } else if (currentState.mode == CategoryMode.Edit && selectedColor != null && selectedId != null) {
                val updateEntity = CategoryEntity(
                    id = selectedId,
                    name = inputName,
                    colorValue = selectedColor.value.toLong()
                )
                repository.updateCategory(updateEntity)
                
                _internalState.update {
                    it.copy(
                        mode = CategoryMode.Selection,
                        form = CategoryFormUiState(),
                        query = ""
                    )
                }
            }
        }
    }
}

// Vì chưa dùng Hilt/Koin, ta dùng Factory để Manual DI truyền Repository vào ViewModel
class CategoryViewModelFactory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}