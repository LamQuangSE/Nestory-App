package com.example.nestory.ui.screen.vault

import com.example.nestory.data.filesystem.VaultCreationError
import com.example.nestory.data.filesystem.VaultCreationStep

data class WaitingUiState(
    val phase: WaitingPhase = WaitingPhase.Loading,
    val completedSteps: List<VaultCreationStep> = emptyList(),
    val failedStep: VaultCreationStep? = null,
    val errorCode: VaultCreationError? = null
)

enum class WaitingPhase {
    Loading,
    Success,
    Error
}
