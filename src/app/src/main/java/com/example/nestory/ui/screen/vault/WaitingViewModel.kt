package com.example.nestory.ui.screen.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestory.data.filesystem.VaultInitializer
import com.example.nestory.data.filesystem.VaultCreationError
import com.example.nestory.data.filesystem.VaultCreationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WaitingViewModel(
    private val vaultInitializer: VaultInitializer,
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
                vaultInitializer.createVaultStructure()
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
    private val vaultInitializer: VaultInitializer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaitingViewModel::class.java)) {
            return WaitingViewModel(vaultInitializer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
