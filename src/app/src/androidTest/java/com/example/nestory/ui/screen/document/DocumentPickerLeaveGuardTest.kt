package com.example.nestory.ui.screen.document

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nestory.ui.theme.NestoryTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DocumentPickerLeaveGuardTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun categoryPickerOpen_thenLeaveRequested_showsEditConfirmDialog() {
        var requestLeave: () -> Unit = {}
        var savedName: String? = null
        var editMode = true
        var leaveCompleted = false

        composeRule.setContent {
            NestoryTheme {
                var editLeaveRequested by remember { mutableStateOf(false) }
                var isEditMode by remember { mutableStateOf(true) }
                requestLeave = { editLeaveRequested = true }
                editMode = isEditMode

                DocumentDetailScreen(
                    document = editableUiDocument(),
                    onBack = {},
                    onSave = { name, _, _, _, _ -> savedName = name },
                    isEditMode = isEditMode,
                    onEditModeChange = { isEditMode = it },
                    editLeaveRequested = editLeaveRequested,
                    onEditLeaveComplete = { leaveCompleted = true },
                    onEditLeaveDismiss = { editLeaveRequested = false },
                )
            }
        }

        // Open the Category selection UI from edit mode.
        composeRule.onNodeWithText("Identity").performClick()
        composeRule.onNodeWithText("Chọn danh mục").fetchSemanticsNode()

        // Merely opening the picker must not confirm anything or show the alert.
        composeRule.onNodeWithText("Xác nhận dừng chỉnh sửa giấy tờ").assertDoesNotExist()

        // Trigger a bottom-navigation leave while the picker is still open.
        composeRule.runOnIdle { requestLeave() }

        // The existing leave-confirmation Alert must appear even with the picker open.
        composeRule.onNodeWithText("Xác nhận dừng chỉnh sửa giấy tờ").fetchSemanticsNode()
        composeRule.onNodeWithText("Có").performClick()

        composeRule.runOnIdle {
            assertEquals("Original Title", savedName)
            assertFalse(editMode)
            assertTrue(leaveCompleted)
        }
    }

    @Test
    fun categoryPickerOpen_leaveRequested_thenDismissed_keepsEditAndPickerOpen() {
        var requestLeave: () -> Unit = {}
        var savedName: String? = null
        var editMode = true
        var leaveDismissed = false

        composeRule.setContent {
            NestoryTheme {
                var editLeaveRequested by remember { mutableStateOf(false) }
                var isEditMode by remember { mutableStateOf(true) }
                requestLeave = { editLeaveRequested = true }
                editMode = isEditMode

                DocumentDetailScreen(
                    document = editableUiDocument(),
                    onBack = {},
                    onSave = { name, _, _, _, _ -> savedName = name },
                    isEditMode = isEditMode,
                    onEditModeChange = { isEditMode = it },
                    editLeaveRequested = editLeaveRequested,
                    onEditLeaveComplete = {},
                    onEditLeaveDismiss = {
                        editLeaveRequested = false
                        leaveDismissed = true
                    },
                )
            }
        }

        composeRule.onNodeWithText("Identity").performClick()
        composeRule.onNodeWithText("Chọn danh mục").fetchSemanticsNode()

        composeRule.runOnIdle { requestLeave() }
        composeRule.onNodeWithText("Xác nhận dừng chỉnh sửa giấy tờ").fetchSemanticsNode()
        composeRule.onNodeWithText("Không").performClick()

        composeRule.runOnIdle {
            assertNull(savedName)
            assertTrue(editMode)
            assertTrue(leaveDismissed)
        }
        composeRule.onNodeWithText("Chọn danh mục").fetchSemanticsNode()
    }

    private fun editableUiDocument(): DocumentUiModel =
        DocumentUiModel(
            id = "1",
            name = "Original Title",
            category = "Identity",
            containerPath = "Main",
            containerId = 1L,
            status = DocumentStatus.Active,
            expiryDate = "31/12/2030",
            categoryColor = Color(0xFF1855EE),
        )
}
