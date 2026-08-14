package com.example.nestory.ui.navigation

import androidx.activity.compose.BackHandler
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
import com.example.nestory.ui.screen.documentkit.DocumentKitRoute
import com.example.nestory.ui.screen.home.HomeDashboardRoute
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
import com.example.nestory.utils.notification.WorkManagerHelper
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

@Composable
fun NestoryApp(initialDocumentId: String? = null) {
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
                if (initialDocumentId != null) NestoryDestination.DocumentDetail else NestoryDestination.Home
            } else {
                NestoryDestination.Unlock
            }
        } else {
            NestoryDestination.StartVault
        }
    }
    var destination by remember { mutableStateOf(initialDestination) }
    var pendingDocumentId by remember { mutableStateOf(initialDocumentId) }
    var ocrReturnDestination by remember { mutableStateOf(NestoryDestination.Home) }
    var pendingKitLinkItemId by remember { mutableStateOf<Long?>(null) }
    var vaultCreationSession by remember { mutableIntStateOf(0) }
    var isEditingMode by remember { mutableStateOf(false) }

    // Edit-leave guard: while editing (Kit/Item/Document), any navigation away via the
    // bottom bar (tab or Scan) must first run the edit confirmation. The request is passed
    // down into the edit screen, which shows the dialog. Yes -> completeEditLeave, No -> dismissEditLeave.
    var editLeaveRequested by remember { mutableStateOf(false) }
    var pendingLeaveDestination by remember { mutableStateOf<NestoryDestination?>(null) }
    var pendingScanLinkItemId by remember { mutableStateOf<Long?>(null) }

    val dismissEditLeave: () -> Unit = {
        editLeaveRequested = false
        pendingLeaveDestination = null
        pendingScanLinkItemId = null
    }

    val completeEditLeave: () -> Unit = {
        editLeaveRequested = false
        val target = pendingLeaveDestination
        val scanItemId = pendingScanLinkItemId
        pendingLeaveDestination = null
        pendingScanLinkItemId = null
        isEditingMode = false
        if (target == NestoryDestination.Scan) {
            ocrReturnDestination = destination
            pendingKitLinkItemId = scanItemId
            destination = NestoryDestination.Scan
        } else if (target != null) {
            destination = target
        }
    }

    val onEditModeChange: (Boolean) -> Unit = { editing ->
        isEditingMode = editing
    }

    // Gom lại logic "mở màn hình Scan" đang bị lặp ở 3 nơi (bottom bar, Home, DocumentSelection)
    val goToScan: () -> Unit = {
        if (isEditingMode) {
            pendingLeaveDestination = NestoryDestination.Scan
            pendingScanLinkItemId = null
            editLeaveRequested = true
        } else {
            isEditingMode = false
            ocrReturnDestination = destination
            pendingKitLinkItemId = null
            destination = NestoryDestination.Scan
        }
    }

    val goToScanForKitLink: (Long?) -> Unit = { itemId ->
        if (isEditingMode) {
            pendingLeaveDestination = NestoryDestination.Scan
            pendingScanLinkItemId = itemId
            editLeaveRequested = true
        } else {
            isEditingMode = false
            ocrReturnDestination = destination
            pendingKitLinkItemId = itemId
            destination = NestoryDestination.Scan
        }
    }

    val onBottomNavNavigate: (NestoryDestination) -> Unit = { target ->
        if (isEditingMode) {
            pendingLeaveDestination = target
            pendingScanLinkItemId = null
            editLeaveRequested = true
        } else {
            destination = target
        }
    }

    BackHandler(
        enabled = when (destination) {
            NestoryDestination.Category,
            NestoryDestination.Container,
            NestoryDestination.Settings,
            NestoryDestination.ExpiryReminderSettings,
            NestoryDestination.DocumentSelection,
            NestoryDestination.DocumentKit -> true
            else -> false
        },
    ) {
        if (isEditingMode) {
            pendingLeaveDestination = NestoryDestination.Home
            pendingScanLinkItemId = null
            editLeaveRequested = true
        } else {
            destination = when (destination) {
                NestoryDestination.ExpiryReminderSettings -> NestoryDestination.Settings
                else -> NestoryDestination.Home
            }
        }
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
                NestoryDestination.DocumentDetail,
                NestoryDestination.DocumentKit,
                NestoryDestination.Category,
                NestoryDestination.Settings -> true
                else -> false
            }

    Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NestoryBottomBar(
                            currentDestination = destination,
                            onNavigate = onBottomNavNavigate,
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
                                    onLoaded = {
                                        destination = if (pendingDocumentId != null) {
                                            NestoryDestination.DocumentDetail
                                        } else {
                                            NestoryDestination.Home
                                        }
                                    },
                            )
                    NestoryDestination.Home ->
                            HomeDashboardRoute(
                                    onOpenDocuments = {
                                        destination = NestoryDestination.DocumentSelection
                                    },
                                    onOpenDocumentKits = {
                                        destination = NestoryDestination.DocumentKit
                                    },
                                    onAddDocument = goToScan,
                                    onRecentDocumentClick = { documentId ->
                                        pendingDocumentId = documentId.toString()
                                        destination = NestoryDestination.DocumentDetail
                                    },
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
                    NestoryDestination.DocumentSelection ->
                            DocumentRoute(
                                onAddDocument = goToScan,
                                initialDocumentId = pendingDocumentId,
                                onClearInitialId = { pendingDocumentId = null },
                                editLeaveRequested = editLeaveRequested,
                                onEditLeaveComplete = completeEditLeave,
                                onEditLeaveDismiss = dismissEditLeave,
                                onEditModeChange = onEditModeChange,
                            )
                    NestoryDestination.DocumentKit ->
                            DocumentKitRoute(
                                onBack = { destination = NestoryDestination.Home },
                                onScanDocument = goToScanForKitLink,
                                editLeaveRequested = editLeaveRequested,
                                onEditLeaveComplete = completeEditLeave,
                                onEditLeaveDismiss = dismissEditLeave,
                                onEditModeChange = onEditModeChange,
                            )
                    NestoryDestination.DocumentDetail ->
                            DocumentRoute(
                                onAddDocument = goToScan,
                                initialDocumentId = pendingDocumentId,
                                onClearInitialId = { pendingDocumentId = null },
                                editLeaveRequested = editLeaveRequested,
                                onEditLeaveComplete = completeEditLeave,
                                onEditLeaveDismiss = dismissEditLeave,
                                onEditModeChange = onEditModeChange,
                            )
                    NestoryDestination.FilterSelection ->
                            DocumentRoute(
                                onAddDocument = goToScan,
                                initialDocumentId = pendingDocumentId,
                                onClearInitialId = { pendingDocumentId = null },
                                editLeaveRequested = editLeaveRequested,
                                onEditLeaveComplete = completeEditLeave,
                                onEditLeaveDismiss = dismissEditLeave,
                                onEditModeChange = onEditModeChange,
                            )
                    NestoryDestination.Scan ->
                            OcrRoute(
                                    onBack = { destination = ocrReturnDestination },
                                    onSaved = {
                                        if (pendingKitLinkItemId != null) {
                                            pendingKitLinkItemId = null
                                            destination = ocrReturnDestination
                                        } else {
                                            destination = NestoryDestination.DocumentSelection
                                        }
                                    },
                                    linkToItemId = pendingKitLinkItemId,
                            )
                }
            }
        }
    }
}
