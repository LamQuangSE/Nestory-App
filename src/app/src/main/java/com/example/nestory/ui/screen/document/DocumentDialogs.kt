package com.example.nestory.ui.screen.document

import androidx.compose.runtime.Composable
import com.example.nestory.ui.components.ConfirmDialog

@Composable
fun ConfirmDeleteDocumentDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDialog(
        title = "Xóa giấy tờ",
        message = "Bạn có chắc chắn muốn xóa giấy tờ này không?",
        highlightRange = 22..38,
        confirmLabel = "Xóa",
        dismissLabel = "Hủy",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun ConfirmEditDocumentDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDialog(
        title = "Xác nhận dừng chỉnh sửa giấy tờ",
        message = "Bạn có muốn lưu các thay đổi và dừng chỉnh sửa giấy tờ không?",
        highlightRange = 12..27,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}