package com.example.nestory.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.nestory.data.filesystem.FileSystemManager
import com.example.nestory.security.VaultUnlockSessionProvider
import com.example.nestory.ui.screens.category.CategoryRoute
import com.example.nestory.ui.screens.home.HomeDashboardScreen
import com.example.nestory.ui.screens.start.StartVaultScreen
import com.example.nestory.ui.screens.unlock.UnlockRoute
import com.example.nestory.ui.screens.unlock.UnlockSuccessScreen
import com.example.nestory.ui.screens.vault.CreateVaultScreen
import com.example.nestory.ui.screens.vault.WaitingScreen

@Composable
fun NestoryApp() {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val unlockSessionManager = remember { VaultUnlockSessionProvider.manager }
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
    var vaultCreationSession by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
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
                onComplete = { destination = NestoryDestination.Unlock },
            )

            NestoryDestination.Unlock -> UnlockRoute(
                onUnlocked = {
                    unlockSessionManager.markUnlocked()
                    destination = NestoryDestination.UnlockSuccess
                },
            )

            NestoryDestination.UnlockSuccess -> UnlockSuccessScreen(
                onLoaded = { destination = NestoryDestination.Home },
            )

            NestoryDestination.Home -> HomeDashboardScreen(
                onOpenAll = { },
                onAddDocument = { },
            )
            NestoryDestination.Category -> CategoryRoute(
                onBack = { destination = NestoryDestination.Home }
            )
        }
    }
}
