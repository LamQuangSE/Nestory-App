package com.example.nestory.ui.screens.unlock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestory.ui.components.BackTextButton
import com.example.nestory.ui.components.NestoryScreen
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestoryRadius
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import kotlinx.coroutines.delay

@Composable
fun UnlockPinScreen(
    onBack: () -> Unit,
    onForgotPin: () -> Unit,
    onUnlocked: () -> Unit
) {
    var pin by remember { mutableStateOf("12") }
    var complete by remember { mutableStateOf(false) }

    LaunchedEffect(complete) {
        if (complete) {
            delay(260)
            onUnlocked()
        }
    }

    NestoryScreen(
        verticalPadding = NestorySpacing.S18,
        useStatusBarPadding = true
    ) {
        BackTextButton(onClick = onBack)
        Spacer(modifier = Modifier.height(NestorySpacing.S20))
        Text(
            text = "Nhập mã PIN",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma000000,
            style = NestoryTextStyles.Display35,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S14))
        Text(
            text = "Nhập mã PIN của bạn để mở\nkhoá kho lưu trữ",
            modifier = Modifier.fillMaxWidth(),
            color = GeneratedColor.Figma717171,
            style = NestoryTextStyles.Title21Semi,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S30))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S24, Alignment.CenterHorizontally)
        ) {
            repeat(4) { index ->
                val filled = index < pin.length
                val dotSize by animateDpAsState(
                    targetValue = if (filled) 20.dp else 18.dp,
                    animationSpec = tween(140),
                    label = "PinDotSize"
                )
                val dotColor by animateColorAsState(
                    targetValue = if (filled) GeneratedColor.Figma000000 else GeneratedColor.FigmaE5e7eb,
                    animationSpec = tween(140),
                    label = "PinDotColor"
                )
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(NestorySpacing.S40 + NestorySpacing.S10))
        Text(
            text = "Quên mã PIN?",
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onForgotPin),
            color = GeneratedColor.Figma1a60e2,
            style = NestoryTextStyles.Body20Medium,
            fontWeight = FontWeight.W600,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S40))
        PinPad(
            onDigit = { digit ->
                if (pin.length < 4) {
                    pin += digit
                    if (pin.length == 4) complete = true
                }
            },
            onBackspace = {
                if (pin.isNotEmpty()) pin = pin.dropLast(1)
            }
        )
    }
}

@Composable
private fun PinPad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            row.forEach { value ->
                if (value.isEmpty()) {
                    Spacer(
                        modifier = Modifier
                            .width(93.dp)
                            .height(60.dp)
                    )
                    return@forEach
                }
                Box(
                    modifier = Modifier
                        .width(93.dp)
                        .height(60.dp)
                        .clip(NestoryRadius.R10)
                        .background(if (value == "⌫") GeneratedColor.FigmaEde9fe else GeneratedColor.FigmaFfffff)
                        .border(
                            width = if (value == "⌫") 0.dp else 1.dp,
                            color = GeneratedColor.Figma000000,
                            shape = NestoryRadius.R10
                        )
                        .clickable {
                            if (value == "⌫") onBackspace() else onDigit(value)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value,
                        color = GeneratedColor.Figma37393b,
                        style = NestoryTextStyles.Body20Medium,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
    }
}
