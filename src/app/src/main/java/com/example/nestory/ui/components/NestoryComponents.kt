package com.example.nestory.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import com.example.nestory.ui.assets.AppImages
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun NestoryScreen(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    horizontalPadding: Dp = NestorySpacing.S20,
    verticalPadding: Dp = NestorySpacing.S40,
    useStatusBarPadding: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollModifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
    val statusModifier = if (useStatusBarPadding) Modifier.statusBarsPadding() else Modifier

    Surface(
        modifier = modifier.fillMaxSize(),
        color = GeneratedColor.FigmaFfffff
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth()
                    .then(statusModifier)
                    .then(scrollModifier)
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                content = content
            )
        }
    }
}

@Composable
fun NestoryLogo(
    modifier: Modifier = Modifier,
    showName: Boolean = true,
    centered: Boolean = false
) {
    if (centered) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(AppImages.FigmaLogoLarge),
                contentDescription = null,
                modifier = Modifier.size(94.dp),
                contentScale = ContentScale.Fit
            )
            if (showName) {
                Spacer(modifier = Modifier.height(NestorySpacing.S8))
                Text(
                    text = "Nestory",
                    color = GeneratedColor.Figma000000,
                    style = NestoryTextStyles.Display35
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
        ) {
            Image(
                painter = painterResource(AppImages.FigmaLogoHeader),
                contentDescription = null,
                modifier = Modifier.size(width = 48.dp, height = 50.dp),
                contentScale = ContentScale.Fit
            )
            if (showName) {
                Text(
                    text = "Nestory",
                    color = GeneratedColor.Figma000000,
                    style = NestoryTextStyles.Title22Bold
                )
            }
        }
    }
}

@Composable
fun ShieldLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    checked: Boolean = false
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(AppImages.FigmaLogoLarge),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        if (checked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.38f)
                    .clip(CircleShape)
                    .background(GeneratedColor.Figma07bc67),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = GeneratedColor.FigmaFfffff,
                    style = NestoryTextStyles.Body16Semi,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}

@Composable
fun SmallFeatureRow(
    title: String,
    description: String,
    @DrawableRes iconRes: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(NestoryRadius.R14)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S20))
        Column {
            Text(
                text = title,
                color = GeneratedColor.Figma000000,
                style = NestoryTextStyles.Body15Semi,
                fontWeight = FontWeight.W600
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S6))
            Text(
                text = description,
                color = GeneratedColor.Figma919191,
                style = NestoryTextStyles.Body14Medium
            )
        }
    }
}

@Composable
fun CreateVaultOptionRow(
    title: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(NestoryRadius.R10)
                .background(GeneratedColor.FigmaEdebff),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S14))
        Text(
            text = title,
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Body12Semi,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes leadingIcon: Int? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = NestoryRadius.R16,
        colors = ButtonDefaults.buttonColors(
            containerColor = GeneratedColor.Figma1a60e2,
            contentColor = Color.White
        )
    ) {
        if (leadingIcon != null) {
            Image(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(NestorySpacing.S8))
        }
        Text(text = text, style = NestoryTextStyles.Body17Bold)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = NestoryRadius.R16,
        colors = ButtonDefaults.buttonColors(
            containerColor = GeneratedColor.FigmaEdebff,
            contentColor = GeneratedColor.Figma6d28d9
        )
    ) {
        Text(text = text, style = NestoryTextStyles.Body17Medium)
    }
}

@Composable
fun BackTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "‹  Trở về",
        modifier = modifier.clickable(onClick = onClick),
        color = GeneratedColor.Figma717171,
        style = NestoryTextStyles.Body15Medium,
        fontWeight = FontWeight.W600
    )
}

@Composable
fun SafeIllustration(
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    compact: Boolean = false,
    @DrawableRes imageRes: Int? = null
) {
    val boxSize = if (checked) 300.dp else if (compact) 170.dp else 280.dp
    Box(
        modifier = modifier.size(width = boxSize, height = boxSize),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                imageRes ?: if (checked) AppImages.FigmaVaultComplete else AppImages.NestoryVaultIllustration
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Body16Semi
        )
        if (action != null) {
            Text(
                text = action,
                modifier = Modifier.clickable(onClick = onAction),
                color = GeneratedColor.Figma1a60e2,
                style = NestoryTextStyles.Body13Semi
            )
        }
    }
}

@Composable
fun DocumentRow(
    name: String,
    subtitle: String,
    tag: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = NestorySpacing.S8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(NestoryRadius.R14)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tag.take(2).uppercase(),
                color = accent,
                style = NestoryTextStyles.Body12Bold,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(NestorySpacing.S14))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = GeneratedColor.Figma000000,
                style = NestoryTextStyles.Body13Semi
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S4))
            Text(
                text = subtitle,
                color = GeneratedColor.Figma919191,
                style = NestoryTextStyles.Body10Semi
            )
        }
        Text(
            text = tag,
            color = accent,
            style = NestoryTextStyles.Body12Semi
        )
    }
}

@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = NestoryRadius.R14,
        colors = CardDefaults.cardColors(containerColor = GeneratedColor.FigmaFfffff),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(NestorySpacing.S18),
            content = content
        )
    }
}
