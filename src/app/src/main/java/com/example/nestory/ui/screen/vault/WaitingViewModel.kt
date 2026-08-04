package com.example.nestory.ui.screen.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.filesystem.FileSystemManager
import com.example.nestory.data.filesystem.VaultCreationError
import com.example.nestory.data.filesystem.VaultCreationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WaitingViewModel(
    private val fileSystemManager: FileSystemManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaitingUiState())
    val uiState: StateFlow<WaitingUiState> = _uiState.asStateFlow()

    init {
        createVault()
    }

    fun retry() {
        createVault()
    }

    private fun createVault() {
        _uiState.value = WaitingUiState(phase = WaitingPhase.Loading)

        viewModelScope.launch {
            val result = runCatching {
                fileSystemManager.createVaultStructure()
            }.getOrElse {
                VaultCreationResult(
                    completedSteps = emptyList(),
                    errorCode = VaultCreationError.Unknown,
                )
            }

            _uiState.value = if (result.isSuccess) {
                WaitingUiState(
                    phase = WaitingPhase.Success,
                    completedSteps = result.completedSteps,
                )
            } else {
                WaitingUiState(
                    phase = WaitingPhase.Error,
                    completedSteps = result.completedSteps,
                    failedStep = result.failedStep,
                    errorCode = result.errorCode ?: VaultCreationError.Unknown,
                )
            }
        }
    }
}

class WaitingViewModelFactory(
    private val fileSystemManager: FileSystemManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaitingViewModel::class.java)) {
            return WaitingViewModel(fileSystemManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
