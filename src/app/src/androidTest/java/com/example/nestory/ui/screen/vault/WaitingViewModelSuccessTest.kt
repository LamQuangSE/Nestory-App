package com.example.nestory.ui.screen.vault

import com.example.nestory.data.filesystem.VaultCreationResult
import com.example.nestory.data.filesystem.VaultCreationStep
import com.example.nestory.data.filesystem.VaultInitializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WaitingViewModelSuccessTest {
    @Test
    fun vaultCreationSuccess_reachesSuccessPhaseWithAllSteps() = runBlocking {
        val viewModel = WaitingViewModel(
            SuccessVaultInitializer(
                VaultCreationResult(
                    completedSteps = listOf(
                        VaultCreationStep.FilesDirectory,
                        VaultCreationStep.CacheDirectory,
                        VaultCreationStep.Preferences,
                        VaultCreationStep.Database,
                    ),
                ),
            ),
        )

        val state = viewModel.waitForPhase(WaitingPhase.Success)

        assertEquals(WaitingPhase.Success, state.phase)
        assertEquals(4, state.completedSteps.size)
        assertNull(state.failedStep)
        assertNull(state.errorCode)
    }

    private suspend fun WaitingViewModel.waitForPhase(phase: WaitingPhase): WaitingUiState {
        repeat(100) {
            val state = uiState.value
            if (state.phase == phase) return state
            delay(50)
        }
        error("Timed out waiting for $phase. Last state=${uiState.value}")
    }
}

private class SuccessVaultInitializer(
    private val result: VaultCreationResult,
) : VaultInitializer {
    override suspend fun createVaultStructure(): VaultCreationResult = result
}