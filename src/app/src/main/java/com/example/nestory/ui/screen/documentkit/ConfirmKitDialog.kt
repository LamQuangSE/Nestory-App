package com.example.nestory.ui.screen.documentkit

import androidx.compose.runtime.Composable
import com.example.nestory.ui.components.ConfirmDialog

@Composable
fun ConfirmCreateKitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KitConfirmDialog(
        title = "Xác nhận tiếp tục tạo bộ hồ sơ",
        message = "Bạn có muốn tiếp tục thực hiện tạo bộ hồ sơ mới không?",
        highlightRange = 12..30,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ConfirmEditKitDialog(
    onConfirm: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit,
) {
    KitConfirmDialog(
        title = "Xác nhận dừng chỉnh sửa bộ hồ sơ",
        message = "Bạn có muốn lưu các thay đổi và dừng chỉnh sửa bộ hồ sơ không?",
        highlightRange = 12..27,
        onConfirm = onConfirm,
        onDismiss = onDiscard,
        onDismissRequest = onCancel,
    )
}

@Composable
fun ConfirmDiscardKitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KitConfirmDialog(
        title = "Huỷ tạo bộ hồ sơ",
        message = "Bạn có muốn thoát khỏi quá trình tạo bộ hồ sơ không?",
        highlightRange = 13..16,
        confirmLabel = "Thoát",
        dismissLabel = "Tiếp tục",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ConfirmDeleteKitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KitConfirmDialog(
        title = "Xóa bộ hồ sơ",
        message = "Bạn có chắc chắn muốn xóa bộ hồ sơ này không?",
        highlightRange = 22..37,
        confirmLabel = "Xóa",
        dismissLabel = "Hủy",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ConfirmCreateItemDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KitConfirmDialog(
        title = "Xác nhận tiếp tục tạo item",
        message = "Bạn có muốn tiếp tục thực hiện tạo item mới không?",
        highlightRange = 12..30,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ConfirmDiscardItemDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KitConfirmDialog(
        title = "Huỷ tạo item",
        message = "Bạn có muốn thoát khỏi quá trình tạo item không?",
        highlightRange = 33..40,
        confirmLabel = "Thoát",
        dismissLabel = "Tiếp tục",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ConfirmEditItemDialog(
    onConfirm: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit,
) {
    KitConfirmDialog(
        title = "Xác nhận dừng chỉnh sửa item",
        message = "Bạn có muốn lưu các thay đổi và dừng chỉnh sửa item không?",
        highlightRange = 12..27,
        onConfirm = onConfirm,
        onDismiss = onDiscard,
        onDismissRequest = onCancel,
    )
}

@Composable
fun ConfirmDeleteItemDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KitConfirmDialog(
        title = "Xóa item",
        message = "Bạn có chắc chắn muốn xóa item này không?",
        highlightRange = 22..37,
        confirmLabel = "Xóa",
        dismissLabel = "Hủy",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ConfirmUnlinkDocumentDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KitConfirmDialog(
        title = "Xóa giấy tờ liên kết",
        message = "Bạn có muốn xóa giấy tờ đã liên kết với item này không?",
        highlightRange = 12..30,
        confirmLabel = "Xóa",
        dismissLabel = "Hủy",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun KitConfirmDialog(
    title: String,
    message: String,
    highlightRange: IntRange,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit = onDismiss,
    confirmLabel: String = "Có",
    dismissLabel: String = "Không",
) {
    ConfirmDialog(
        title = title,
        message = message,
        highlightRange = highlightRange,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        onDismissRequest = onDismissRequest,
        confirmLabel = confirmLabel,
        dismissLabel = dismissLabel,
    )
}
