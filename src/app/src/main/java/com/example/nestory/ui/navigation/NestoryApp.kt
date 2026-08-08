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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.nestory.data.filesystem.FileSystemManager
import com.example.nestory.data.settings.ExpiryReminderSettingsRepository
import com.example.nestory.security.VaultUnlockSessionProvider
import com.example.nestory.ui.components.NestoryBottomBar
import com.example.nestory.ui.screen.category.CategoryRoute
import com.example.nestory.ui.screen.container.ContainerRoute
import com.example.nestory.ui.screen.document.DocumentRoute
import com.example.nestory.ui.screen.home.HomeDashboardScreen
import com.example.nestory.ui.screen.start.StartVaultScreen
import com.example.nestory.ui.screen.unlock.UnlockRoute
import com.example.nestory.ui.screen.unlock.UnlockSuccessScreen
import com.example.nestory.ui.screen.vault.CreateVaultScreen
import com.example.nestory.ui.screen.vault.WaitingScreen
import com.example.nestory.ui.screen.ocr.OcrRoute
import com.example.nestory.ui.screen.setting.ExpiryReminderSettingScreen
import com.example.nestory.ui.screen.setting.SettingScreen
import com.example.nestory.ui.screen.setting.toSettings
import com.example.nestory.ui.screen.setting.toUiState
import com.example.nestory.worker.ExpiryReminderWorker
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.example.nestory.utils.notification.WorkManagerHelper

@Composable
fun NestoryApp() {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val unlockSessionManager = remember { VaultUnlockSessionProvider.manager }
    val settingsRepository = remember { ExpiryReminderSettingsRepository(context) }
    val expiryReminderSettings by settingsRepository.settings.collectAsState(
        initial = com.example.nestory.domain.model.ExpiryReminderSettings(),
    )
    val coroutineScope = rememberCoroutineScope()

    // Theo dõi thay đổi settings để cập nhật lịch nhắc
    LaunchedEffect(expiryReminderSettings) {
        WorkManagerHelper.schedulePeriodicReminder(context, expiryReminderSettings)
    }
    val initialDestination = remember {
        if (FileSystemManager(context).isVaultInitialized()) {
            if (unlockSessionManager.isSessionValid()) {
                NestoryDestination.Home
            } else {
                NestoryDestination.Unlock
            }
        } else {
            NestoryDestination.StartVault
        }
    }
    var destination by remember { mutableStateOf(initialDestination) }
    var ocrReturnDestination by remember { mutableStateOf(NestoryDestination.Home) }
    var vaultCreationSession by remember { mutableIntStateOf(0) }
    var isEditingMode by remember { mutableStateOf(false) }

    // Gom lại logic "mở màn hình Scan" đang bị lặp ở 3 nơi (bottom bar, Home, DocumentSelection)
    val goToScan: () -> Unit = {
        isEditingMode = false
        ocrReturnDestination = destination
        destination = NestoryDestination.Scan
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> unlockSessionManager.onAppBackgrounded()
                Lifecycle.Event.ON_START -> {
                    val isVaultInitialized = FileSystemManager(context).isVaultInitialized()
                    val isSessionValid = unlockSessionManager.onAppForegrounded()
                    if (isVaultInitialized && !isSessionValid) {
                        destination = NestoryDestination.Unlock
                    }
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val showBottomBar =
            when (destination) {
                NestoryDestination.Home,
                NestoryDestination.DocumentSelection,
                NestoryDestination.Category,
                NestoryDestination.Settings -> true
                else -> false
            }

    Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NestoryBottomBar(
                            currentDestination = destination,
                            onNavigate = { destination = it },
                            onScanClick = goToScan
                    )
                }
            }
    ) { innerPadding ->
        AnimatedContent(
                targetState = destination,
                transitionSpec = {
                    val forward = targetState.ordinal >= initialState.ordinal
                    val enterOffset: (Int) -> Int = { width ->
                        if (forward) width / 5 else -width / 5
                    }
                    val exitOffset: (Int) -> Int = { width ->
                        if (forward) -width / 6 else width / 6
                    }

                    (slideInHorizontally(
                                    animationSpec = tween(260),
                                    initialOffsetX = enterOffset,
                            ) + fadeIn(animationSpec = tween(180)))
                            .togetherWith(
                                    slideOutHorizontally(
                                            animationSpec = tween(220),
                                            targetOffsetX = exitOffset,
                                    ) + fadeOut(animationSpec = tween(160))
                            )
                            .using(SizeTransform(clip = false))
                },
                label = "NestoryRouteTransition",
        ) { currentDestination ->
            Box(
                    modifier =
                            Modifier.padding(
                                    bottom =
                                            if (showBottomBar) innerPadding.calculateBottomPadding()
                                            else 0.dp
                            )
            ) {
                when (currentDestination) {
                    NestoryDestination.StartVault ->
                            StartVaultScreen(
                                    onCreateVault = {
                                        destination = NestoryDestination.CreateVault
                                    },
                            )
                    NestoryDestination.CreateVault ->
                            CreateVaultScreen(
                                    onBack = { destination = NestoryDestination.StartVault },
                                    onCreateVault = {
                                        vaultCreationSession += 1
                                        destination = NestoryDestination.Waiting
                                    },
                            )
                    NestoryDestination.Waiting ->
                            WaitingScreen(
                                    sessionKey = vaultCreationSession,
                                    onBack = { destination = NestoryDestination.CreateVault },
                                    onComplete = { destination = NestoryDestination.Unlock },
                            )
                    NestoryDestination.Unlock ->
                            UnlockRoute(
                                    onUnlocked = {
                                        unlockSessionManager.markUnlocked()
                                        destination = NestoryDestination.UnlockSuccess
                                    },
                            )
                    NestoryDestination.UnlockSuccess ->
                            UnlockSuccessScreen(
                                    onLoaded = { destination = NestoryDestination.Home },
                            )
                    NestoryDestination.Home ->
                            HomeDashboardScreen(
                                    onOpenAll = { destination = NestoryDestination.Container },
                                    onAddDocument = goToScan,
                            )
                    NestoryDestination.Category ->
                            CategoryRoute(onBack = { destination = NestoryDestination.Home })
                    NestoryDestination.Container ->
                            ContainerRoute(onBack = { destination = NestoryDestination.Home })
                    NestoryDestination.Settings ->
                            SettingScreen(
                                    onBack = { destination = NestoryDestination.Home },
                                    onExpiryReminderClick = {
                                        destination = NestoryDestination.ExpiryReminderSettings
                                    },
                                    onCategoryClick = { destination = NestoryDestination.Category },
                                    onContainerClick = { destination = NestoryDestination.Container },
                            )
                    NestoryDestination.ExpiryReminderSettings ->
                            ExpiryReminderSettingScreen(
                                    state = expiryReminderSettings.toUiState(),
                                    onStateChange = { uiState ->
                                        coroutineScope.launch {
                                            settingsRepository.updateSettings(uiState.toSettings())
                                        }
                                    },
                                    onBack = { destination = NestoryDestination.Settings },
                            )
                    NestoryDestination.DocumentSelection,
                    NestoryDestination.DocumentDetail,
                    NestoryDestination.FilterSelection ->
                            DocumentRoute(onAddDocument = goToScan)
                    NestoryDestination.Scan ->
                            OcrRoute(
                                    onBack = { destination = ocrReturnDestination },
                                    onSaved = { destination = NestoryDestination.DocumentSelection },
                            )
                }
            }
        }
    }
}
