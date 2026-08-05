package com.example.nestory.ui.screen.setting

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nestory.domain.model.ExpiryReminderSettings
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestoryTextStyles

@Immutable
data class ExpiryReminderUiState(
    val enabled: Boolean = true,
    val leadTimeDays: Int = 7,
    val repeatDaily: Boolean = true,
    val inAppEnabled: Boolean = true,
    val emailEnabled: Boolean = false,
    val pushEnabled: Boolean = true,
)

fun ExpiryReminderSettings.toUiState(): ExpiryReminderUiState =
    ExpiryReminderUiState(
        enabled = enabled,
        leadTimeDays = leadTimeDays,
        repeatDaily = repeatDaily,
        inAppEnabled = inAppEnabled,
        emailEnabled = emailEnabled,
        pushEnabled = pushEnabled,
    )

fun ExpiryReminderUiState.toSettings(): ExpiryReminderSettings =
    ExpiryReminderSettings(
        enabled = enabled,
        leadTimeDays = leadTimeDays,
        repeatDaily = repeatDaily,
        inAppEnabled = inAppEnabled,
        emailEnabled = emailEnabled,
        pushEnabled = pushEnabled,
    )

@Composable
fun SettingScreen(
    onBack: () -> Unit,
    onExpiryReminderClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onContainerClick: () -> Unit,
    onExportBackupClick: () -> Unit = {},
    onRestoreBackupClick: () -> Unit = {},
) {
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
                .height(300.dp)
                .clip(NestoryRadius.R10)
                .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
                .background(GeneratedColor.FigmaFfffff),
        ) {
            SettingMenuItem(
                title = "Thông báo nhắc hạn",
                icon = AppIcons.NestoryNotification,
                onClick = onExpiryReminderClick,
            )
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
    NestoryScreen(
        horizontalPadding = 20.dp,
        verticalPadding = 0.dp,
        useStatusBarPadding = true,
        scrollable = true,
    ) {
        SettingHeader(title = "Cài đặt thông báo nhắc hạn", onBack = onBack)
        Spacer(modifier = Modifier.height(15.dp))
        ReminderMasterCard(
            enabled = state.enabled,
            onEnabledChange = { onStateChange(state.copy(enabled = it)) },
        )
        if (state.enabled) {
            Spacer(modifier = Modifier.height(15.dp))
            ReminderEnabledPanel(
                state = state,
                onStateChange = onStateChange,
            )
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
            modifier = Modifier.height(26.dp),
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
                style = NestoryTextStyles.Body15Semi.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W700),
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
                style = NestoryTextStyles.Body15Semi.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W700),
                color = GeneratedColor.Figma000000,
            )
            Text(
                text = "Nhận thông báo khi giấy tờ sắp hết hạn",
                style = NestoryTextStyles.Body10Semi.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W500),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(647.dp)
            .clip(NestoryRadius.R10)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, NestoryRadius.R10)
            .background(GeneratedColor.FigmaFfffff)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        ReminderLeadTimeSection(
            selectedDays = state.leadTimeDays,
            onSelectedDaysChange = { onStateChange(state.copy(leadTimeDays = it)) },
        )
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))
        ReminderFrequencySection(
            repeatDaily = state.repeatDaily,
            onRepeatDailyChange = { onStateChange(state.copy(repeatDaily = it)) },
        )
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))
        ReminderChannelSection(
            state = state,
            onStateChange = onStateChange,
        )
    }
}

@Composable
private fun ReminderLeadTimeSection(
    selectedDays: Int,
    onSelectedDaysChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(234.dp)
            .padding(horizontal = 20.dp),
    ) {
        SectionTitle(
            title = "Thời gian nhắc",
            subtitle = "Chọn thời điểm nhắc trước khi hết hạn",
        )
        Spacer(modifier = Modifier.height(10.dp))
        listOf(1, 3, 7, 14).forEach { days ->
            ReminderRadioRow(
                text = "Trước $days ngày",
                selected = selectedDays == days,
                onClick = { onSelectedDaysChange(days) },
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        ReminderRadioRow(
            text = "Tùy chỉnh",
            selected = selectedDays !in setOf(1, 3, 7, 14),
            onClick = { onSelectedDaysChange(30) },
        )
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
            .height(99.dp)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        SectionTitle(
            title = "Tần suất nhắc lại",
            subtitle = "Nhắc lại nếu chưa đánh dấu đã xử lý",
        )
        Spacer(modifier = Modifier.height(11.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
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
private fun ReminderChannelSection(
    state: ExpiryReminderUiState,
    onStateChange: (ExpiryReminderUiState) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(234.dp)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        SectionTitle(
            title = "Kênh thông báo",
            subtitle = "Chọn nơi nhận thông báo nhắc hạn",
        )
        ReminderSwitchRow(
            text = "Thông báo trong ứng dụng",
            checked = state.inAppEnabled,
            onCheckedChange = { onStateChange(state.copy(inAppEnabled = it)) },
        )
        ReminderSwitchRow(
            text = "Email",
            checked = state.emailEnabled,
            onCheckedChange = { onStateChange(state.copy(emailEnabled = it)) },
        )
        ReminderSwitchRow(
            text = "Thông báo đẩy",
            checked = state.pushEnabled,
            onCheckedChange = { onStateChange(state.copy(pushEnabled = it)) },
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
) {
    Column(modifier = Modifier.height(34.dp)) {
        Text(
            text = title,
            style = NestoryTextStyles.Body15Semi.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W700),
            color = GeneratedColor.Figma000000,
        )
        Text(
            text = subtitle,
            style = NestoryTextStyles.Body10Semi.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W500),
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
            style = NestoryTextStyles.Body15Medium,
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
            .height(24.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) GeneratedColor.Figma1855ee else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = NestoryTextStyles.Body10Semi.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W500),
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
            style = NestoryTextStyles.Body15Medium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W400),
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
            .height(0.dp)
            .drawBehind {
                drawLine(
                    color = GeneratedColor.FigmaE5e7eb,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    SettingScreen(
        onBack = {},
        onExpiryReminderClick = {},
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
