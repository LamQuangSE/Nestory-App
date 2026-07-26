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
    private var pathJob: Job? = null

    init {
        navigateTo(null)
    }

    fun navigateTo(newParentId: Long?) {
        setCurrentParent(newParentId)
        observeChildren(newParentId)
        loadPathForParentId(newParentId)
    }

    private fun setCurrentParent(parentId: Long?) {
        _uiState.update {
            it.copy(
                parentId = parentId,
                isLoading = true,
                errorMessage = null
            )
        }
    }

    private fun loadPathForParentId(parentId: Long?) {
        pathJob?.cancel()
        pathJob = viewModelScope.launch {
            val path = if (parentId == null) {
                emptyList()
            } else {
                containerRepository.getContainerPath(parentId)
            }
            _uiState.update { it.copy(containerPath = path) }
        }
    }

    private fun observeChildren(parentId: Long?) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            containerRepository.observeChildContainers(parentId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = getErrorMessage(e)) }
                }
                .collect { children ->
                    _uiState.update { it.copy(containerList = children, isLoading = false) }
                }
        }
    }

    fun openContainer(containerId: Long) {
        navigateTo(containerId)
    }

    fun goBack() {
        val currentParentId = _uiState.value.parentId ?: return

        viewModelScope.launch {
            val parent = containerRepository.getContainerById(currentParentId).getOrNull()
            navigateTo(parent?.parentId)
        }
    }

    fun loadContainerPath(containerId: Long) {
        navigateTo(containerId)
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