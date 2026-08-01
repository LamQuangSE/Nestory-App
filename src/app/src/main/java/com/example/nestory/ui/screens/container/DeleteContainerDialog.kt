package com.example.nestory.ui.screens.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import androidx.compose.foundation.clickable

@Composable
fun DeleteContainerDialog(
    containerName: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(367.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .clickable(enabled = false) { },
            verticalArrangement = Arrangement.spacedBy(NestorySpacing.S15)
        ) {
            Spacer(modifier = Modifier.height(NestorySpacing.S10))

            Text(
                text = "Xác nhận xóa container",
                style = NestoryTextStyles.Body20Bold.copy(fontWeight = FontWeight.SemiBold),
                color = GeneratedColor.Figma000000,
                modifier = Modifier.padding(horizontal = NestorySpacing.S15)
            )

            Divider(
                color = GeneratedColor.FigmaE5e7eb,
                thickness = 1.dp
            )

            Text(
                text = buildAnnotatedString {
                    append("Bạn có chắc chắn muốn xóa container ")
                    withStyle(SpanStyle(color = GeneratedColor.FigmaFf0000)) {
                        append(containerName)
                    }
                    append(" này không?")
                },
                style = NestoryTextStyles.Body15Medium,
                color = GeneratedColor.Figma000000,
                modifier = Modifier.padding(horizontal = NestorySpacing.S15)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NestorySpacing.S15)
                    .padding(bottom = NestorySpacing.S15),
                horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(10.dp))
                        .clickable { onConfirmDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Có",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = GeneratedColor.Figma919191
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .background(GeneratedColor.Figma1855ee, RoundedCornerShape(10.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = GeneratedColor.FigmaFfffff
                    )
                }
            }
        }
    }
}
