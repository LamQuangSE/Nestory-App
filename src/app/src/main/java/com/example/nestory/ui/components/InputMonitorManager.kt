package com.example.nestory.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity

@Stable
class InputMonitorState {
    var text by mutableStateOf("")
    var label by mutableStateOf("")
    var isFieldFocused by mutableStateOf(false)
    
    fun show(text: String, label: String) {
        this.text = text
        this.label = label
        this.isFieldFocused = true
    }
    
    fun update(text: String) {
        this.text = text
    }
    
    fun hide() {
        this.isFieldFocused = false
    }

    @Composable
    fun isVisible(): Boolean {
        val density = LocalDensity.current
        // Lấy bottom padding của bàn phím, nếu > 0 tức là bàn phím đang mở
        val bottomPadding = WindowInsets.ime.getBottom(density)
        return isFieldFocused && bottomPadding > 0
    }
}

val LocalInputMonitor = staticCompositionLocalOf { InputMonitorState() }
