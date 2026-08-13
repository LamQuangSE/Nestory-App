package com.example.nestory.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.navigation.NestoryDestination
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun NestoryBottomBar(
    currentDestination: NestoryDestination,
    onNavigate: (NestoryDestination) -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp) // As per Navigation Frame in Figma specs
            .background(GeneratedColor.FigmaFfffff)
            .border(1.dp, GeneratedColor.FigmaE5e7eb)
            .padding(horizontal = NestorySpacing.S16),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = AppIcons.FigmaNavHome,
            label = "Trang chủ",
            selected = currentDestination == NestoryDestination.Home,
            onClick = { onNavigate(NestoryDestination.Home) }
        )
        BottomNavItem(
            icon = if (currentDestination == NestoryDestination.DocumentSelection) AppIcons.NavDocumentEnable else AppIcons.FigmaNavDocument,
            label = "Giấy tờ",
            selected = currentDestination == NestoryDestination.DocumentSelection,
            onClick = { onNavigate(NestoryDestination.DocumentSelection) }
        )
        
        // Scan Button (Center)
        Column(
            modifier = Modifier.clickable(onClick = onScanClick),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GeneratedColor.FigmaFfffff),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(AppIcons.FigmaNavAdd),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = "Scan",
                color = GeneratedColor.Figma1a60e2,
                style = NestoryTextStyles.Body12Bold,
                fontWeight = FontWeight.W600
            )
        }

        BottomNavItem(
            icon = if (currentDestination == NestoryDestination.DocumentKit) AppIcons.NavDocumentEnable else AppIcons.FigmaNavFolder,
            label = "Bộ hồ sơ",
            selected = currentDestination == NestoryDestination.DocumentKit,
            onClick = { onNavigate(NestoryDestination.DocumentKit) }
        )
        BottomNavItem(
            icon = AppIcons.FigmaNavSettings,
            label = "Cài đặt",
            selected = currentDestination == NestoryDestination.Settings,
            onClick = { onNavigate(NestoryDestination.Settings) }
        )
    }
}

@Composable
private fun BottomNavItem(
    @androidx.annotation.DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) GeneratedColor.Figma1a60e2 else GeneratedColor.Figma919191
    
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            contentScale = ContentScale.Fit,
            colorFilter = if (icon == AppIcons.NavDocumentEnable) null else androidx.compose.ui.graphics.ColorFilter.tint(color)
        )
        Text(
            text = label,
            color = color,
            style = NestoryTextStyles.Body12Bold,
            fontWeight = FontWeight.W600,
            textAlign = TextAlign.Center
        )
    }
}
