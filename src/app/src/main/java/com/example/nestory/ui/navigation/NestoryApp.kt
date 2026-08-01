package com.example.nestory.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nestory.data.filesystem.FileSystemManager
import com.example.nestory.ui.components.NestoryBottomBar
import com.example.nestory.ui.screens.home.HomeDashboardScreen
import com.example.nestory.ui.screens.start.StartVaultScreen
import com.example.nestory.ui.screens.unlock.UnlockChoiceScreen
import com.example.nestory.ui.screens.unlock.UnlockFingerprintScreen
import com.example.nestory.ui.screens.unlock.UnlockPinScreen
import com.example.nestory.ui.screens.unlock.UnlockSuccessScreen
import com.example.nestory.ui.screens.vault.CreateVaultScreen
import com.example.nestory.ui.screens.vault.WaitingScreen
import com.example.nestory.ui.screens.category.CategoryRoute
import com.example.nestory.ui.screens.container.ContainerRoute
import com.example.nestory.ui.screens.document.DocumentSelectionScreen
import com.example.nestory.ui.screens.document.DocumentDetailScreen
import com.example.nestory.ui.screens.document.FilterSelectionScreen
import com.example.nestory.ui.screens.document.ScanScreen
import com.example.nestory.ui.screens.document.DocumentUiState
import com.example.nestory.ui.screens.document.DocumentUiModel
import com.example.nestory.ui.screens.document.DocumentStatus
import androidx.compose.ui.graphics.Color

@Composable
fun NestoryApp() {
    val context = LocalContext.current.applicationContext
    val initialDestination = remember {
        if (FileSystemManager(context).isVaultInitialized()) {
            NestoryDestination.UnlockChoice
        } else {
            NestoryDestination.StartVault
        }
    }   
    var destination by remember { mutableStateOf(initialDestination) }
    var vaultCreationSession by remember { mutableIntStateOf(0) }
    var isEditingMode by remember { mutableStateOf(false) }

    val showBottomBar = when (destination) {
        NestoryDestination.Home,
        NestoryDestination.DocumentSelection,
        NestoryDestination.Category -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NestoryBottomBar(
                    currentDestination = destination,
                    onNavigate = { destination = it },
                    onScanClick = {
                        isEditingMode = false
                        destination = NestoryDestination.Scan
                    }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = destination,
            transitionSpec = {
                val forward = targetState.ordinal >= initialState.ordinal
                val enterOffset: (Int) -> Int = { width -> if (forward) width / 5 else -width / 5 }
                val exitOffset: (Int) -> Int = { width -> if (forward) -width / 6 else width / 6 }

                (slideInHorizontally(
                    animationSpec = tween(260),
                    initialOffsetX = enterOffset,
                ) + fadeIn(animationSpec = tween(180))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(220),
                        targetOffsetX = exitOffset,
                    ) + fadeOut(animationSpec = tween(160))
                ).using(SizeTransform(clip = false))
            },
            label = "NestoryRouteTransition",
        ) { currentDestination ->
            Box(modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)) {
                when (currentDestination) {
                    NestoryDestination.StartVault -> StartVaultScreen(
                        onCreateVault = { destination = NestoryDestination.CreateVault },
                    )

                    NestoryDestination.CreateVault -> CreateVaultScreen(
                        onBack = { destination = NestoryDestination.StartVault },
                        onCreateVault = {
                            vaultCreationSession += 1
                            destination = NestoryDestination.Waiting
                        },
                    )

                    NestoryDestination.Waiting -> WaitingScreen(
                        sessionKey = vaultCreationSession,
                        onBack = { destination = NestoryDestination.CreateVault },
                        onComplete = { destination = NestoryDestination.UnlockChoice },
                    )

                    NestoryDestination.UnlockChoice -> UnlockChoiceScreen(
                        onFingerprint = { destination = NestoryDestination.Fingerprint },
                        onPin = { destination = NestoryDestination.Pin },
                    )

                    NestoryDestination.Fingerprint -> UnlockFingerprintScreen(
                        onCancel = { destination = NestoryDestination.UnlockChoice },
                        onUsePin = { destination = NestoryDestination.Pin },
                        onUnlocked = { destination = NestoryDestination.UnlockSuccess },
                    )

                    NestoryDestination.Pin -> UnlockPinScreen(
                        onBack = { destination = NestoryDestination.UnlockChoice },
                        onForgotPin = { destination = NestoryDestination.UnlockChoice },
                        onUnlocked = { destination = NestoryDestination.UnlockSuccess },
                    )

                    NestoryDestination.UnlockSuccess -> UnlockSuccessScreen(
                        onLoaded = { destination = NestoryDestination.Home },
                    )

            NestoryDestination.Home -> HomeDashboardScreen(
                onOpenAll = { destination = NestoryDestination.Container },
                onOpenCategory = { destination = NestoryDestination.Category },
                onOpenContainer = { destination = NestoryDestination.Container },
                onAddDocument = { },
            )
            NestoryDestination.Category -> CategoryRoute(
                onBack = { destination = NestoryDestination.Home }
            )
            NestoryDestination.Container -> ContainerRoute(
                onBack = { destination = NestoryDestination.Home }
            )
                    NestoryDestination.Home -> HomeDashboardScreen(
                        onOpenAll = { },
                        onAddDocument = { 
                            isEditingMode = false
                            destination = NestoryDestination.Scan 
                        }
                    )
                    NestoryDestination.Category -> CategoryRoute(
                        onBack = { destination = NestoryDestination.Home }
                    )
                    NestoryDestination.DocumentSelection -> DocumentSelectionScreen(
                        uiState = DocumentUiState(
                            documents = listOf(
                                DocumentUiModel(
                                    id = "1",
                                    name = "Hợp đồng thuê nhà 2026",
                                    category = "Hợp đồng, Pháp lý",
                                    containerPath = "Tủ tài liệu > Ngăn 4",
                                    status = DocumentStatus.Active,
                                    expiryDate = "20/08/2026",
                                    categoryColor = Color(0xFF1855EE)
                                )
                            )
                        ),
                        onAddDocument = { 
                            isEditingMode = false
                            destination = NestoryDestination.Scan 
                        },
                        onDocumentClick = { destination = NestoryDestination.DocumentDetail },
                        onFilterClick = { destination = NestoryDestination.FilterSelection }
                    )
                    NestoryDestination.DocumentDetail -> DocumentDetailScreen(
                        document = DocumentUiModel(
                            id = "1",
                            name = "Hợp đồng thuê nhà 2026",
                            category = "Hợp đồng, Pháp lý",
                            containerPath = "Tủ tài liệu > Ngăn 4",
                            status = DocumentStatus.Active,
                            expiryDate = "20/08/2026",
                            categoryColor = Color(0xFF1855EE)
                        ),
                        onBack = { destination = NestoryDestination.DocumentSelection },
                        onDelete = { destination = NestoryDestination.DocumentSelection }
                    )
                    NestoryDestination.FilterSelection -> FilterSelectionScreen(
                        onBack = { destination = NestoryDestination.DocumentSelection },
                        onApply = { destination = NestoryDestination.DocumentSelection },
                        onReset = { }
                    )
                    NestoryDestination.Scan -> ScanScreen(
                        onClose = { destination = NestoryDestination.DocumentSelection }
                    )
                }
            }
        }
    }
}
