package com.example.nestory.ui.screen.documentkit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import kotlin.math.roundToInt

object KitItemStatus {
    const val PENDING = "PENDING"
    const val READY = "READY"
    const val MISSING = "MISSING"
    const val NEED_REVIEW = "NEED_REVIEW"
    const val EXPIRED = "EXPIRED"
}

data class KitStatusVisual(
    val label: String,
    val bgColor: Color,
    val textColor: Color,
    @DrawableRes val iconRes: Int,
    val iconTint: Color,
)

fun kitStatusVisual(status: String?): KitStatusVisual = when (status) {
    KitItemStatus.READY -> KitStatusVisual(
        label = "Sẵn sàng",
        bgColor = GeneratedColor.FigmaE0fff0,
        textColor = GeneratedColor.Figma137c23,
        iconRes = AppIcons.KitCheckCircle,
        iconTint = GeneratedColor.Figma008000,
    )
    KitItemStatus.NEED_REVIEW -> KitStatusVisual(
        label = "Cần bổ sung",
        bgColor = GeneratedColor.FigmaFff7ed,
        textColor = GeneratedColor.FigmaD97706,
        iconRes = AppIcons.KitAlert,
        iconTint = GeneratedColor.FigmaF59e0b,
    )
    KitItemStatus.EXPIRED -> KitStatusVisual(
        label = "Hết hạn",
        bgColor = GeneratedColor.FigmaFff1f2,
        textColor = GeneratedColor.FigmaDc2626,
        iconRes = AppIcons.KitClose,
        iconTint = GeneratedColor.FigmaFf0000,
    )
    else -> KitStatusVisual(
        label = "Thiếu",
        bgColor = GeneratedColor.FigmaFff1f2,
        textColor = GeneratedColor.FigmaDc2626,
        iconRes = AppIcons.KitClose,
        iconTint = GeneratedColor.FigmaFf0000,
    )
}

data class KitCategoryVisual(
    @DrawableRes val iconRes: Int,
    val boxColor: Color,
    val iconTint: Color,
)

fun kitCategoryVisual(category: String?): KitCategoryVisual {
    val c = category.orEmpty()
    return when {
        c.contains("thuê nhà") -> KitCategoryVisual(
            iconRes = AppIcons.KitHome,
            boxColor = GeneratedColor.FigmaEcfdf5,
            iconTint = GeneratedColor.Figma059669,
        )
        c.contains("xe máy") || c.contains("bảo hiểm") -> KitCategoryVisual(
            iconRes = AppIcons.KitMotorbike,
            boxColor = GeneratedColor.FigmaFff7ed,
            iconTint = GeneratedColor.FigmaEa580c,
        )
        c.contains("nhập học") -> KitCategoryVisual(
            iconRes = AppIcons.KitGraduate,
            boxColor = GeneratedColor.FigmaEff6ff,
            iconTint = GeneratedColor.Figma2563eb,
        )
        c.contains("du lịch") -> KitCategoryVisual(
            iconRes = AppIcons.KitBus,
            boxColor = GeneratedColor.FigmaF5f3ff,
            iconTint = GeneratedColor.Figma7c3aed,
        )
        c.contains("du học") || c.contains("visa") -> KitCategoryVisual(
            iconRes = AppIcons.KitGlobe,
            boxColor = GeneratedColor.FigmaF3eeff,
            iconTint = GeneratedColor.Figma522ec8,
        )
        else -> KitCategoryVisual(
            iconRes = AppIcons.KitGlobe,
            boxColor = GeneratedColor.FigmaF3eeff,
            iconTint = GeneratedColor.Figma522ec8,
        )
    }
}

fun kitItemCounts(items: List<KitItemEntity>): Pair<Int, Int> {
    val total = items.size
    val remaining = items.count {
        when (it.status) {
            KitItemStatus.READY -> false
            else -> true
        }
    }
    return total to remaining
}

fun kitProgressPercent(items: List<KitItemEntity>): Int {
    if (items.isEmpty()) return 0
    val (total, remaining) = kitItemCounts(items)
    return ((total - remaining).toFloat() / total * 100).roundToInt()
}

fun kitStatusDistribution(items: List<KitItemEntity>): List<Pair<KitStatusVisual, Int>> {
    val statuses = listOf(
        KitItemStatus.READY,
        KitItemStatus.NEED_REVIEW,
        KitItemStatus.MISSING,
    )
    return statuses.map { status ->
        kitStatusVisual(status) to items.count { it.status == status }
    }
}

@Composable
fun KitTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
            Icon(
                painter = painterResource(id = AppIcons.GlyphsArrowBold),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = GeneratedColor.Figma000000
            )
        }
        Text(
            text = title,
            style = NestoryTextStyles.Body20Bold,
            color = GeneratedColor.Figma000000,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        trailingContent?.invoke()
        if (trailingContent == null) {
            Spacer(modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
fun KitSectionHeader(
    title: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    iconTint: Color = GeneratedColor.Figma522ec8,
    iconBoxColor: Color = GeneratedColor.FigmaF3eeff,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(NestorySpacing.S10))
                .background(iconBoxColor, RoundedCornerShape(NestorySpacing.S10))
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }
        Text(
            text = title,
            style = NestoryTextStyles.Body16Bold,
            color = GeneratedColor.Figma000000
        )
    }
}

@Composable
fun KitStatusChip(
    status: String?,
    modifier: Modifier = Modifier
) {
    val visual = kitStatusVisual(status)
    Row(
        modifier = modifier
            .background(visual.bgColor, RoundedCornerShape(5.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = visual.iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = visual.iconTint
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = visual.label,
            style = NestoryTextStyles.Body12Semi,
            color = visual.textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun KitProgressRing(
    percent: Int,
    modifier: Modifier = Modifier,
    strokeColor: Color = GeneratedColor.Figma522ec8,
    strokeWidth: androidx.compose.ui.unit.Dp = 3.dp,
) {
    val startAngle = -90f
    val sweepAngle = percent.coerceIn(0, 100) / 100f * 360f
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(55.dp)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = GeneratedColor.FigmaE5e7eb,
                startAngle = startAngle,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset.Zero,
                size = size,
                style = stroke,
            )
            drawArc(
                color = strokeColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset.Zero,
                size = size,
                style = stroke,
            )
        }
        Text(
            text = "$percent%",
            style = NestoryTextStyles.Body12Semi.copy(fontWeight = FontWeight.W600),
            color = strokeColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun KitLabeledField(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 48.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NestorySpacing.S6)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.Figma000000
            )
            if (isRequired) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "*",
                    style = NestoryTextStyles.Body12Semi,
                    color = GeneratedColor.FigmaFf0000
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(5.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma000000,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = placeholder,
                    style = NestoryTextStyles.Body14Medium,
                    color = GeneratedColor.Figma919191,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
