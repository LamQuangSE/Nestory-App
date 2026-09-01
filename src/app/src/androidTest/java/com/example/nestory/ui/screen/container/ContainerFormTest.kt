package com.example.nestory.ui.screen.container

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.nestory.ui.theme.NestoryTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ContainerFormTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun createContainer_blankName_showsErrorAndDoesNotCreate() {
        var created: String? = null
        composeRule.setContent {
            NestoryTheme {
                CreateContainerScreen(
                    onBackClick = {},
                    onCreate = { created = it },
                )
            }
        }

        composeRule.onNodeWithText("Tạo container mới").performClick()

        composeRule.onNodeWithText("Tên container không được để trống").assertIsDisplayed()
        composeRule.runOnIdle { assertTrue(created == null) }
    }

    @Test
    fun createContainer_typingName_clearsErrorAndSubmits() {
        var created: String? = null
        composeRule.setContent {
            NestoryTheme {
                CreateContainerScreen(
                    onBackClick = {},
                    onCreate = { created = it },
                )
            }
        }

        composeRule.onNodeWithText("Tạo container mới").performClick()
        composeRule.onNodeWithText("Tên container không được để trống").assertIsDisplayed()

        composeRule.onNode(hasSetTextAction()).performTextInput("Hồ sơ gia đình")
        composeRule.onNodeWithText("Tên container không được để trống").assertDoesNotExist()
        composeRule.onNodeWithText("Tạo container mới").performClick()

        composeRule.runOnIdle { assertEquals("Hồ sơ gia đình", created) }
    }

    @Test
    fun createContainer_duplicateErrorMessage_showsBanner() {
        composeRule.setContent {
            NestoryTheme {
                CreateContainerScreen(
                    onBackClick = {},
                    onCreate = {},
                    errorMessage = "Tên container đã tồn tại",
                )
            }
        }

        composeRule.onNodeWithText("Tên container đã tồn tại").assertIsDisplayed()
    }

    @Test
    fun editContainer_blankName_saveDisabled() {
        var saved: String? = null
        composeRule.setContent {
            NestoryTheme {
                EditContainerScreen(
                    onBackClick = {},
                    onSave = { saved = it },
                )
            }
        }

        composeRule.onNodeWithText("Lưu").assertIsNotEnabled()
        composeRule.runOnIdle { assertFalse(saved != null) }
    }

    @Test
    fun editContainer_typingName_enablesSaveAndSaves() {
        var saved: String? = null
        composeRule.setContent {
            NestoryTheme {
                EditContainerScreen(
                    initialState = EditContainerState.Modified,
                    initialName = "Cũ",
                    onBackClick = {},
                    onSave = { saved = it },
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.onNode(hasSetTextAction()).performTextInput("Mới")
        composeRule.onNodeWithText("Lưu").assertIsEnabled()
        composeRule.onNodeWithText("Lưu").performClick()

        composeRule.runOnIdle { assertEquals("Mới", saved) }
    }

    @Test
    fun editContainer_validationErrorState_showsDuplicateNameError() {
        composeRule.setContent {
            NestoryTheme {
                EditContainerScreen(
                    initialState = EditContainerState.ValidationError,
                    initialName = "Trùng tên",
                    onBackClick = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("Tên container đã tồn tại").assertIsDisplayed()
    }
}