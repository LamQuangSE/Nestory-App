package com.example.nestory.ui.screen.container

import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import com.example.nestory.ui.components.LocalInputMonitor
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun ContainerItem(
    name: String,
    isSelected: Boolean = false,
    isExpanded: Boolean = false,
    hasChildren: Boolean = false,
    level: Int = 0,
    isLastChild: Boolean = false,
    showDelete: Boolean = false,
    onToggle: () -> Unit = {},
    onItemClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isSelected) GeneratedColor.FigmaF3f6ff.copy(alpha = 0.5f)
                else Color.Transparent
            )
            .padding(horizontal = NestorySpacing.S15, vertical = NestorySpacing.S10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width((level * 30).dp))

        if (level > 0) {
            Text(
                text = if (isLastChild) "└──" else "├──",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                color = GeneratedColor.Figma919191,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(NestorySpacing.S4))
        }

        if (hasChildren) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isExpanded) AppIcons.LsiconDownFilled
                        else AppIcons.LsiconRightFilled
                    ),
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(16.dp),
                    tint = GeneratedColor.Figma919191
                )
            }
            Spacer(modifier = Modifier.width(NestorySpacing.S4))
        } else {
            Spacer(modifier = Modifier.width(20.dp))
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onItemClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = AppIcons.FigmaNavFolder),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = GeneratedColor.Figma1855ee
            )
            Spacer(modifier = Modifier.width(NestorySpacing.S10))
            Text(
                text = name,
                style = NestoryTextStyles.Body15Medium,
                color = GeneratedColor.Figma000000,
            )
        }

        if (showDelete && onDeleteClick != null) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(id = AppIcons.MageTrash),
                    contentDescription = "Delete",
                    modifier = Modifier.size(24.dp),
                    tint = GeneratedColor.FigmaCf1111
                )
            }
        }
    }
}

@Composable
fun ContainerSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Tìm container"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, style = NestoryTextStyles.Body13Medium) },
        leadingIcon = {
            Icon(
                painter = painterResource( id = AppIcons.MaterialSymbolsLightSearch),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = GeneratedColor.Figma919191
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(NestorySpacing.S10),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = GeneratedColor.Figma1855ee,
            unfocusedIndicatorColor = GeneratedColor.FigmaE5e7eb
        ),
        textStyle = NestoryTextStyles.Body13Medium.copy(textAlign = TextAlign.Start),
        singleLine = true
    )
}

@Composable
fun ContainerErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NestorySpacing.S10))
            .background(GeneratedColor.FigmaFca5a5.copy(alpha = 0.25f))
            .padding(horizontal = NestorySpacing.S15, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        Text(
            text = message,
            style = NestoryTextStyles.Body13Medium,
            color = GeneratedColor.FigmaCf1111,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clickable { onDismiss() }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                color = GeneratedColor.FigmaCf1111
            )
        }
    }
}

@Composable
fun ContainerActionButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = true,
    isDashed: Boolean = false,
    isEnabled: Boolean = true,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    textStyle: TextStyle = NestoryTextStyles.Body15Medium,
    modifier: Modifier = Modifier
) {
    val effectiveBackground = backgroundColor
        ?: if (isPrimary) GeneratedColor.Figma1855ee else Color.Transparent
    val effectiveContent = contentColor
        ?: if (isPrimary) GeneratedColor.FigmaFfffff else GeneratedColor.Figma1855ee
    val borderColor = if (isEnabled) GeneratedColor.Figma1855ee else GeneratedColor.FigmaCbd5e1

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .then(
                if (isDashed) {
                    Modifier.drawDashedBorder(borderColor, 1.dp, NestorySpacing.S10)
                } else {
                    Modifier.border(1.dp, if (isPrimary) Color.Transparent else borderColor, RoundedCornerShape(NestorySpacing.S10))
                }
            )
            .clickable(enabled = isEnabled) { onClick() }
            .background(effectiveBackground, RoundedCornerShape(NestorySpacing.S10))
            .padding(horizontal = NestorySpacing.S15),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = textStyle,
            color = effectiveContent,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ContainerTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    useMonitor: Boolean = true
) {
    val monitor = LocalInputMonitor.current

    Column(verticalArrangement = Arrangement.spacedBy(NestorySpacing.S4)) {
        Text(
            text = buildAnnotatedString {
                val asteriskIndex = label.indexOf('*')
                if (asteriskIndex != -1) {
                    append(label.substring(0, asteriskIndex))
                    withStyle(SpanStyle(color = GeneratedColor.FigmaFf0000)) {
                        append("*")
                    }
                    append(label.substring(asteriskIndex + 1))
                } else {
                    append(label)
                }
            },
            style = NestoryTextStyles.Body16Bold,
            color = GeneratedColor.Figma000000
        )
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (useMonitor) monitor.update(it)
            },
            placeholder = { Text(text = placeholder, style = NestoryTextStyles.Body15Medium) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { 
                    if (it.isFocused && useMonitor) {
                        monitor.show(value, label)
                    } else if (!it.isFocused) {
                        monitor.hide()
                    }
                },
            shape = RoundedCornerShape(NestorySpacing.S10),
            isError = isError,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = if (isError) GeneratedColor.FigmaFf0000 else GeneratedColor.Figma1855ee,
                unfocusedIndicatorColor = GeneratedColor.FigmaE5e7eb,
                errorIndicatorColor = GeneratedColor.FigmaFf0000,
                disabledContainerColor = Color.Transparent,
                disabledIndicatorColor = GeneratedColor.FigmaE5e7eb
            ),
            textStyle = NestoryTextStyles.Body15Medium,
            singleLine = true
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = NestoryTextStyles.Body12Semi,
                color = GeneratedColor.FigmaFf0000
            )
        }
    }
}

@Composable
fun ContainerBreadcrumb(
    pathSegments: List<String>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = NestorySpacing.S10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pathSegments.forEachIndexed { index, segment ->
            val isActive = index == pathSegments.lastIndex
            Text(
                text = segment,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                color = if (isActive) GeneratedColor.Figma1855ee else GeneratedColor.Figma000000
            )
            if (index < pathSegments.lastIndex) {
                Icon(
                    painter = painterResource(id = AppIcons.LsiconRightFilled),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = GeneratedColor.Figma919191
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(id = AppIcons.GridiconsCross),
                contentDescription = "Close",
                modifier = Modifier.size(22.dp),
                tint = GeneratedColor.Figma919191
            )
        }
    }
}

@Composable
fun ContainerFormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    useMonitor: Boolean = true
) {
    val monitor = LocalInputMonitor.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, if (isError) GeneratedColor.FigmaFf0000 else GeneratedColor.FigmaE5e7eb, RoundedCornerShape(12.dp))
            .padding(horizontal = NestorySpacing.S15),
        contentAlignment = Alignment.CenterStart
    ) {
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (useMonitor) monitor.update(it)
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = NestoryTextStyles.Body15Medium,
                    color = GeneratedColor.Figma919191
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { 
                    if (it.isFocused && useMonitor) {
                        monitor.show(value, placeholder)
                    } else if (!it.isFocused) {
                        monitor.hide()
                    }
                },
            textStyle = NestoryTextStyles.Body15Medium.copy(color = GeneratedColor.Figma000000),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

fun Modifier.drawDashedBorder(color: Color, strokeWidth: androidx.compose.ui.unit.Dp, shape: androidx.compose.ui.unit.Dp) = this.drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset.Zero,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shape.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.0f, 3.0f), 0f)
        )
    )
}
