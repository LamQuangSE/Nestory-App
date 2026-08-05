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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.data.filesystem.FileSystemManager
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.CategoryRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl
import com.example.nestory.ui.components.NestoryBottomBar
import com.example.nestory.ui.screen.category.CategoryRoute
import com.example.nestory.ui.screen.container.ContainerRoute
import com.example.nestory.ui.screen.document.*
import com.example.nestory.ui.screen.home.HomeDashboardScreen
import com.example.nestory.ui.screen.start.StartVaultScreen
import com.example.nestory.ui.screen.unlock.UnlockChoiceScreen
import com.example.nestory.ui.screen.unlock.UnlockFingerprintScreen
import com.example.nestory.ui.screen.unlock.UnlockPinScreen
import com.example.nestory.ui.screen.unlock.UnlockSuccessScreen
import com.example.nestory.ui.screen.vault.CreateVaultScreen
import com.example.nestory.ui.screen.vault.WaitingScreen
import com.example.nestory.ui.screen.ocr.OcrRoute

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
    var ocrReturnDestination by remember { mutableStateOf(NestoryDestination.Home) }
    var vaultCreationSession by remember { mutableIntStateOf(0) }
    var isEditingMode by remember { mutableStateOf(false) }
    var selectedDocumentId by remember { mutableStateOf<String?>(null) }

    // Database and Repositories Setup
    val db = remember { AppDatabase.getDatabase(context) }
    val documentRepository = remember { DocumentRepositoryImpl(db.documentDao()) }
    val containerRepository = remember { ContainerRepositoryImpl(db.containerDao()) }
    val categoryRepository = remember { CategoryRepositoryImpl(db.categoryDao()) }
    
    val documentViewModel: DocumentViewModel = viewModel(
        factory = DocumentViewModelFactory(
            documentRepository,
            containerRepository,
            categoryRepository
        )
    )
    val documentUiState by documentViewModel.uiState.collectAsState()

    // Gom lại logic "mở màn hình Scan" đang bị lặp ở 3 nơi (bottom bar, Home, DocumentSelection)
    val goToScan: () -> Unit = {
        isEditingMode = false
        ocrReturnDestination = destination
        destination = NestoryDestination.Scan
    }

    val showBottomBar =
            when (destination) {
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
                                    onComplete = { destination = NestoryDestination.UnlockChoice },
                            )
                    NestoryDestination.UnlockChoice ->
                            UnlockChoiceScreen(
                                    onFingerprint = {
                                        destination = NestoryDestination.Fingerprint
                                    },
                                    onPin = { destination = NestoryDestination.Pin },
                            )
                    NestoryDestination.Fingerprint ->
                            UnlockFingerprintScreen(
                                    onCancel = { destination = NestoryDestination.UnlockChoice },
                                    onUsePin = { destination = NestoryDestination.Pin },
                                    onUnlocked = { destination = NestoryDestination.UnlockSuccess },
                            )
                    NestoryDestination.Pin ->
                            UnlockPinScreen(
                                    onBack = { destination = NestoryDestination.UnlockChoice },
                                    onForgotPin = { destination = NestoryDestination.UnlockChoice },
                                    onUnlocked = { destination = NestoryDestination.UnlockSuccess },
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
                    NestoryDestination.DocumentSelection ->
                            DocumentSelectionScreen(
                                    uiState = documentUiState,
                                    onAddDocument = goToScan,
                                    onDocumentClick = { id ->
                                        selectedDocumentId = id
                                        destination = NestoryDestination.DocumentDetail
                                    },
                                    onFilterClick = {
                                        destination = NestoryDestination.FilterSelection
                                    },
                                    onSearchQueryChange = { query ->
                                        documentViewModel.onSearchQueryChange(query)
                                    }
                            )
                    NestoryDestination.DocumentDetail -> {
                        val document = documentUiState.documents.find { it.id == selectedDocumentId }
                        if (document != null) {
                            DocumentDetailScreen(
                                document = document,
                                onBack = { destination = NestoryDestination.DocumentSelection },
                                onSave = { name, category, expiryDate, containerId ->
                                    documentViewModel.updateDocument(document.id, name, category, expiryDate, containerId)
                                },
                                onDelete = { id ->
                                    documentViewModel.deleteDocument(id)
                                    destination = NestoryDestination.DocumentSelection
                                }
                            )
                        }
                    }
                    NestoryDestination.FilterSelection ->
                            FilterSelectionScreen(
                                    onBack = { destination = NestoryDestination.DocumentSelection },
                                    onApply = {
                                        destination = NestoryDestination.DocumentSelection
                                    },
                                    onReset = {}
                            )
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
