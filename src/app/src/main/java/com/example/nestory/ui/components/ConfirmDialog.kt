package com.example.nestory.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

/**
 * Shared confirmation dialog used by Kit, Item and Document screens so every
 * Edit/Delete confirmation keeps the exact same popup style.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    highlightRange: IntRange,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit = onDismiss,
    confirmLabel: String = "Có",
    dismissLabel: String = "Không",
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GeneratedColor.FigmaFfffff, RoundedCornerShape(NestorySpacing.S10))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, top = 22.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = NestoryTextStyles.Body20Bold.copy(fontWeight = FontWeight.W600),
                    color = GeneratedColor.Figma000000
                )
                Text(
                    text = highlightMessage(message, highlightRange),
                    style = NestoryTextStyles.Body15Medium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W600,
                        lineHeight = 21.sp
                    ),
                    color = GeneratedColor.Figma000000
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GeneratedColor.FigmaE5e7eb)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 17.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConfirmDialogButton(
                    text = dismissLabel,
                    containerColor = GeneratedColor.FigmaFfffff,
                    borderColor = GeneratedColor.FigmaE5e7eb,
                    textColor = GeneratedColor.Figma919191,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(NestorySpacing.S10))
                ConfirmDialogButton(
                    text = confirmLabel,
                    containerColor = GeneratedColor.Figma1855ee,
                    borderColor = null,
                    textColor = GeneratedColor.FigmaFfffff,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ConfirmDialogButton(
    text: String,
    containerColor: Color,
    borderColor: Color?,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(45.dp)
            .background(containerColor, RoundedCornerShape(NestorySpacing.S10))
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(NestorySpacing.S10))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = NestoryTextStyles.Body15Semi,
            color = textColor
        )
    }
}

private fun highlightMessage(message: String, range: IntRange): AnnotatedString = buildAnnotatedString {
    if (range.last < message.length) {
        append(message.substring(0, range.first))
        withStyle(SpanStyle(color = GeneratedColor.FigmaFf0000)) {
            append(message.substring(range))
        }
        append(message.substring(range.last + 1))
    } else {
        append(message)
    }
}
