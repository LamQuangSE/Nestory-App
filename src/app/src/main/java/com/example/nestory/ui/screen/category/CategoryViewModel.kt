package com.example.nestory.ui.screen.category

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.CategoryEntity
import com.example.nestory.domain.model.DocumentCategory
import com.example.nestory.domain.repository.CategoryRepository
import com.example.nestory.ui.theme.categoryColor
import com.example.nestory.ui.theme.predefinedCategoryColor
import com.example.nestory.ui.theme.isPredefinedCategoryName
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

    private val _internalState = MutableStateFlow(CategoryUiState())

    val uiState: StateFlow<CategoryUiState> = combine(
        repository.getAllCategories(),
        _internalState
    ) { entities, state ->
        // 1. Lấy danh sách từ DB
        val dbCategories = entities.map {
            CategoryUiModel(
                id = it.id,
                name = it.name,
                color = predefinedCategoryColor(it.name) ?: Color(it.colorValue.toULong())
            )
        }

        // 2. Chèn 6 mục Default vào nếu trong DB chưa có
        val existingNames = dbCategories.map { it.name.trim().lowercase() }.toSet()
        val missingDefaults = DocumentCategory.entries.mapNotNull { preset ->
            val label = preset.toVietnameseLabel()
            if (label.lowercase() in existingNames) null
            else CategoryUiModel(
                id = "preset_${preset.name}",
                name = label,
                color = preset.categoryColor
            )
        }

        // 3. Phân chia 2 nhóm và Sắp xếp A-Z (2-Tier Sorting)
        val allCategories = dbCategories + missingDefaults
        val defaultGroup = allCategories.filter { isPredefinedCategoryName(it.name) }.sortedBy { it.name }
        val customGroup = allCategories.filterNot { isPredefinedCategoryName(it.name) }.sortedBy { it.name }
        val sortedCategories = defaultGroup + customGroup

        // 4. Logic Bảng màu MỚI (Theo đúng yêu cầu của bạn):
        // Chỉ loại bỏ 6 màu Default. Giữ nguyên các màu User tự tạo để UI không bị thụt lùi.
        val totalColors = defaultCategoryColors()
        val predefinedColors = DocumentCategory.entries.map { it.categoryColor }.toSet()
        val availableColors = totalColors.filterNot { it in predefinedColors }

        state.copy(
            categories = sortedCategories,
            availableColors = availableColors
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryUiState()
    )

    fun ensurePresetCategory(name: String, onReady: (CategoryUiModel) -> Unit) {
        viewModelScope.launch {
            val existing = uiState.value.categories.firstOrNull { it.name.equals(name, ignoreCase = true) }
            if (existing != null && !existing.id.startsWith("preset_")) {
                onReady(existing)
                return@launch
            }
            val color = predefinedCategoryColor(name) ?: defaultCategoryColors().first()
            val newEntity = CategoryEntity(
                id = System.currentTimeMillis().toString(),
                name = name,
                colorValue = color.value.toLong(),
            )
            repository.insertCategory(newEntity)
            onReady(CategoryUiModel(newEntity.id, newEntity.name, color))
        }
    }

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
                        // Focus vào màu khả dụng đầu tiên
                        form = CategoryFormUiState(selectedColor = uiState.value.availableColors.firstOrNull())
                    )
                }
            }
            CategoryEvent.OnEditClick -> {
                val currentState = uiState.value
                val selected = currentState.categories.firstOrNull { it.id == currentState.selectedCategoryId }
                if (selected != null && !isPredefinedCategoryName(selected.name)) {
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
                val target = uiState.value.categories.firstOrNull { it.id == event.categoryId }
                if (target != null && !isPredefinedCategoryName(target.name)) {
                    _internalState.update { it.copy(deleteTargetId = event.categoryId) }
                }
            }
            CategoryEvent.OnDismissDeleteDialog -> {
                _internalState.update { it.copy(deleteTargetId = null) }
            }
            CategoryEvent.OnConfirmDelete -> {
                val deleteId = uiState.value.deleteTargetId
                val target = uiState.value.categories.firstOrNull { it.id == deleteId }
                if (deleteId != null && (target == null || !isPredefinedCategoryName(target.name))) {
                    viewModelScope.launch {
                        repository.deleteCategory(deleteId)
                    }
                    _internalState.update {
                        it.copy(
                            selectedCategoryId = if (it.selectedCategoryId == deleteId) null else it.selectedCategoryId,
                            deleteTargetId = null
                        )
                    }
                } else {
                    _internalState.update { it.copy(deleteTargetId = null) }
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
                // Xử lý logic khi bấm Xác nhận danh mục
            }
        }
    }

    fun submitForm(onCreated: ((CategoryUiModel) -> Unit)? = null) {
        val currentState = _internalState.value
        val categories = uiState.value.categories
        val inputName = currentState.form.name.trim()
        val selectedColor = currentState.form.selectedColor
        val selectedId = currentState.selectedCategoryId

        val isNameDuplicate = categories.any {
            it.name.equals(inputName, ignoreCase = true) &&
                    !(currentState.mode == CategoryMode.Edit && it.id == selectedId)
        }
        
        // Logic validation: Giữ nguyên để báo lỗi khi user cố ý chọn màu đã có người dùng[cite: 21]
        val isColorDuplicate = categories.any {
            it.color == selectedColor &&
                    !(currentState.mode == CategoryMode.Edit && it.id == selectedId)
        }

        val nameError = when {
            inputName.isBlank() -> "Tên danh mục không được để trống"
            isNameDuplicate -> "Tên '$inputName' đã tồn tại"
            else -> null
        }
        val colorError = when {
            selectedColor == null -> "Màu sắc không được để trống"
            isColorDuplicate -> "Màu sắc đã được sử dụng"
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
                    colorValue = selectedColor.value.toLong()
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
                onCreated?.invoke(CategoryUiModel(newEntity.id, newEntity.name, selectedColor))
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

class CategoryViewModelFactory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
