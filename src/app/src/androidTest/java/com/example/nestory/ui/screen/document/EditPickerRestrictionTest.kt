package com.example.nestory.ui.screen.document

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.ui.screen.category.CategoryMode
import com.example.nestory.ui.screen.category.CategoryScreen
import com.example.nestory.ui.screen.category.CategoryUiModel
import com.example.nestory.ui.screen.category.CategoryUiState
import com.example.nestory.ui.screen.container.ContainerSelectionScreen
import com.example.nestory.ui.screen.container.ContainerUiState
import com.example.nestory.ui.theme.NestoryTheme
import org.junit.Rule
import org.junit.Test

class EditPickerRestrictionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun containerPicker_selectionOnlyWithoutCreate_hidesCreateAndEditActions() {
        composeRule.setContent {
            NestoryTheme {
                ContainerSelectionScreen(
                    uiState = ContainerUiState(
                        allContainers = listOf(ContainerEntity(id = 1L, name = "Kệ A")),
                    ),
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onSelectContainer = {},
                    onToggleContainer = {},
                    onCreateClick = {},
                    onEditClick = {},
                    onConfirmClick = {},
                    onDeleteClick = {},
                    onCloseBreadcrumb = {},
                    onBackClick = {},
                    selectionOnly = true,
                    allowCreate = false,
                )
            }
        }

        composeRule.onNodeWithText("Tạo container mới").assertDoesNotExist()
        composeRule.onNodeWithText("Chỉnh sửa container").assertDoesNotExist()
        composeRule.onNodeWithText("Xác nhận vị trí").fetchSemanticsNode()
        composeRule.onNodeWithText("Kệ A").fetchSemanticsNode()
    }

    @Test
    fun containerPicker_selectionOnlyWithCreate_stillShowsCreateButton() {
        composeRule.setContent {
            NestoryTheme {
                ContainerSelectionScreen(
                    uiState = ContainerUiState(
                        allContainers = listOf(ContainerEntity(id = 1L, name = "Kệ A")),
                    ),
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onSelectContainer = {},
                    onToggleContainer = {},
                    onCreateClick = {},
                    onEditClick = {},
                    onConfirmClick = {},
                    onDeleteClick = {},
                    onCloseBreadcrumb = {},
                    onBackClick = {},
                    selectionOnly = true,
                    allowCreate = true,
                )
            }
        }

        composeRule.onNodeWithText("Tạo container mới").fetchSemanticsNode()
        composeRule.onNodeWithText("Chỉnh sửa container").assertDoesNotExist()
    }

    @Test
    fun categoryPicker_selectionOnlyWithoutCreate_hidesCreateAndEditActions() {
        composeRule.setContent {
            NestoryTheme {
                CategoryScreen(
                    uiState = CategoryUiState(
                        categories = listOf(
                            CategoryUiModel("1", "Căn cước công dân", Color(0xFF1855EE)),
                            CategoryUiModel("2", "Hộ chiếu", Color(0xFFEB6E00)),
                        ),
                        mode = CategoryMode.Selection,
                    ),
                    onEvent = {},
                    onBack = {},
                    selectionOnly = true,
                    allowCreate = false,
                )
            }
        }

        composeRule.onNodeWithText("Tạo danh mục mới").assertDoesNotExist()
        composeRule.onNodeWithText("Chỉnh sửa danh mục").assertDoesNotExist()
        composeRule.onNodeWithText("Xác nhận danh mục").fetchSemanticsNode()
        composeRule.onNodeWithText("Căn cước công dân").fetchSemanticsNode()
    }

    @Test
    fun categoryPicker_selectionOnlyWithCreate_stillShowsCreateButton() {
        composeRule.setContent {
            NestoryTheme {
                CategoryScreen(
                    uiState = CategoryUiState(
                        categories = listOf(
                            CategoryUiModel("1", "Căn cước công dân", Color(0xFF1855EE)),
                        ),
                        mode = CategoryMode.Selection,
                    ),
                    onEvent = {},
                    onBack = {},
                    selectionOnly = true,
                    allowCreate = true,
                )
            }
        }

        composeRule.onNodeWithText("Tạo danh mục mới").fetchSemanticsNode()
        composeRule.onNodeWithText("Chỉnh sửa danh mục").assertDoesNotExist()
    }
}