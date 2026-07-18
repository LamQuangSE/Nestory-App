package com.example.nestory.ui.screens.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.data.filesystem.FileSystemManager
import com.example.nestory.data.filesystem.VaultCreationError
import com.example.nestory.data.filesystem.VaultCreationStep
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.components.SafeIllustration
import com.example.nestory.ui.components.SecondaryActionButton
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import kotlinx.coroutines.delay

private val vaultCreationSteps = listOf(
    VaultCreationStep.FilesDirectory,
    VaultCreationStep.CacheDirectory,
    VaultCreationStep.Preferences,
    VaultCreationStep.Database
)

@Composable
fun WaitingScreen(
    sessionKey: Int,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val waitingViewModel: WaitingViewModel = viewModel(
        key = "waiting-vault-$sessionKey",
        factory = WaitingViewModelFactory(FileSystemManager(context))
    )
    val uiState by waitingViewModel.uiState.collectAsState()
    var visibleStepCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.phase, uiState.completedSteps, uiState.failedStep) {
        visibleStepCount = 0
        if (uiState.phase == WaitingPhase.Loading) {
            return@LaunchedEffect
        }

        vaultCreationSteps.forEachIndexed { index, _ ->
            delay(160)
            visibleStepCount = index + 1
        }

        if (uiState.phase == WaitingPhase.Success) {
            delay(650)
            onComplete()
        }
    }

    val pulse by rememberInfiniteTransition(label = "WaitingPulse").animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaitingVaultScale"
    )

    val imageRes = when (uiState.phase) {
        WaitingPhase.Loading -> AppImages.VaultLoading
        WaitingPhase.Success -> AppImages.VaultSuccess
        WaitingPhase.Error -> AppImages.VaultError
    }

    NestoryScreen(scrollable = false) {
        Spacer(modifier = Modifier.height(NestorySpacing.S30))
        SafeIllustration(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .scale(if (uiState.phase == WaitingPhase.Loading) pulse else 1f),
            checked = uiState.phase == WaitingPhase.Success,
            imageRes = imageRes
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S20))
        Text(
            text = uiState.title(),
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Heading25Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S10))
        Text(
            text = uiState.description(),
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body15Semi,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S30))
        Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            vaultCreationSteps.forEachIndexed { index, step ->
                AnimatedVisibility(
                    visible = index < visibleStepCount,
                    enter = fadeIn(tween(180)) + slideInVertically(
                        animationSpec = tween(220),
                        initialOffsetY = { it / 3 }
                    )
                ) {
                    VaultCreationStepRow(
                        label = step.label(),
                        iconRes = step.iconRes(uiState),
                        isError = uiState.phase == WaitingPhase.Error && uiState.failedStep == step
                    )
                }
                Spacer(modifier = Modifier.height(NestorySpacing.S17))
            }
        }

        if (uiState.phase == WaitingPhase.Error) {
            Spacer(modifier = Modifier.height(NestorySpacing.S8))
            PrimaryActionButton(
                text = "Thử lại",
                onClick = waitingViewModel::retry
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S10))
            SecondaryActionButton(
                text = "Quay lại",
                onClick = onBack
            )
        }
    }
}

@Composable
private fun VaultCreationStepRow(
    label: String,
    @DrawableRes iconRes: Int,
    isError: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier
                .width(28.dp)
                .height(28.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(NestorySpacing.S14))
        Text(
            text = label,
            color = if (isError) GeneratedColor.FigmaCf1111 else GeneratedColor.Figma000000,
            style = NestoryTextStyles.Body14Medium
        )
    }
}

private fun VaultCreationStep.label(): String = when (this) {
    VaultCreationStep.FilesDirectory -> "Chuẩn bị thư mục lưu trữ"
    VaultCreationStep.CacheDirectory -> "Khởi tạo bộ nhớ tạm"
    VaultCreationStep.Preferences -> "Lưu cấu hình kho"
    VaultCreationStep.Database -> "Khởi tạo cơ sở dữ liệu"
}

@DrawableRes
private fun VaultCreationStep.iconRes(uiState: WaitingUiState): Int = when {
    uiState.failedStep == this -> AppIcons.VaultStepError
    uiState.completedSteps.contains(this) -> AppIcons.NestoryTickCircle
    else -> AppIcons.VaultStepPending
}

private fun WaitingUiState.title(): String = when (phase) {
    WaitingPhase.Loading -> "Đang tạo kho lưu trữ..."
    WaitingPhase.Success -> "Kho lưu trữ đã sẵn sàng"
    WaitingPhase.Error -> "Không thể tạo kho lưu trữ"
}

private fun WaitingUiState.description(): String = when (phase) {
    WaitingPhase.Loading -> "Vui lòng đợi trong giây lát."
    WaitingPhase.Success -> "Các thành phần cần thiết đã được thiết lập."
    WaitingPhase.Error -> errorCode.errorMessage()
}

private fun VaultCreationError?.errorMessage(): String = when (this) {
    VaultCreationError.FilesDirectoryUnavailable -> "Không thể chuẩn bị thư mục lưu trữ."
    VaultCreationError.CacheDirectoryUnavailable -> "Không thể khởi tạo bộ nhớ tạm."
    VaultCreationError.PreferencesWriteFailed -> "Không thể lưu cấu hình kho."
    VaultCreationError.DatabaseOpenFailed -> "Không thể khởi tạo cơ sở dữ liệu."
    VaultCreationError.Unknown, null -> "Đã xảy ra lỗi không xác định. Bạn có thể thử lại."
}
