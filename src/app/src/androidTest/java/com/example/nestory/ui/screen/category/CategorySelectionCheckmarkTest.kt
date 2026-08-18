package com.example.nestory.ui.screen.category

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nestory.ui.theme.NestoryTheme
import org.junit.Rule
import org.junit.Test

class CategorySelectionCheckmarkTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectedPredefinedCategory_showsCheckmarkAndNoDeleteAction() {
        var selectedId by mutableStateOf("1")
        composeRule.setContent {
            NestoryTheme {
                CategoryScreen(
                    uiState = CategoryUiState(
                        categories = listOf(
                            CategoryUiModel("1", "Học vấn", Color(0xFFFDBA74)),
                            CategoryUiModel("2", "Bảo hiểm", Color(0xFF1855EE)),
                        ),
                        mode = CategoryMode.Selection,
                        selectedCategoryId = selectedId,
                    ),
                    onEvent = { event ->
                        if (event is CategoryEvent.OnCategorySelected) selectedId = event.categoryId
                    },
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Đã chọn").fetchSemanticsNode()
        composeRule.onNodeWithContentDescription("Xóa danh mục").assertDoesNotExist()
        composeRule.onNodeWithText("Chỉnh sửa danh mục").assertIsNotEnabled()
    }

    @Test
    fun selectingAnotherCategory_movesCheckmarkAndEnablesDeleteForUserCategory() {
        var selectedId by mutableStateOf("1")
        composeRule.setContent {
            NestoryTheme {
                CategoryScreen(
                    uiState = CategoryUiState(
                        categories = listOf(
                            CategoryUiModel("1", "Học vấn", Color(0xFFFDBA74)),
                            CategoryUiModel("2", "Bảo hiểm", Color(0xFF1855EE)),
                        ),
                        mode = CategoryMode.Selection,
                        selectedCategoryId = selectedId,
                    ),
                    onEvent = { event ->
                        if (event is CategoryEvent.OnCategorySelected) selectedId = event.categoryId
                    },
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Bảo hiểm").performClick()

        composeRule
            .onNode(hasContentDescription("Đã chọn") and hasAnySibling(hasText("Bảo hiểm")))
            .fetchSemanticsNode()
        composeRule.onNodeWithContentDescription("Xóa danh mục").fetchSemanticsNode()
        composeRule.onNodeWithText("Chỉnh sửa danh mục").assertIsEnabled()
    }
}
