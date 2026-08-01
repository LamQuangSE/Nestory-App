package com.example.nestory.ui.screens.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.entity.ContainerEntity
import com.example.nestory.data.repository.ContainerRepository
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
                    _uiState.update { it.copy(allContainers = containers, isLoading = false) }
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

    fun createContainer(name: String, parentId: Long?) {
        viewModelScope.launch {
            val container = ContainerEntity(name = name, parentId = parentId)
            containerRepository.createContainer(container).fold(
                onSuccess = { },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = getErrorMessage(error)) }
                }
            )
        }
    }

    fun updateContainer(container: ContainerEntity) {
        viewModelScope.launch {
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
            containerRepository.deleteContainer(container).fold(
                onSuccess = { },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = getErrorMessage(error)) }
                }
            )
        }
    }

    private fun getErrorMessage(e: Throwable): String {
        return when (e) {
            is android.database.sqlite.SQLiteConstraintException -> "Folder name already exists"
            else -> e.localizedMessage ?: "An unexpected error occurred"
        }
    }
}