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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import com.example.nestory.ui.components.GlobalInputMonitor
import com.example.nestory.ui.components.InputMonitorState
import com.example.nestory.ui.components.LocalInputMonitor

enum class CreateContainerState {
    Default,
    Filled
}

@Composable
fun CreateContainerScreen(
    initialState: CreateContainerState = CreateContainerState.Default,
    parentContainerName: String = "",
    isParentLocked: Boolean = false,
    onBackClick: () -> Unit,
    onCreate: (String) -> Unit,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var containerName by remember { mutableStateOf("") }
    var currentState by remember { mutableStateOf(initialState) }
    var showNameError by remember { mutableStateOf(false) }
    val monitorState = remember { InputMonitorState() }

    CompositionLocalProvider(LocalInputMonitor provides monitorState) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(GeneratedColor.FigmaFfffff)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = NestorySpacing.S20)
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
                    text = "Tạo container",
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

            // Title Frame — text input
            ContainerFormField(
                value = containerName,
                onValueChange = {
                    containerName = it
                    currentState = if (it.isNotEmpty()) CreateContainerState.Filled
                    else CreateContainerState.Default
                    showNameError = false
                    onDismissError()
                },
                placeholder = "Nhập tên container",
                isError = showNameError
            )

            // Error message when tapping Create with an empty name
            if (showNameError) {
                Text(
                    text = "Tên container không được để trống",
                    style = NestoryTextStyles.Body13Medium,
                    color = GeneratedColor.FigmaFf0000
                )
            }

            if (errorMessage != null) {
                ContainerErrorBanner(
                    message = errorMessage,
                    onDismiss = onDismissError
                )
            }

            // "Container cha" label
            Text(
                text = "Container cha",
                style = NestoryTextStyles.Body16Bold,
                color = GeneratedColor.Figma000000
            )

            // Title Frame — parent container selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(GeneratedColor.FigmaF3f6ff.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(12.dp))
                    .padding(horizontal = 15.dp)
                    .clickable { },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = parentContainerName,
                        style = NestoryTextStyles.Body15Medium,
                        color = GeneratedColor.Figma000000
                    )
                    Icon(
                        painter = painterResource(id = AppIcons.MaterialSymbolsLightLock),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = GeneratedColor.Figma919191
                    )
                }
            }

            // Info text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
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
                    text = "Container mới sẽ được tạo bên trong vị trí này.",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = GeneratedColor.Figma919191,
                    lineHeight = 14.52.sp
                )
            }
        }

                // Frame 128 — Action button (always solid blue, always enabled per Figma)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NestorySpacing.S20)
                        .padding(vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(GeneratedColor.Figma1855ee, RoundedCornerShape(10.dp))
                            .clickable {
                                if (containerName.isBlank()) {
                                    showNameError = true
                                } else {
                                    onCreate(containerName)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tạo container mới",
                            style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                            color = GeneratedColor.FigmaFfffff
                        )
                    }
                }
            }
            GlobalInputMonitor()
        }
    }
}
