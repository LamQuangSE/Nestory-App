package com.example.nestory.ui.screen.category

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nestory.ui.theme.NestoryTheme
import org.junit.Rule
import org.junit.Test

class CategoryFormUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun createMode_blankFormNameError_isRendered() {
        composeRule.setContent {
            NestoryTheme {
                CategoryScreen(
                    uiState = CategoryUiState(
                        mode = CategoryMode.Create,
                        form = CategoryFormUiState(
                            selectedColor = Color(0xFF1855EE),
                            nameError = "Tên giấy tờ không được để trống",
                        ),
                    ),
                    onEvent = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Tên giấy tờ không được để trống").fetchSemanticsNode()
        composeRule.onNodeWithText("Màu sắc đã tồn tại").assertDoesNotExist()
    }

    @Test
    fun createMode_duplicateColorError_isRendered() {
        composeRule.setContent {
            NestoryTheme {
                CategoryScreen(
                    uiState = CategoryUiState(
                        mode = CategoryMode.Create,
                        form = CategoryFormUiState(
                            name = "Thứ hai",
                            selectedColor = Color(0xFF1855EE),
                            colorError = "Màu sắc đã tồn tại",
                        ),
                    ),
                    onEvent = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Màu sắc đã tồn tại").fetchSemanticsNode()
        composeRule.onNodeWithText("Tên giấy tờ không được để trống").assertDoesNotExist()
    }

    @Test
    fun createMode_validForm_rendersNoErrors() {
        composeRule.setContent {
            NestoryTheme {
                CategoryScreen(
                    uiState = CategoryUiState(
                        mode = CategoryMode.Create,
                        form = CategoryFormUiState(
                            name = "Hợp đồng lao động",
                            selectedColor = Color(0xFF1855EE),
                        ),
                    ),
                    onEvent = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Tên giấy tờ không được để trống").assertDoesNotExist()
        composeRule.onNodeWithText("Màu sắc đã tồn tại").assertDoesNotExist()
        composeRule.onNodeWithText("Hợp đồng lao động").fetchSemanticsNode()
    }
}