package com.example.nestory.ui.screen.vault

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import com.example.nestory.data.filesystem.VaultCreationError
import com.example.nestory.data.filesystem.VaultCreationResult
import com.example.nestory.data.filesystem.VaultCreationStep
import com.example.nestory.data.filesystem.VaultInitializer
import com.example.nestory.ui.theme.NestoryTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VaultCreationSessionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun systemBackOnCreateVault_matchesBackButton() {
        composeRule.setContent {
            NestoryTheme {
                CreateVaultBackTestHost()
            }
        }

        composeRule.waitUntilTextExists("Tạo kho lưu trữ cục bộ")

        pressBack()

        composeRule.waitUntilTextExists("Start vault placeholder")
    }

    @Test
    fun createVaultAgainAfterFailure_startsNewWaitingSession() {
        val vaultInitializer = SequenceVaultInitializer(
            VaultCreationResult(
                completedSteps = emptyList(),
                failedStep = VaultCreationStep.FilesDirectory,
                errorCode = VaultCreationError.FilesDirectoryUnavailable,
            ),
            VaultCreationResult(
                completedSteps = listOf(
                    VaultCreationStep.FilesDirectory,
                    VaultCreationStep.CacheDirectory,
                    VaultCreationStep.Preferences,
                    VaultCreationStep.Database,
                ),
            ),
        )

        composeRule.setContent {
            NestoryTheme {
                VaultCreationTestHost(vaultInitializer = vaultInitializer)
            }
        }

        composeRule.clickCreateVault()
        composeRule.waitUntilTextExists("Không thể tạo kho lưu trữ")

        composeRule.onNodeWithText("Quay lại").performClick()
        composeRule.clickCreateVault()

        composeRule.waitUntilTextExists("Kho lưu trữ đã sẵn sàng")
        assertTrue(
            composeRule.onAllNodesWithText("Không thể tạo kho lưu trữ")
                .fetchSemanticsNodes()
                .isEmpty(),
        )
        assertEquals(2, vaultInitializer.callCount)
    }

    @Test
    fun goBackAfterVaultCreationFailure_returnsToCreateVaultScreen() {
        val vaultInitializer = SequenceVaultInitializer(
            VaultCreationResult(
                completedSteps = emptyList(),
                failedStep = VaultCreationStep.FilesDirectory,
                errorCode = VaultCreationError.FilesDirectoryUnavailable,
            ),
        )

        composeRule.setContent {
            NestoryTheme {
                VaultCreationTestHost(vaultInitializer = vaultInitializer)
            }
        }

        composeRule.clickCreateVault()
        composeRule.waitUntilTextExists("Không thể tạo kho lưu trữ")

        composeRule.onNodeWithText("Quay lại").performClick()

        composeRule.waitUntilTextExists("Tạo kho lưu trữ cục bộ")
        assertEquals(1, vaultInitializer.callCount)
    }

    @Test
    fun systemBackAfterVaultCreationFailure_returnsToCreateVaultScreen() {
        val vaultInitializer = SequenceVaultInitializer(
            VaultCreationResult(
                completedSteps = emptyList(),
                failedStep = VaultCreationStep.FilesDirectory,
                errorCode = VaultCreationError.FilesDirectoryUnavailable,
            ),
        )

        composeRule.setContent {
            NestoryTheme {
                VaultCreationTestHost(vaultInitializer = vaultInitializer)
            }
        }

        composeRule.clickCreateVault()
        composeRule.waitUntilTextExists("Không thể tạo kho lưu trữ")

        pressBack()

        composeRule.waitUntilTextExists("Tạo kho lưu trữ cục bộ")
        assertEquals(1, vaultInitializer.callCount)
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.clickCreateVault() {
        onAllNodesWithText("Tạo kho lưu trữ")
            .filter(hasClickAction())
            .onFirst()
            .performClick()
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.waitUntilTextExists(
        text: String,
    ) {
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

@Composable
private fun CreateVaultBackTestHost() {
    var destination by remember { mutableStateOf(TestDestination.CreateVault) }

    when (destination) {
        TestDestination.CreateVault -> CreateVaultScreen(
            onBack = { destination = TestDestination.StartVault },
            onCreateVault = {},
        )
        TestDestination.StartVault -> Text("Start vault placeholder")
        else -> Text("Unused")
    }
}

@Composable
private fun VaultCreationTestHost(
    vaultInitializer: VaultInitializer,
) {
    var destination by remember { mutableStateOf(TestDestination.CreateVault) }
    var sessionKey by remember { mutableIntStateOf(0) }

    when (destination) {
        TestDestination.StartVault -> Text("Start vault placeholder")

        TestDestination.CreateVault -> CreateVaultScreen(
            onBack = {},
            onCreateVault = {
                sessionKey += 1
                destination = TestDestination.Waiting
            },
        )

        TestDestination.Waiting -> WaitingScreen(
            sessionKey = sessionKey,
            onBack = { destination = TestDestination.CreateVault },
            onComplete = { destination = TestDestination.Complete },
            vaultInitializerProvider = { _: Context -> vaultInitializer },
        )

        TestDestination.Complete -> Text("Vault creation complete")
    }
}

private enum class TestDestination {
    StartVault,
    CreateVault,
    Waiting,
    Complete,
}

private class SequenceVaultInitializer(
    private vararg val results: VaultCreationResult,
) : VaultInitializer {
    var callCount: Int = 0
        private set

    override suspend fun createVaultStructure(): VaultCreationResult {
        val result = results.getOrElse(callCount) { results.last() }
        callCount += 1
        return result
    }
}
