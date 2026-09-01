package com.example.nestory.ui.screen.vault

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.filesystem.VaultCreationError
import com.example.nestory.data.filesystem.VaultCreationResult
import com.example.nestory.data.filesystem.VaultCreationStep
import com.example.nestory.data.filesystem.VaultInitializer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WaitingViewModelFailureTest {
    @Test
    fun filesDirectoryUnavailable_setsFilesDirectoryErrorState() = runBlocking {
        val viewModel = WaitingViewModel(
            StaticVaultInitializer(
                VaultCreationResult(
                    completedSteps = emptyList(),
                    failedStep = VaultCreationStep.FilesDirectory,
                    errorCode = VaultCreationError.FilesDirectoryUnavailable,
                ),
            ),
        )

        val state = viewModel.waitForPhase(WaitingPhase.Error)

        assertEquals(VaultCreationStep.FilesDirectory, state.failedStep)
        assertEquals(VaultCreationError.FilesDirectoryUnavailable, state.errorCode)
    }

    @Test
    fun cacheDirectoryUnavailable_setsCacheDirectoryErrorState() = runBlocking {
        val viewModel = WaitingViewModel(
            StaticVaultInitializer(
                VaultCreationResult(
                    completedSteps = listOf(VaultCreationStep.FilesDirectory),
                    failedStep = VaultCreationStep.CacheDirectory,
                    errorCode = VaultCreationError.CacheDirectoryUnavailable,
                ),
            ),
        )

        val state = viewModel.waitForPhase(WaitingPhase.Error)

        assertEquals(listOf(VaultCreationStep.FilesDirectory), state.completedSteps)
        assertEquals(VaultCreationStep.CacheDirectory, state.failedStep)
        assertEquals(VaultCreationError.CacheDirectoryUnavailable, state.errorCode)
    }

    @Test
    fun preferencesWriteFailed_setsPreferencesErrorState() = runBlocking {
        val viewModel = WaitingViewModel(
            StaticVaultInitializer(
                VaultCreationResult(
                    completedSteps = listOf(
                        VaultCreationStep.FilesDirectory,
                        VaultCreationStep.CacheDirectory,
                    ),
                    failedStep = VaultCreationStep.Preferences,
                    errorCode = VaultCreationError.PreferencesWriteFailed,
                ),
            ),
        )

        val state = viewModel.waitForPhase(WaitingPhase.Error)

        assertEquals(VaultCreationStep.Preferences, state.failedStep)
        assertEquals(VaultCreationError.PreferencesWriteFailed, state.errorCode)
    }

    @Test
    fun databaseOpenFailed_setsDatabaseErrorState() = runBlocking {
        val viewModel = WaitingViewModel(
            StaticVaultInitializer(
                VaultCreationResult(
                    completedSteps = listOf(
                        VaultCreationStep.FilesDirectory,
                        VaultCreationStep.CacheDirectory,
                        VaultCreationStep.Preferences,
                    ),
                    failedStep = VaultCreationStep.Database,
                    errorCode = VaultCreationError.DatabaseOpenFailed,
                ),
            ),
        )

        val state = viewModel.waitForPhase(WaitingPhase.Error)

        assertEquals(VaultCreationStep.Database, state.failedStep)
        assertEquals(VaultCreationError.DatabaseOpenFailed, state.errorCode)
    }

    @Test
    fun retryAfterFailure_returnsToLoadingAndRunsAgain() = runBlocking {
        val retryResult = CompletableDeferred<VaultCreationResult>()
        val vaultInitializer = RetryVaultInitializer(
            firstResult = VaultCreationResult(
                completedSteps = emptyList(),
                failedStep = VaultCreationStep.FilesDirectory,
                errorCode = VaultCreationError.FilesDirectoryUnavailable,
            ),
            secondResult = retryResult,
        )
        val viewModel = WaitingViewModel(
            vaultInitializer,
        )
        viewModel.waitForPhase(WaitingPhase.Error)

        viewModel.retry()

        assertEquals(WaitingPhase.Loading, viewModel.uiState.value.phase)

        retryResult.complete(
            VaultCreationResult(
                completedSteps = listOf(
                    VaultCreationStep.FilesDirectory,
                    VaultCreationStep.CacheDirectory,
                    VaultCreationStep.Preferences,
                    VaultCreationStep.Database,
                ),
            ),
        )
        val state = viewModel.waitForPhase(WaitingPhase.Success)

        assertEquals(2, vaultInitializer.callCount)
        assertEquals(4, state.completedSteps.size)
    }

    @Test
    fun unexpectedException_mapsToUnknownErrorState() = runBlocking {
        val viewModel = WaitingViewModel(
            ThrowingVaultInitializer,
        )

        val state = viewModel.waitForPhase(WaitingPhase.Error)

        assertEquals(VaultCreationError.Unknown, state.errorCode)
        assertEquals(null, state.failedStep)
    }

    @Test
    fun databaseFailureAfterPreferences_keepsPriorStepsAndDoesNotMarkVaultInitialized() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("nestory_vault_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val viewModel = WaitingViewModel(
            StaticVaultInitializer(
                VaultCreationResult(
                    completedSteps = listOf(
                        VaultCreationStep.FilesDirectory,
                        VaultCreationStep.CacheDirectory,
                        VaultCreationStep.Preferences,
                    ),
                    failedStep = VaultCreationStep.Database,
                    errorCode = VaultCreationError.DatabaseOpenFailed,
                ),
            ),
        )

        val state = viewModel.waitForPhase(WaitingPhase.Error)

        assertEquals(
            listOf(
                VaultCreationStep.FilesDirectory,
                VaultCreationStep.CacheDirectory,
                VaultCreationStep.Preferences,
            ),
            state.completedSteps,
        )
        assertEquals(VaultCreationStep.Database, state.failedStep)
        assertEquals(VaultCreationError.DatabaseOpenFailed, state.errorCode)
        assertFalse(prefs.getBoolean("vault_initialized", false))
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

private class StaticVaultInitializer(
    private val result: VaultCreationResult,
) : VaultInitializer {
    override suspend fun createVaultStructure(): VaultCreationResult = result
}

private class RetryVaultInitializer(
    private val firstResult: VaultCreationResult,
    private val secondResult: CompletableDeferred<VaultCreationResult>,
) : VaultInitializer {
    var callCount = 0
        private set

    override suspend fun createVaultStructure(): VaultCreationResult {
        callCount += 1
        return if (callCount == 1) firstResult else secondResult.await()
    }
}

private object ThrowingVaultInitializer : VaultInitializer {
    override suspend fun createVaultStructure(): VaultCreationResult {
        error("Unexpected vault creation failure")
    }
}
