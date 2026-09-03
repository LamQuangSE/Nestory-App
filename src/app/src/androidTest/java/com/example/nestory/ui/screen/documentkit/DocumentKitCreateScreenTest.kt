package com.example.nestory.ui.screen.documentkit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
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

class DocumentKitCreateScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun kitCreate_blankNameAndBlankDate_showsBothErrorsAndDoesNotSubmit() {
        var submitted = false
        composeRule.setContent {
            NestoryTheme {
                DocumentKitCreateScreen(
                    onBackClick = {},
                    onSubmit = { _, _, _, _, _ -> submitted = true },
                )
            }
        }

        composeRule.onNode(hasText("Tạo bộ hồ sơ mới") and hasClickAction()).performClick()

        composeRule.onNodeWithText("Tên bộ hồ sơ không được để trống").fetchSemanticsNode()
        composeRule.onNodeWithText("Vui lòng chọn ngày sử dụng").fetchSemanticsNode()
        composeRule.runOnIdle { assertFalse(submitted) }
    }

    @Test
    fun kitCreate_typingName_clearsNameErrorWhileDateErrorRemains() {
        composeRule.setContent {
            NestoryTheme {
                DocumentKitCreateScreen(
                    onBackClick = {},
                    onSubmit = { _, _, _, _, _ -> },
                )
            }
        }

        composeRule.onNode(hasText("Tạo bộ hồ sơ mới") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Tên bộ hồ sơ không được để trống").fetchSemanticsNode()

        composeRule.onNodeWithTag("kit_name_input").performTextInput("Passport Kit")

        composeRule.onNodeWithText("Tên bộ hồ sơ không được để trống").assertDoesNotExist()
        composeRule.onNodeWithText("Vui lòng chọn ngày sử dụng").fetchSemanticsNode()
    }

    @Test
    fun kitEdit_clearingName_showsNameRequiredErrorThenSubmitsAfterFix() {
        var submitted = false
        var submittedName: String? = null
        composeRule.setContent {
            NestoryTheme {
                DocumentKitCreateScreen(
                    onBackClick = {},
                    onSubmit = { name, _, _, _, _ ->
                        submitted = true
                        submittedName = name
                    },
                    initialName = "Existing Kit",
                    initialDate = "31/12/2030",
                    isEdit = true,
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithTag("kit_name_input").performTextClearance()
        composeRule.onNodeWithText("Cập nhật Kit").performClick()

        composeRule.onNodeWithText("Tên bộ hồ sơ không được để trống").fetchSemanticsNode()
        composeRule.runOnIdle { assertFalse(submitted) }

        composeRule.onNodeWithTag("kit_name_input").performTextInput("Renamed Kit")
        composeRule.onNodeWithText("Tên bộ hồ sơ không được để trống").assertDoesNotExist()
        composeRule.onNodeWithText("Cập nhật Kit").performClick()

        composeRule.runOnIdle {
            assertTrue(submitted)
            assertEquals("Renamed Kit", submittedName)
        }
    }

    @Test
    fun kitEdit_duplicateName_showsDuplicateErrorAndBlocksSubmit() {
        var submitted = false
        composeRule.setContent {
            NestoryTheme {
                DocumentKitCreateScreen(
                    onBackClick = {},
                    onSubmit = { _, _, _, _, _ -> submitted = true },
                    initialName = "Duplicate Kit",
                    initialDate = "31/12/2030",
                    isEdit = true,
                    onDelete = {},
                    existingNames = listOf("Duplicate Kit"),
                )
            }
        }

        composeRule.onNodeWithText("Cập nhật Kit").performClick()

        composeRule.onNodeWithText("Tên bộ hồ sơ đã tồn tại").fetchSemanticsNode()
        composeRule.onNodeWithText("Tên bộ hồ sơ không được để trống").assertDoesNotExist()
        composeRule.runOnIdle { assertFalse(submitted) }
    }

    @Test
    fun kitEdit_validInput_submitsEditedValues() {
        var submittedName: String? = null
        var submittedDate: String? = null
        var submittedCategory: String? = null
        var submittedDescription: String? = null
        composeRule.setContent {
            NestoryTheme {
                DocumentKitCreateScreen(
                    onBackClick = {},
                    onSubmit = { name, date, category, description, _ ->
                        submittedName = name
                        submittedDate = date
                        submittedCategory = category
                        submittedDescription = description
                    },
                    initialName = "Original Kit",
                    initialDate = "31/12/2030",
                    initialDescription = "Mô tả",
                    isEdit = true,
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithTag("kit_name_input").performTextClearance()
        composeRule.onNodeWithTag("kit_name_input").performTextInput("Renamed Kit")
        composeRule.onNodeWithText("Cập nhật Kit").performClick()

        composeRule.runOnIdle {
            assertEquals("Renamed Kit", submittedName)
            assertEquals("31/12/2030", submittedDate)
            assertEquals("Khác", submittedCategory)
            assertEquals("Mô tả", submittedDescription)
        }
    }
}
