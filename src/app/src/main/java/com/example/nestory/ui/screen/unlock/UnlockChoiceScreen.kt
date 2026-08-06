package com.example.nestory.ui.screen.unlock

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryLogo
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.components.SafeIllustration
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

private const val FINGERPRINT_AUTHENTICATORS = BIOMETRIC_STRONG
private const val DEVICE_PASSWORD_AUTHENTICATORS = DEVICE_CREDENTIAL
private const val SETUP_AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

@Composable
fun UnlockRoute(
    onUnlocked: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findFragmentActivity() }
    val currentOnUnlocked by rememberUpdatedState(onUnlocked)
    var uiState by remember { mutableStateOf(UnlockUiState()) }

    fun refreshAvailability() {
        uiState = uiState.copy(
            fingerprintAvailability = context.unlockAvailability(FINGERPRINT_AUTHENTICATORS),
            devicePasswordAvailability = context.unlockAvailability(DEVICE_PASSWORD_AUTHENTICATORS),
            authenticating = null,
            errorMessage = null,
        )
    }

    LaunchedEffect(context) {
        refreshAvailability()
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshAvailability()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val biometricPrompt = remember(activity) {
        activity?.let { fragmentActivity ->
            BiometricPrompt(
                fragmentActivity,
                ContextCompat.getMainExecutor(fragmentActivity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult,
                    ) {
                        uiState = uiState.copy(authenticating = null, errorMessage = null)
                        currentOnUnlocked()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        uiState = uiState.copy(
                            authenticating = null,
                            errorMessage = errorCode.toUnlockErrorMessage(errString),
                        )
                    }

                    override fun onAuthenticationFailed() {
                        uiState = uiState.copy(
                            authenticating = null,
                            errorMessage = "Không thể xác thực. Vui lòng thử lại.",
                        )
                    }
                },
            )
        }
    }

    val fingerprintPromptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mở khóa bằng vân tay")
            .setSubtitle("Chạm cảm biến vân tay để truy cập kho lưu trữ")
            .setNegativeButtonText("Hủy")
            .setAllowedAuthenticators(FINGERPRINT_AUTHENTICATORS)
            .build()
    }

    val devicePasswordPromptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mở khóa bằng khóa màn hình thiết bị")
            .setSubtitle("Nhập mã khóa, PIN hoặc hình mở khóa của thiết bị")
            .setAllowedAuthenticators(DEVICE_PASSWORD_AUTHENTICATORS)
            .build()
    }

    fun authenticateWith(promptInfo: BiometricPrompt.PromptInfo, method: UnlockMethod) {
        if (biometricPrompt == null) {
            uiState = uiState.copy(
                errorMessage = "Không thể mở xác thực hệ thống trên màn hình hiện tại.",
            )
            return
        }

        uiState = uiState.copy(authenticating = method, errorMessage = null)
        runCatching {
            biometricPrompt.authenticate(promptInfo)
        }.onFailure { error ->
            uiState = uiState.copy(
                authenticating = null,
                errorMessage = error.message ?: "Không thể mở xác thực hệ thống.",
            )
        }
    }

    UnlockChoiceScreen(
        uiState = uiState,
        onFingerprintAction = {
            when (uiState.fingerprintAvailability) {
                UnlockAvailability.Available -> authenticateWith(
                    promptInfo = fingerprintPromptInfo,
                    method = UnlockMethod.Fingerprint,
                )

                UnlockAvailability.NotEnrolled -> context.openSecuritySettings()
                UnlockAvailability.Checking,
                UnlockAvailability.Unavailable -> refreshAvailability()
            }
        },
        onDevicePasswordAction = {
            when (uiState.devicePasswordAvailability) {
                UnlockAvailability.Available -> authenticateWith(
                    promptInfo = devicePasswordPromptInfo,
                    method = UnlockMethod.DevicePassword,
                )

                UnlockAvailability.NotEnrolled -> context.openSecuritySettings()
                UnlockAvailability.Checking,
                UnlockAvailability.Unavailable -> refreshAvailability()
            }
        },
    )
}

@Composable
fun UnlockChoiceScreen(
    uiState: UnlockUiState,
    onFingerprintAction: () -> Unit,
    onDevicePasswordAction: () -> Unit,
) {
    val statusMessage = uiState.statusMessage

    NestoryScreen {
        Spacer(modifier = Modifier.height(NestorySpacing.S40))
        NestoryLogo(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            centered = true,
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S30))
        Text(
            text = "Mở khoá kho lưu trữ",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Title21Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        Text(
            text = uiState.description,
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma919191,
            style = NestoryTextStyles.Body18Semi,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        SafeIllustration(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            compact = false,
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S24))
        PrimaryActionButton(
            text = uiState.fingerprintActionLabel,
            leadingIcon = AppIcons.FigmaFingerprint,
            enabled = uiState.isFingerprintActionEnabled,
            onClick = onFingerprintAction,
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S17))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(GeneratedColor.FigmaE5e7eb),
            )
            Text(
                text = "hoặc",
                modifier = Modifier.width(NestorySpacing.S75),
                color = GeneratedColor.Figma717171,
                style = NestoryTextStyles.Body17Medium,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(GeneratedColor.FigmaE5e7eb),
            )
        }
        Spacer(modifier = Modifier.height(NestorySpacing.S17))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(
                    enabled = uiState.isDevicePasswordActionEnabled,
                    onClick = onDevicePasswordAction,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.devicePasswordActionLabel,
                color = if (uiState.isDevicePasswordActionEnabled) {
                    GeneratedColor.Figma1a60e2
                } else {
                    GeneratedColor.Figma919191
                },
                style = NestoryTextStyles.Body17Medium,
                textAlign = TextAlign.Center,
            )
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(NestorySpacing.S17))
            Text(
                text = statusMessage,
                modifier = Modifier.fillMaxWidth(),
                color = uiState.statusColor,
                style = NestoryTextStyles.Body17Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

data class UnlockUiState(
    val fingerprintAvailability: UnlockAvailability = UnlockAvailability.Checking,
    val devicePasswordAvailability: UnlockAvailability = UnlockAvailability.Checking,
    val authenticating: UnlockMethod? = null,
    val errorMessage: String? = null,
) {
    val description: String
        get() = when {
            fingerprintAvailability == UnlockAvailability.Checking ||
                devicePasswordAvailability == UnlockAvailability.Checking ->
                "Đang kiểm tra phương thức xác thực của thiết bị."

            fingerprintAvailability == UnlockAvailability.Available ||
                devicePasswordAvailability == UnlockAvailability.Available ->
                "Chọn vân tay hoặc khóa màn hình thiết bị để truy cập giấy tờ của bạn."

            fingerprintAvailability == UnlockAvailability.NotEnrolled ||
                devicePasswordAvailability == UnlockAvailability.NotEnrolled ->
                "Thiết bị cần cài khóa màn hình hoặc sinh trắc học trước khi mở kho."

            else -> "Thiết bị hiện chưa sẵn sàng cho xác thực hệ thống."
        }

    val fingerprintActionLabel: String
        get() = when {
            authenticating == UnlockMethod.Fingerprint -> "Đang mở vân tay..."
            else -> "Mở khóa bằng vân tay"
        }

    val devicePasswordActionLabel: String
        get() = when {
            authenticating == UnlockMethod.DevicePassword -> "Đang mở khóa thiết bị..."
            else -> "Sử dụng khóa màn hình thiết bị"
        }

    val isFingerprintActionEnabled: Boolean
        get() = authenticating == null && fingerprintAvailability == UnlockAvailability.Available

    val isDevicePasswordActionEnabled: Boolean
        get() = authenticating == null && devicePasswordAvailability == UnlockAvailability.Available

    val statusMessage: String?
        get() = errorMessage ?: when {
            fingerprintAvailability == UnlockAvailability.Checking ||
                devicePasswordAvailability == UnlockAvailability.Checking -> null

            fingerprintAvailability == UnlockAvailability.Available &&
                devicePasswordAvailability == UnlockAvailability.Available -> null

            fingerprintAvailability == UnlockAvailability.Available ->
                "Khóa màn hình thiết bị chưa sẵn sàng. Bạn vẫn có thể mở khóa bằng vân tay."

            devicePasswordAvailability == UnlockAvailability.Available ->
                "Vân tay chưa sẵn sàng. Bạn vẫn có thể mở khóa bằng khóa màn hình thiết bị."

            fingerprintAvailability == UnlockAvailability.NotEnrolled ||
                devicePasswordAvailability == UnlockAvailability.NotEnrolled ->
                "Sau khi thiết lập trong Cài đặt, quay lại Nestory để mở khóa."

            else -> "Hãy thử lại sau hoặc kiểm tra cài đặt bảo mật của thiết bị."
        }

    val statusColor
        get() = if (errorMessage == null) {
            GeneratedColor.Figma717171
        } else {
            GeneratedColor.FigmaCf1111
        }
}

enum class UnlockAvailability {
    Checking,
    Available,
    NotEnrolled,
    Unavailable,
}

enum class UnlockMethod {
    Fingerprint,
    DevicePassword,
}

private fun Context.unlockAvailability(authenticators: Int): UnlockAvailability {
    val status = BiometricManager.from(this).canAuthenticate(authenticators)
    return when (status) {
        BiometricManager.BIOMETRIC_SUCCESS -> UnlockAvailability.Available
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> UnlockAvailability.NotEnrolled
        else -> UnlockAvailability.Unavailable
    }
}

private fun Int.toUnlockErrorMessage(errString: CharSequence): String = when (this) {
    BiometricPrompt.ERROR_CANCELED,
    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
    BiometricPrompt.ERROR_USER_CANCELED -> "Bạn đã hủy xác thực."

    BiometricPrompt.ERROR_LOCKOUT,
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "Thiết bị tạm khóa xác thực. Hãy thử lại sau."

    else -> errString.toString()
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

private fun Context.openSecuritySettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_BIOMETRIC_ENROLL).putExtra(
            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            SETUP_AUTHENTICATORS,
        )
    } else {
        Intent(Settings.ACTION_SECURITY_SETTINGS)
    }

    runCatching {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.recoverCatching {
        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
