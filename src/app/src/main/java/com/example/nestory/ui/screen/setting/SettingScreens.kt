package com.example.nestory.ui.screen.setting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.example.nestory.data.local.entity.ReminderEntity
import com.example.nestory.domain.model.ExpiryReminderSettings
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.LocalInputMonitor
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.components.PrimaryActionButton
import com.example.nestory.ui.screen.document.DocumentStatusCalculator
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles
import com.example.nestory.utils.notification.NotificationHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Immutable
data class ExpiryReminderUiState(
    val enabled: Boolean = true,
    val leadTimeDays: Int = 7,
    val customLeadTimeMode: Boolean = false,
    val repeatDaily: Boolean = true,
    val inAppEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val hour: Int = 12,
    val minute: Int = 0,
)

fun ExpiryReminderSettings.toUiState(): ExpiryReminderUiState =
    ExpiryReminderUiState(
        enabled = enabled,
        leadTimeDays = leadTimeDays,
        customLeadTimeMode = false,
        repeatDaily = repeatDaily,
        inAppEnabled = inAppEnabled,
        pushEnabled = pushEnabled,
        hour = hour,
        minute = minute,
    )

fun ExpiryReminderUiState.toSettings(): ExpiryReminderSettings =
    ExpiryReminderSettings(
        enabled = enabled,
        leadTimeDays = leadTimeDays,
        repeatDaily = repeatDaily,
        inAppEnabled = inAppEnabled,
        pushEnabled = pushEnabled,
        hour = hour,
        minute = minute,
    )

private val reminderDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Ánh xạ UI reminder sang ReminderEntity cho riêng 1 giấy tờ hoặc 1 bộ hồ sơ. */
fun ExpiryReminderUiState.toReminderEntity(
    documentId: Long? = null,
    documentKitId: Long? = null,
    expiryDate: String?,
    id: Long = 0,
): ReminderEntity {
    val today = LocalDate.now()
    val reminderDate = expiryDate
        ?.let(DocumentStatusCalculator::parseExpirationDate)
        ?.let { expiry ->
            val configuredStartDate = expiry.minusDays(leadTimeDays.toLong())
            val effectiveReminderDate = if (configuredStartDate.isBefore(today)) {
                today
            } else {
                configuredStartDate
            }
            effectiveReminderDate.format(reminderDateFormatter)
        }
    return ReminderEntity(
        id = id,
        documentId = documentId,
        documentKitId = documentKitId,
        isEnabled = enabled,
        reminderDate = reminderDate,
        reminderTime = "%02d:%02d".format(hour, minute),
        leadTimeDays = leadTimeDays,
        customLeadTimeMode = customLeadTimeMode,
        repeatDaily = repeatDaily,
        inAppEnabled = inAppEnabled,
        pushEnabled = pushEnabled,
    )
}

/** Ánh xạ ngược ReminderEntity (của 1 giấy tờ/bộ hồ sơ) sang UI state. */
fun ReminderEntity.toExpiryReminderUiState(expiryDate: String?): ExpiryReminderUiState {
    val parts = reminderTime?.split(":") ?: emptyList()
    val parsedHour = parts.getOrNull(0)?.toIntOrNull()
    val parsedMinute = parts.getOrNull(1)?.toIntOrNull()
    val standardDays = setOf(1, 3, 7, 14)
    return ExpiryReminderUiState(
        enabled = isEnabled,
        leadTimeDays = leadTimeDays,
        customLeadTimeMode = customLeadTimeMode || leadTimeDays !in standardDays,
        repeatDaily = repeatDaily,
        inAppEnabled = inAppEnabled,
        pushEnabled = pushEnabled,
        hour = parsedHour ?: 12,
        minute = parsedMinute ?: 0,
    )
}

@Composable
fun SettingScreen(
    onBack: () -> Unit,
    onCategoryClick: () -> Unit,
    onContainerClick: () -> Unit,
    onExportBackupClick: () -> Unit = {},
    onRestoreBackupClick: () -> Unit = {},
) {
    BackHandler(onBack = onBack)

    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
    ) {
        SettingHeader(title = "Cài đặt", onBack = onBack)
        Spacer(modifier = Modifier.height(15.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NestoryRadius.R10)
                .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                .background(GeneratedColor.FigmaFfffff),
        ) {
            SettingMenuItem(
                title = "Danh mục",
                icon = AppIcons.FigmaCategory,
                iconSize = 34.dp,
                onClick = onCategoryClick,
            )
            SettingMenuItem(
                title = "Container",
                icon = AppIcons.WeuiFolderOutlined,
                onClick = onContainerClick,
            )
            SettingMenuItem(
                title = "Xuất bản sao lưu",
                icon = AppIcons.MdiExport,
                onClick = onExportBackupClick,
            )
            SettingMenuItem(
                title = "Khôi phục từ bản sao lưu",
                icon = AppIcons.MdiImport,
                onClick = onRestoreBackupClick,
                showDivider = false,
            )
        }
    }
}

@Composable
fun ExpiryReminderSettingScreen(
    state: ExpiryReminderUiState,
    onStateChange: (ExpiryReminderUiState) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = androidx.compose.ui.platform.LocalContext.current

    // Local state for immediate feedback to avoid flickering from async DataStore updates
    var localState by remember { mutableStateOf(state) }
    var savedState by remember { mutableStateOf(state) }
    var pendingSaveState by remember { mutableStateOf<ExpiryReminderUiState?>(null) }

    fun persistState(newState: ExpiryReminderUiState) {
        localState = newState
        savedState = newState
        onStateChange(newState)
    }

    val pushPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val pendingState = pendingSaveState
        pendingSaveState = null
        if (granted && pendingState != null) {
            persistState(pendingState)
        } else if (!granted && pendingState != null) {
            val fallback = pendingState.copy(pushEnabled = false)
            persistState(fallback)
            Toast.makeText(context, "Bạn cần cấp quyền thông báo để nhận nhắc hạn.", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Keep local state in sync with external state changes (e.g. initial load or background updates)
    LaunchedEffect(state) {
        if (localState == savedState || localState == state) {
            localState = state
        }
        savedState = state
    }

    val updateDraftState: (ExpiryReminderUiState) -> Unit = { newState ->
        localState = newState
    }

    fun saveStateWithPermission(newState: ExpiryReminderUiState) {
        if (
            newState.pushEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingSaveState = newState
            pushPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            persistState(newState)
        }
    }

    val saveDraft: () -> Unit = {
        saveStateWithPermission(localState)
    }
    val hasUnsavedChanges = localState != savedState

    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
    ) {
        SettingHeader(title = "Cài đặt thông báo nhắc hạn", onBack = onBack)
        Spacer(modifier = Modifier.height(15.dp))
        
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ReminderMasterCard(
                enabled = localState.enabled,
                onEnabledChange = { enabled ->
                    if (enabled) {
                        saveStateWithPermission(ExpiryReminderUiState())
                    } else {
                        persistState(localState.copy(enabled = false))
                    }
                },
            )
            
            if (localState.enabled) {
                Spacer(modifier = Modifier.height(15.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ReminderEnabledPanel(
                        state = localState,
                        onStateChange = updateDraftState,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                PrimaryActionButton(
                    text = "Lưu",
                    enabled = hasUnsavedChanges,
                    onClick = saveDraft,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SettingHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Image(
            painter = painterResource(AppIcons.IcBackwardArrow),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .clickable(onClick = onBack),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = NestoryTextStyles.Body20Bold,
            color = GeneratedColor.Figma000000,
        )
    }
}

@Composable
private fun SettingMenuItem(
    title: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    iconSize: androidx.compose.ui.unit.Dp = 30.dp,
    showDivider: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(GeneratedColor.Figma1855ee),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W700),
                color = GeneratedColor.Figma000000,
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GeneratedColor.FigmaE5e7eb),
            )
        }
    }
}

@Composable
private fun ReminderMasterCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(NestoryRadius.R10)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
            .background(GeneratedColor.FigmaFfffff)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Nhắc hạn sử dụng",
                style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W700),
                color = GeneratedColor.Figma000000,
            )
            Text(
                text = "Nhận thông báo khi giấy tờ sắp hết hạn",
                style = NestoryTextStyles.Body10Semi.copy(fontWeight = FontWeight.W500),
                color = GeneratedColor.Figma919191,
            )
        }
        NestorySwitch(checked = enabled, onCheckedChange = onEnabledChange)
    }
}

@Composable
private fun ReminderEnabledPanel(
    state: ExpiryReminderUiState,
    onStateChange: (ExpiryReminderUiState) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentState by rememberUpdatedState(state)
    val testNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            NotificationHelper(context).showTestNotification()
        } else {
            Toast.makeText(context, "Bạn cần cấp quyền thông báo để gửi thử nghiệm.", Toast.LENGTH_SHORT).show()
        }
    }
    val sendTestNotification = {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            testNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationHelper(context).showTestNotification()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NestoryRadius.R10)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
            .background(GeneratedColor.FigmaFfffff)
            .padding(top = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReminderLeadTimeSection(
            selectedDays = state.leadTimeDays,
            customMode = state.customLeadTimeMode,
            onSelectedDaysChange = { days, customMode ->
                onStateChange(
                    currentState.copy(
                        leadTimeDays = days,
                        customLeadTimeMode = customMode,
                    )
                )
            },
        )
        HorizontalDivider()
        ReminderFrequencySection(
            repeatDaily = state.repeatDaily,
            onRepeatDailyChange = { onStateChange(currentState.copy(repeatDaily = it)) },
        )
        ReminderTimeOfDaySection(
            hour = state.hour,
            minute = state.minute,
            onTimeChange = { h, m -> onStateChange(currentState.copy(hour = h, minute = m)) }
        )
        HorizontalDivider()
        ReminderChannelSection(
            state = state,
            onStateChange = onStateChange,
        )
        
        // Nút Test Thông báo
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(45.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GeneratedColor.FigmaFfffff)
                .border(1.dp, GeneratedColor.Figma1855ee, RoundedCornerShape(8.dp))
                .clickable { sendTestNotification() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Gửi thông báo thử nghiệm ngay",
                style = NestoryTextStyles.Body13Semi,
                color = GeneratedColor.Figma1855ee
            )
        }
    }
}

@Composable
private fun ReminderLeadTimeSection(
    selectedDays: Int,
    customMode: Boolean,
    onSelectedDaysChange: (Int, Boolean) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val monitor = LocalInputMonitor.current
    val standardDays = remember { listOf(1, 3, 7, 14) }
    var customText by remember(selectedDays, customMode) {
        mutableStateOf(if (customMode) selectedDays.toString() else "30")
    }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(customText, customMode) {
        if (!customMode || customText.isNotEmpty()) {
            isError = false
        }
    }

    val validateAndSubmit = remember(customText) {
        {
            val value = customText.toIntOrNull()
            if (value == null || value <= 0) {
                isError = true
            } else {
                isError = false
                onSelectedDaysChange(value, true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        SectionTitle(
            title = "Thời gian nhắc",
            subtitle = "Chọn thời điểm nhắc trước khi hết hạn",
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        standardDays.forEach { days ->
            key(days) {
                ReminderRadioRow(
                    text = "Trước $days ngày",
                    selected = !customMode && selectedDays == days,
                    onClick = { 
                        isError = false
                        onSelectedDaysChange(days, false)
                    },
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
        
        // Custom row with Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.clickable { 
                if (!customMode) {
                    val nextDays = selectedDays.takeIf { it !in standardDays }
                        ?: customText.toIntOrNull()?.takeIf { it > 0 }
                        ?: 30
                    customText = nextDays.toString()
                    isError = false
                    onSelectedDaysChange(nextDays, true)
                }
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingRadioMark(selected = customMode)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tùy chỉnh",
                        style = NestoryTextStyles.Body15Medium.copy(fontWeight = FontWeight.W500),
                        color = GeneratedColor.Figma000000,
                    )
                }
            }
            
            if (customMode) {
                Spacer(modifier = Modifier.width(15.dp))
                Row(
                    modifier = Modifier
                        .width(50.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .border(
                            width = 1.dp, 
                            color = if (isError) Color.Red else GeneratedColor.FigmaE5e7eb, 
                            shape = RoundedCornerShape(5.dp)
                        )
                        .background(GeneratedColor.FigmaFfffff),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    BasicTextField(
                        value = customText,
                        onValueChange = { 
                            val digits = it.filter { c -> c.isDigit() }
                            if (digits.length <= 3) {
                                customText = digits
                                monitor.update(digits)
                                val value = digits.toIntOrNull()
                                if (value != null && value > 0) {
                                    isError = false
                                    onSelectedDaysChange(value, true)
                                } else {
                                    isError = digits.isNotEmpty()
                                }
                            }
                        },
                        textStyle = NestoryTextStyles.Body12Medium.copy(
                            textAlign = TextAlign.Center,
                            color = GeneratedColor.Figma000000
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { 
                                validateAndSubmit()
                                focusManager.clearFocus()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    monitor.show(customText, "Số ngày")
                                } else {
                                    monitor.hide()
                                }
                                if (!focusState.isFocused && customText.isNotEmpty()) {
                                    validateAndSubmit()
                                }
                            }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ngày",
                    style = NestoryTextStyles.Body13Medium,
                    color = GeneratedColor.Figma919191
                )
            }
        }
        if (customMode && isError) {
            Text(
                text = "Vui lòng nhập số ngày hợp lệ",
                color = Color.Red,
                style = NestoryTextStyles.Body10Semi,
                modifier = Modifier.padding(start = 125.dp, top = 2.dp)
            )
        }
    }
}




@Composable
private fun ReminderFrequencySection(
    repeatDaily: Boolean,
    onRepeatDailyChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        SectionTitle(
            title = "Tần suất nhắc lại",
            subtitle = "Nhắc lại nếu chưa đánh dấu đã xử lý",
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .clip(RoundedCornerShape(5.dp))
                .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(5.dp))
                .padding(2.dp),
        ) {
            FrequencyOption(
                text = "Không lặp",
                selected = !repeatDaily,
                onClick = { onRepeatDailyChange(false) },
                modifier = Modifier.weight(1f),
            )
            FrequencyOption(
                text = "Hàng ngày",
                selected = repeatDaily,
                onClick = { onRepeatDailyChange(true) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReminderTimeOfDaySection(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        SectionTitle(
            title = "Thời điểm nhắc lại trong ngày",
            subtitle = "Chọn thời điểm ứng dụng gửi thông báo",
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(10.dp))
                    .background(GeneratedColor.FigmaFfffff),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour Wheel
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TimeWheelPicker(
                        range = 0..23,
                        initialValue = hour,
                        onValueChange = {
                            if (it != hour) {
                                onTimeChange(it, minute)
                            }
                        }
                    )
                }
                
                Text(
                    text = ":",
                    style = NestoryTextStyles.Body18Semi,
                    color = GeneratedColor.Figma000000,
                )
                
                // Minute Wheel
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TimeWheelPicker(
                        range = 0..59,
                        initialValue = minute,
                        onValueChange = {
                            if (it != minute) {
                                onTimeChange(hour, it)
                            }
                        },
                        format = { it.toString().padStart(2, '0') }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeWheelPicker(
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    format: (Int) -> String = { it.toString() }
) {
    val items = remember(range) { range.toList() }
    val totalItems = items.size
    val initialPage = remember(totalItems, initialValue) {
        val index = items.indexOf(initialValue).let { if (it == -1) 0 else it }
        500 * totalItems + index
    }
    
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 1000 * totalItems }
    )

    LaunchedEffect(initialPage) {
        if (pagerState.currentPage != initialPage) {
            pagerState.scrollToPage(initialPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val actualIndex = pagerState.currentPage % totalItems
        onValueChange(items[actualIndex])
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.height(88.dp),
        contentPadding = PaddingValues(vertical = 31.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) { page ->
        val actualIndex = page % totalItems
        val value = items[actualIndex]
        
        // Optimize selection state check
        val isSelected = pagerState.currentPage == page
        
        Box(
            modifier = Modifier
                .height(26.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = format(value),
                style = if (isSelected) NestoryTextStyles.Body15Semi else NestoryTextStyles.Body12Medium,
                color = if (isSelected) GeneratedColor.Figma1855ee else GeneratedColor.Figma919191,
                modifier = Modifier.alpha(if (isSelected) 1f else 0.5f)
            )
        }
    }
}



@Composable
private fun ReminderChannelSection(
    state: ExpiryReminderUiState,
    onStateChange: (ExpiryReminderUiState) -> Unit,
) {
    val currentState by rememberUpdatedState(state)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        SectionTitle(
            title = "Kênh thông báo",
            subtitle = "Chọn nơi nhận thông báo nhắc hạn",
        )
        Spacer(modifier = Modifier.height(10.dp))
        ReminderSwitchRow(
            text = "Thông báo trong ứng dụng",
            checked = state.inAppEnabled,
            onCheckedChange = { onStateChange(currentState.copy(inAppEnabled = it)) },
        )
        ReminderSwitchRow(
            text = "Thông báo đẩy",
            checked = state.pushEnabled,
            onCheckedChange = { onStateChange(currentState.copy(pushEnabled = it)) },
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
) {
    Column {
        Text(
            text = title,
            style = NestoryTextStyles.Body15Semi.copy(fontWeight = FontWeight.W700),
            color = GeneratedColor.Figma000000,
        )
        Text(
            text = subtitle,
            style = NestoryTextStyles.Body10Semi.copy(fontWeight = FontWeight.W500),
            color = GeneratedColor.Figma919191,
        )
    }
}

@Composable
private fun ReminderRadioRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingRadioMark(
            selected = selected,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = NestoryTextStyles.Body15Medium.copy(fontWeight = FontWeight.W500),
            color = GeneratedColor.Figma000000,
        )
    }
}

@Composable
private fun SettingRadioMark(
    selected: Boolean,
) {
    Box(
        modifier = Modifier.size(30.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (selected) GeneratedColor.Figma1855ee else Color(0xFF49454F),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GeneratedColor.Figma1855ee),
                )
            }
        }
    }
}

@Composable
private fun FrequencyOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) GeneratedColor.Figma1855ee else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = NestoryTextStyles.Body10Semi.copy(fontWeight = FontWeight.W500),
            color = if (selected) GeneratedColor.FigmaFfffff else GeneratedColor.Figma000000,
        )
    }
}

@Composable
private fun ReminderSwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = NestoryTextStyles.Body15Medium.copy(fontWeight = FontWeight.W400),
            color = GeneratedColor.Figma000000,
            modifier = Modifier.weight(1f),
        )
        NestorySwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NestorySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = GeneratedColor.FigmaFfffff,
            checkedTrackColor = GeneratedColor.Figma1855ee,
            uncheckedThumbColor = Color(0xFF79747E),
            uncheckedTrackColor = Color(0xFFE6E0E9),
            uncheckedBorderColor = Color(0xFF79747E),
        ),
    )
}

@Composable
private fun HorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GeneratedColor.FigmaE5e7eb),
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    SettingScreen(
        onBack = {},
        onCategoryClick = {},
        onContainerClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ExpiryReminderSettingScreenPreview() {
    ExpiryReminderSettingScreen(
        state = ExpiryReminderUiState(enabled = true),
        onStateChange = {},
        onBack = {},
    )
}
