package com.example.nestory.ui.screen.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.domain.repository.ContainerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

class ContainerViewModel(
    private val containerRepository: ContainerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContainerUiState())
    val uiState: StateFlow<ContainerUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeAllContainers()
    }

    private fun observeAllContainers() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            containerRepository.observeAllContainers()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = getErrorMessage(e)) }
                }
                .collect { containers ->
                    _uiState.update { state ->
                        val selectedId = state.selectedContainerId
                        if (selectedId == null) {
                            state.copy(allContainers = containers, isLoading = false)
                        } else {
                            val stillExists = containers.any { it.id == selectedId }
                            if (stillExists) {
                                state.copy(
                                    allContainers = containers,
                                    containerPath = buildContainerPath(selectedId, containers),
                                    isLoading = false
                                )
                            } else {
                                state.copy(
                                    allContainers = containers,
                                    selectedContainerId = null,
                                    containerPath = emptyList(),
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
        }
    }

    fun toggleContainer(containerId: Long) {
        _uiState.update { state ->
            val newExpandedIds = if (containerId in state.expandedIds) {
                state.expandedIds - containerId
            } else {
                state.expandedIds + containerId
            }
            state.copy(expandedIds = newExpandedIds)
        }
    }

    fun selectContainer(containerId: Long) {
        _uiState.update { state ->
            val path = buildContainerPath(containerId, state.allContainers)
            state.copy(selectedContainerId = containerId, containerPath = path)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedContainerId = null, containerPath = emptyList()) }
    }

    private fun buildContainerPath(containerId: Long, allContainers: List<ContainerEntity>): List<ContainerEntity> {
        val path = mutableListOf<ContainerEntity>()
        var currentId: Long? = containerId
        while (currentId != null) {
            val container = allContainers.find { it.id == currentId } ?: break
            path.add(0, container)
            currentId = container.parentId
        }
        return path
    }

    fun createContainer(name: String, parentId: Long?, onCreated: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            clearError()
            _uiState.update { it.copy(isCreating = true) }
            val container = ContainerEntity(name = name, parentId = parentId)
            containerRepository.createContainer(container).fold(
                onSuccess = { newId ->
                    _uiState.update { it.copy(isCreating = false) }
                    onCreated?.invoke(newId)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = getErrorMessage(error),
                            isCreating = false,
                        )
                    }
                }
            )
        }
    }

    fun updateContainer(container: ContainerEntity) {
        viewModelScope.launch {
            clearError()
            containerRepository.updateContainer(container).fold(
                onSuccess = { },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = getErrorMessage(error)) }
                }
            )
        }
    }

    fun deleteContainer(container: ContainerEntity) {
        viewModelScope.launch {
            clearError()
            containerRepository.deleteContainer(container).fold(
                onSuccess = { },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = getErrorMessage(error)) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun getErrorMessage(e: Throwable): String {
        val message = e.message?.lowercase() ?: ""
        return when {
            e is android.database.sqlite.SQLiteConstraintException ->
                if (message.contains("foreign key")) {
                    "Không thể xóa container đang chứa dữ liệu"
                } else {
                    "Tên container đã tồn tại"
                }
            else -> e.localizedMessage ?: "Đã xảy ra lỗi không mong muốn"
        }
    }
}