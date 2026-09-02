package com.example.nestory.ui.screen.setting

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.R
import com.example.nestory.data.filesystem.BackupExportEstimate
import com.example.nestory.data.filesystem.BackupExportResult
import com.example.nestory.data.filesystem.BackupImportResult
import com.example.nestory.data.filesystem.BackupOperationStep
import com.example.nestory.data.filesystem.BackupOperationStepStatus
import com.example.nestory.data.filesystem.BackupProgressStep
import com.example.nestory.data.filesystem.BackupProgressSnapshot
import com.example.nestory.data.filesystem.NestoryBackupManager
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.components.SecondaryActionButton
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles
import com.example.nestory.ui.components.LocalInputMonitor
import androidx.compose.ui.focus.onFocusChanged
import java.util.Locale

enum class BackupStepStatus {
    PENDING, SUCCESS, ERROR
}

data class BackupStep(
    val title: String,
    val status: BackupStepStatus
)

@Composable
fun ExportBackupScreen(
    onBack: () -> Unit,
    onContinue: (targetUri: Uri, fileName: String) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val backupManager = remember { NestoryBackupManager(context) }
    var estimate by remember {
        mutableStateOf(
            BackupExportEstimate(
                includedLabel = "Đang tính",
                encryptionLabel = "AES-256-GCM",
                estimatedSize = 0L,
            )
        )
    }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedLocationLabel by remember { mutableStateOf("Chưa chọn") }
    val createBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            selectedUri = uri
            val displayName = context.contentResolver.displayName(uri)
            selectedFileName = displayName ?: selectedFileName.ifBlank { backupManager.defaultBackupFileName() }
            selectedLocationLabel = displayName
                ?.shortenMiddle(maxLength = 24)
                ?: uri.toString().shortenMiddle(maxLength = 24)
        }
    }

    LaunchedEffect(Unit) {
        estimate = backupManager.estimateExport()
    }

    fun openSavePicker() {
        val fileName = backupManager.defaultBackupFileName()
        selectedFileName = fileName
        createBackupLauncher.launch(fileName)
    }

    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
        scrollable = true
    ) {
        BackupHeader(title = "Xuất bản sao lưu", onBack = onBack)
        
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            
            Image(
                painter = painterResource(id = AppImages.ExportIllustration),
                contentDescription = null,
                modifier = Modifier.size(width = 300.dp, height = 203.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(
                text = "Xuất bản sao lưu",
                style = NestoryTextStyles.Body20Bold,
                color = GeneratedColor.Figma000000
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(
                text = "Tạo một bản sao lưu để lưu trữ an toàn dữ liệu của bạn hoặc chuyển sang thiết bị khác",
                style = NestoryTextStyles.Body18Semi.copy(fontWeight = FontWeight.W500),
                color = GeneratedColor.Figma919191,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(318.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NestoryRadius.R10)
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                    .background(GeneratedColor.FigmaFfffff)
            ) {
                BackupOptionItem(label = "Bao gồm", value = estimate.includedLabel, showChevron = false)
                HorizontalDivider()
                BackupOptionItem(label = "Mã hóa", value = estimate.encryptionLabel, showChevron = false)
                HorizontalDivider()
                BackupOptionItem(label = "Dung lượng ước tính", value = estimate.estimatedSize.formatFileSize(), showChevron = false)
                HorizontalDivider()
                BackupOptionItem(
                    label = "Vị trí lưu",
                    value = selectedLocationLabel,
                    showChevron = true,
                    valueColor = if (selectedUri == null) Color(0xFFFF4D00) else GeneratedColor.Figma919191,
                    onClick = ::openSavePicker,
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            PrimaryBackupButton(
                text = "Tiếp tục",
                onClick = {
                    val uri = selectedUri
                    if (uri == null) {
                        openSavePicker()
                    } else {
                        onContinue(uri, selectedFileName.ifBlank { backupManager.defaultBackupFileName() })
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(49.dp))
        }
    }
}

@Composable
fun SetBackupPasswordScreen(
    onBack: () -> Unit,
    onContinue: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val passwordValid = password.length >= 12 &&
        password.any { it.isUpperCase() } &&
        password.any { it.isLowerCase() } &&
        password.any { !it.isLetterOrDigit() || it.isDigit() } &&
        password == confirmPassword

    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
        scrollable = false
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BackupHeader(title = "Đổi mật khẩu", onBack = onBack)
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Image(
                painter = painterResource(id = AppImages.ImgLock),
                contentDescription = null,
                modifier = Modifier.size(width = 240.dp, height = 180.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(
                text = "Đổi mật khẩu cho sao lưu",
                style = NestoryTextStyles.Body20Bold,
                color = GeneratedColor.Figma000000
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Mật khẩu sẽ được dùng để mã hóa và bảo vệ file sao lưu của bạn",
                style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W500),
                color = GeneratedColor.Figma919191,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(318.dp)
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            BackupTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Mật khẩu",
                isVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BackupTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Nhập lại mật khẩu",
                isVisible = confirmVisible,
                onToggleVisibility = { confirmVisible = !confirmVisible }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEFEF), RoundedCornerShape(10.dp))
                    .padding(15.dp)
            ) {
                Text(text = "Yêu cầu mật khẩu", style = NestoryTextStyles.Body12Medium, color = GeneratedColor.Figma919191)
                Spacer(modifier = Modifier.height(8.dp))
                PasswordRequirementRow(text = "Tối thiểu 12 ký tự", isMet = password.length >= 12)
                PasswordRequirementRow(text = "Bao gồm chữ hoa và thường", isMet = password.any { it.isUpperCase() } && password.any { it.isLowerCase() })
                PasswordRequirementRow(text = "Bao gồm số hoặc ký tự đặc biệt", isMet = password.any { !it.isLetter() })
                PasswordRequirementRow(text = "Mật khẩu nhập lại khớp", isMet = password.isNotEmpty() && password == confirmPassword)
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    style = NestoryTextStyles.Body12Medium,
                    color = GeneratedColor.FigmaFf0000,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryBackupButton(
                text = "Tiếp tục",
                enabled = passwordValid,
                onClick = {
                    if (!passwordValid) {
                        errorMessage = "Mật khẩu chưa đạt yêu cầu hoặc chưa khớp."
                        return@PrimaryBackupButton
                    }
                    errorMessage = null
                    onContinue(password)
                }
            )
            
            Spacer(modifier = Modifier.height(49.dp))
        }
    }
}

@Composable
fun ImportBackupScreen(
    onBack: () -> Unit,
    onContinue: (sourceUri: Uri, fileName: String) -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    val openBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            selectedUri = uri
            selectedName = context.contentResolver.displayName(uri) ?: "File sao lưu đã chọn"
        }
    }

    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
        scrollable = true
    ) {
        BackupHeader(title = "Nhập bản sao lưu", onBack = onBack)
        
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            
            Image(
                painter = painterResource(id = AppImages.ImportIllustration),
                contentDescription = null,
                modifier = Modifier.size(width = 322.dp, height = 240.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(
                text = "Chọn file bản sao lưu",
                style = NestoryTextStyles.Body20Bold,
                color = GeneratedColor.Figma000000
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(
                text = "Chọn file bản sao lưu được xuất ra từ thiết bị khác để khôi phục dữ liệu",
                style = NestoryTextStyles.Body18Semi.copy(fontWeight = FontWeight.W500),
                color = GeneratedColor.Figma919191,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(318.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(NestoryRadius.R10)
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                    .background(GeneratedColor.FigmaFfffff)
                    .clickable { openBackupLauncher.launch(arrayOf("application/octet-stream", "application/zip", "*/*")) }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chọn file sao lưu",
                    style = NestoryTextStyles.Body15Semi,
                    color = GeneratedColor.Figma000000,
                    modifier = Modifier.padding(start = 10.dp).weight(1f)
                )
                Text(
                    text = selectedName ?: "Chưa chọn",
                    style = NestoryTextStyles.Body15Semi,
                    color = if (selectedUri == null) GeneratedColor.Figma919191 else GeneratedColor.Figma000000,
                    textAlign = TextAlign.End,
                    modifier = Modifier.widthIn(max = 150.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Image(
                    painter = painterResource(id = AppIcons.WeuiFolderOutlined),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(GeneratedColor.Figma919191)
                )
            }
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = AppIcons.MaterialSymbolsLightLock),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    colorFilter = ColorFilter.tint(GeneratedColor.Figma717171)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Đảm bảo file không lỗi và được xuất từ ứng dụng nestory",
                    style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W600),
                    color = GeneratedColor.Figma919191,
                    modifier = Modifier.width(314.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            PrimaryBackupButton(
                text = "Tiếp tục",
                enabled = selectedUri != null,
                onClick = {
                    val uri = selectedUri ?: return@PrimaryBackupButton
                    onContinue(uri, selectedName ?: "Nestory backup")
                }
            )
            
            Spacer(modifier = Modifier.height(49.dp))
        }
    }
}

@Composable
fun ImportPasswordScreen(
    onBack: () -> Unit,
    onContinue: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
        scrollable = false
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BackupHeader(title = "Nhập mật khẩu", onBack = onBack)
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Image(
                painter = painterResource(id = AppImages.ImgLock),
                contentDescription = null,
                modifier = Modifier.size(width = 240.dp, height = 180.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(
                text = "Nhập mật khẩu để giải mã",
                style = NestoryTextStyles.Body20Bold,
                color = GeneratedColor.Figma000000
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Nhập mật khẩu của bản sao lưu để tiếp tục khôi phục dữ liệu",
                style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W500),
                color = GeneratedColor.Figma919191,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(318.dp)
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            BackupTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Mật khẩu",
                isVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Quên mật khẩu?",
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.Figma1855ee,
                modifier = Modifier.clickable { }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryBackupButton(
                text = "Tiếp tục",
                enabled = password.isNotBlank(),
                onClick = { onContinue(password) }
            )
            
            Spacer(modifier = Modifier.height(49.dp))
        }
    }
}

@Composable
fun BackupProgressScreen(
    title: String,
    initialProgress: Float = 0f,
    steps: List<BackupStep>,
    isImportFlow: Boolean,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    operation: suspend (onProgress: (BackupProgressSnapshot) -> Unit) -> Unit,
) {
    var progress by remember { mutableFloatStateOf(initialProgress) }
    var currentSteps by remember { mutableStateOf(steps) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }
    val hasError = currentSteps.any { it.status == BackupStepStatus.ERROR } || errorMessage != null
    
    LaunchedEffect(retryKey) {
        progress = initialProgress
        currentSteps = steps
        errorMessage = null
        runCatching {
            operation { snapshot ->
                progress = snapshot.progress
                currentSteps = snapshot.steps.map { it.toBackupStep(isImportFlow) }
            }
        }.onSuccess {
            onComplete()
        }.onFailure { throwable ->
            errorMessage = throwable.message ?: "Không thể xử lý file sao lưu."
            if (currentSteps.none { it.status == BackupStepStatus.ERROR }) {
                val index = currentSteps.indexOfFirst { it.status == BackupStepStatus.PENDING }
                    .takeIf { it >= 0 }
                    ?: currentSteps.lastIndex
                currentSteps = currentSteps.mapIndexed { i, step ->
                    if (i == index) step.copy(status = BackupStepStatus.ERROR) else step
                }
            }
        }
    }

    NestoryScreen(useStatusBarPadding = true) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            
            // Circular Progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    drawArc(
                        color = Color(0xFFEFF6FF),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = GeneratedColor.Figma1855ee,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = NestoryTextStyles.Heading25Bold,
                    color = GeneratedColor.Figma000000
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = title,
                style = NestoryTextStyles.Heading25Bold,
                color = GeneratedColor.Figma000000,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Vui lòng không đóng ứng dụng.",
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.Figma919191
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(NestoryRadius.R18)
                    .background(GeneratedColor.FigmaFfffff)
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                currentSteps.forEach { step ->
                    Row(
                        modifier = Modifier.width(250.dp).height(46.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconRes = when (step.status) {
                            BackupStepStatus.SUCCESS -> AppIcons.NestoryTickCircle
                            BackupStepStatus.ERROR -> AppIcons.CharmCircleCross
                            BackupStepStatus.PENDING -> AppIcons.MaterialSymbolsPending
                        }
                        val iconColor = when (step.status) {
                            BackupStepStatus.SUCCESS -> GeneratedColor.Figma07bc67
                            BackupStepStatus.ERROR -> GeneratedColor.FigmaFf0000
                            BackupStepStatus.PENDING -> GeneratedColor.Figma919191
                        }
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(iconColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = step.title, 
                            style = NestoryTextStyles.Body14Medium,
                            color = GeneratedColor.Figma000000
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            if (hasError) {
                errorMessage?.let {
                    Text(
                        text = it,
                        style = NestoryTextStyles.Body12Medium,
                        color = GeneratedColor.FigmaFf0000,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 49.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryActionButton(text = "Thử lại", onClick = { retryKey++ })
                    SecondaryActionButton(text = "Quay lại", onClick = onCancel)
                }
            }
        }
    }
}

@Composable
fun ExportSuccessScreen(
    result: BackupExportResult?,
    onHomeClick: () -> Unit
) {
    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
        scrollable = false
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Image(
                painter = painterResource(id = AppImages.ImgSuccess),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Bản sao lưu được xuất thành công",
                style = NestoryTextStyles.Body20Bold,
                color = GeneratedColor.Figma000000,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "File đã lưu tại:",
                style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W500),
                color = GeneratedColor.Figma919191
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(10.dp))
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = AppIcons.KitFile),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    colorFilter = ColorFilter.tint(GeneratedColor.Figma1855ee)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result?.fileName ?: "Nestory backup",
                        style = NestoryTextStyles.Body14Medium,
                        color = GeneratedColor.Figma000000
                    )
                    Text(
                        text = result?.fileSize?.formatFileSize() ?: "--",
                        style = NestoryTextStyles.Body12Medium,
                        color = GeneratedColor.Figma919191
                    )
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = AppIcons.IcShare),
                        contentDescription = null,
                        tint = GeneratedColor.Figma1855ee,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NestoryRadius.R10)
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                    .background(GeneratedColor.FigmaFfffff)
            ) {
                InfoRow(label = "Ngày tạo:", value = result?.createdAt ?: "--")
                HorizontalDivider()
                InfoRow(label = "Kích thước", value = result?.fileSize?.formatFileSize() ?: "--")
                HorizontalDivider()
                InfoRow(label = "Vị trí lưu", value = result?.locationLabel ?: "Vị trí bạn đã chọn")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryBackupButton(text = "Trở về trang home", onClick = onHomeClick)
            
            Spacer(modifier = Modifier.height(49.dp))
        }
    }
}

@Composable
fun ImportSuccessScreen(
    result: BackupImportResult?,
    onHomeClick: () -> Unit
) {
    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
        scrollable = false
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Image(
                painter = painterResource(id = AppImages.ImgSuccess),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Đã khôi phục dữ liệu thành công!",
                style = NestoryTextStyles.Body20Bold,
                color = GeneratedColor.Figma000000,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NestoryRadius.R10)
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                    .background(GeneratedColor.FigmaFfffff)
            ) {
                InfoRow(label = "Tổng số mục", value = result?.totalItems?.toString() ?: "--")
                HorizontalDivider()
                InfoRow(label = "Dung lượng", value = result?.fileSize?.formatFileSize() ?: "--")
                HorizontalDivider()
                InfoRow(label = "Thời gian hoàn tất", value = result?.completedAt ?: "--")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryBackupButton(text = "Trở về trang home", onClick = onHomeClick)
            
            Spacer(modifier = Modifier.height(49.dp))
        }
    }
}

@Composable
private fun BackupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    val monitor = LocalInputMonitor.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(NestoryRadius.R10)
                .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                .background(GeneratedColor.FigmaFfffff)
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = { 
                    if (!it.contains('\n')) {
                        onValueChange(it)
                        monitor.update(it)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { 
                        if (it.isFocused) monitor.show(value, placeholder)
                        else monitor.hide()
                    },
                singleLine = true,
                visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = NestoryTextStyles.Body15Semi.copy(color = GeneratedColor.Figma000000),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = NestoryTextStyles.Body15Semi,
                            color = GeneratedColor.Figma919191
                        )
                    }
                    innerTextField()
                }
            )
            Text(
                text = if (isVisible) "Ẩn" else "Hiện",
                style = NestoryTextStyles.Body12Medium,
                color = GeneratedColor.Figma919191,
                modifier = Modifier.clickable { onToggleVisibility() }
            )
        }
    }
}

@Composable
private fun PasswordRequirementRow(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Image(
            painter = painterResource(id = AppIcons.NestoryTickCircle),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            colorFilter = ColorFilter.tint(if (isMet) GeneratedColor.Figma07bc67 else GeneratedColor.Figma919191)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = NestoryTextStyles.Body12Medium, color = GeneratedColor.Figma919191)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W500), 
            modifier = Modifier.weight(1f),
            color = GeneratedColor.Figma000000
        )
        Text(
            text = value, 
            style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W500), 
            color = GeneratedColor.Figma919191
        )
    }
}

@Composable
private fun BackupOptionItem(
    label: String, 
    value: String, 
    showChevron: Boolean = true,
    valueColor: Color = GeneratedColor.Figma919191,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = NestoryTextStyles.Body15Semi, 
            color = GeneratedColor.Figma000000,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value, 
            style = NestoryTextStyles.Body15Semi,
            color = valueColor,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 170.dp)
        )
        if (showChevron) {
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = AppIcons.LsiconRightFilled),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(GeneratedColor.Figma717171)
            )
        }
    }
}

@Composable
private fun PrimaryBackupButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    PrimaryActionButton(text = text, onClick = onClick, enabled = enabled)
}

@Composable
private fun BackupHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Image(
            painter = painterResource(id = AppIcons.IcBackwardArrow),
            contentDescription = null,
            modifier = Modifier.size(26.dp).clickable(onClick = onBack)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title, 
            style = NestoryTextStyles.Body20Bold,
            color = GeneratedColor.Figma000000
        )
    }
}

private fun BackupProgressStep.toBackupStep(isImportFlow: Boolean): BackupStep =
    BackupStep(
        title = step.label(isImportFlow),
        status = when (status) {
            BackupOperationStepStatus.PENDING -> BackupStepStatus.PENDING
            BackupOperationStepStatus.SUCCESS -> BackupStepStatus.SUCCESS
            BackupOperationStepStatus.ERROR -> BackupStepStatus.ERROR
        }
    )

private fun BackupOperationStep.label(isImportFlow: Boolean): String = when (this) {
    BackupOperationStep.PREPARE_DATA -> "Đang chuẩn bị dữ liệu"
    BackupOperationStep.COMPRESS_DATA -> "Đang nén dữ liệu"
    BackupOperationStep.ENCRYPT_DATA -> "Đang mã hóa dữ liệu"
    BackupOperationStep.SAVE_FILE -> "Đang lưu file"
    BackupOperationStep.VERIFY_BACKUP -> "Đang xác thực file sao lưu"
    BackupOperationStep.DECRYPT_DATA -> "Đang giải mã dữ liệu"
    BackupOperationStep.RESTORE_DATA -> "Đang khôi phục dữ liệu"
    BackupOperationStep.COMPLETE -> if (isImportFlow) "Hoàn tất" else "Hoàn tất"
}

private fun Long.formatFileSize(): String {
    if (this <= 0L) return "--"
    val kb = this / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.US, "%.1f MB", mb)
    } else {
        String.format(Locale.US, "%.0f KB", kb)
    }
}

private fun String.shortenMiddle(maxLength: Int): String {
    if (length <= maxLength) return this
    val sideLength = ((maxLength - 1) / 2).coerceAtLeast(1)
    return take(sideLength) + "…" + takeLast(sideLength)
}

private fun android.content.ContentResolver.displayName(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }

@Composable
private fun HorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GeneratedColor.FigmaE5e7eb)
    )
}
