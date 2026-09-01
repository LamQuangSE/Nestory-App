package com.example.nestory.ui.screen.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

enum class EditContainerState {
    Default,
    Modified,
    ValidationError
}

@Composable
fun EditContainerScreen(
    initialState: EditContainerState = EditContainerState.Default,
    initialName: String = "",
    onBackClick: () -> Unit,
    onSave: (String) -> Unit,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var containerName by remember { mutableStateOf(initialName) }
    var currentState by remember { mutableStateOf(initialState) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeneratedColor.FigmaFfffff)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Frame 83 — Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = AppIcons.GlyphsArrowBold),
                        contentDescription = "Back",
                        modifier = Modifier.size(26.dp),
                        tint = GeneratedColor.Figma000000
                    )
                }
                Text(
                    text = "Chỉnh sửa container",
                    style = NestoryTextStyles.Body20Bold,
                    color = GeneratedColor.Figma000000
                )
            }

            // "Tên container *" label
            Text(
                text = buildAnnotatedString {
                    append("Tên container ")
                    withStyle(SpanStyle(color = GeneratedColor.FigmaFf0000)) {
                        append("*")
                    }
                },
                style = NestoryTextStyles.Body16Bold,
                color = GeneratedColor.Figma000000
            )

            // Title Frame — text input field
            ContainerFormField(
                value = containerName,
                onValueChange = {
                    containerName = it
                    currentState = if (it.isEmpty()) EditContainerState.Default
                    else if (currentState == EditContainerState.ValidationError) EditContainerState.ValidationError
                    else EditContainerState.Modified
                    onDismissError()
                },
                placeholder = if (initialName.isEmpty()) "Nhập tên container" else initialName,
                isError = currentState == EditContainerState.ValidationError
            )

            if (errorMessage != null) {
                ContainerErrorBanner(
                    message = errorMessage,
                    onDismiss = onDismissError
                )
            }

            // Error message (Validation Error state only)
            if (currentState == EditContainerState.ValidationError) {
                Text(
                    text = "Tên container đã tồn tại",
                    style = NestoryTextStyles.Body13Medium,
                    color = GeneratedColor.FigmaFf0000
                )
            }

            // Help text Title Frame with icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = AppIcons.Vector),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = GeneratedColor.Figma919191
                    )
                }
                Text(
                    text = "Trong cùng một container cha, tên container con mới không được trùng với tên container con tồn tại trước đó",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = GeneratedColor.Figma919191,
                    lineHeight = 14.52.sp
                )
            }
        }

        // Frame 128 — Save button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(vertical = 6.dp)
        ) {
            val isEnabled = containerName.isNotBlank()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(
                        if (isEnabled) GeneratedColor.Figma1855ee else GeneratedColor.FigmaE5e7eb,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = isEnabled) { onSave(containerName) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lưu",
                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                    color = if (isEnabled) GeneratedColor.FigmaFfffff else GeneratedColor.Figma919191
                )
            }
        }
    }
}
