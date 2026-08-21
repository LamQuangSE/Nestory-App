package com.example.nestory.ui.screen.documentkit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.nestory.ui.theme.NestoryTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DocumentKitItemFormScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun itemCreate_blankNameAndBlankRequiredDocs_showsBothErrorsAndDoesNotSubmit() {
        var submitted = false
        composeRule.setContent {
            NestoryTheme {
                DocumentKitItemFormScreen(
                    onBackClick = {},
                    onSubmit = { _, _, _, _ -> submitted = true },
                )
            }
        }

        composeRule.onNode(hasText("Tạo Item mới") and hasClickAction()).performClick()

        composeRule.onNodeWithText("Tên item không được để trống").fetchSemanticsNode()
        composeRule.onNodeWithText("Số giấy tờ cần liên kết không được để trống").fetchSemanticsNode()
        composeRule.runOnIdle { assertFalse(submitted) }
    }

    @Test
    fun itemCreate_validInput_submitsNameAndRequiredDocuments() {
        var submittedName: String? = null
        var submittedRequiredDocs: Int? = null
        composeRule.setContent {
            NestoryTheme {
                DocumentKitItemFormScreen(
                    onBackClick = {},
                    onSubmit = { name, _, _, requiredDocuments ->
                        submittedName = name
                        submittedRequiredDocs = requiredDocuments
                    },
                )
            }
        }

        composeRule.onNodeWithTag("item_name_input").performTextInput("Căn cước công dân")
        composeRule.onNodeWithTag("item_required_docs_input").performTextInput("2")
        composeRule.onNode(hasText("Tạo Item mới") and hasClickAction()).performClick()

        composeRule.runOnIdle {
            assertEquals("Căn cước công dân", submittedName)
            assertEquals(2, submittedRequiredDocs)
        }
    }

    @Test
    fun itemCreate_duplicateName_showsDuplicateErrorAndBlocksSubmit() {
        var submitted = false
        composeRule.setContent {
            NestoryTheme {
                DocumentKitItemFormScreen(
                    onBackClick = {},
                    onSubmit = { _, _, _, _ -> submitted = true },
                    existingNames = listOf("Hộ chiếu"),
                )
            }
        }

        composeRule.onNodeWithTag("item_name_input").performTextInput("Hộ chiếu")
        composeRule.onNodeWithTag("item_required_docs_input").performTextInput("1")
        composeRule.onNode(hasText("Tạo Item mới") and hasClickAction()).performClick()

        composeRule.onNodeWithText("Tên item đã tồn tại trong bộ hồ sơ này").fetchSemanticsNode()
        composeRule.runOnIdle { assertFalse(submitted) }
    }

    @Test
    fun itemEdit_blankName_blocksSubmitButSkipsRequiredDocsValidation() {
        var submitted = false
        composeRule.setContent {
            NestoryTheme {
                DocumentKitItemFormScreen(
                    onBackClick = {},
                    onSubmit = { _, _, _, _ -> submitted = true },
                    initialName = "Hộ chiếu",
                    isEdit = true,
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithTag("item_name_input").performTextClearance()
        composeRule.onNodeWithText("Cập nhật Item").performClick()

        composeRule.onNodeWithText("Tên item không được để trống").fetchSemanticsNode()
        composeRule.onNodeWithText("Số giấy tờ cần liên kết không được để trống").assertDoesNotExist()
        composeRule.runOnIdle { assertFalse(submitted) }
    }

    @Test
    fun itemEdit_validInput_submitsEditedValues() {
        var submittedName: String? = null
        var submittedNote: String? = null
        var submittedRequiredDocs: Int? = null
        composeRule.setContent {
            NestoryTheme {
                DocumentKitItemFormScreen(
                    onBackClick = {},
                    onSubmit = { name, _, note, requiredDocuments ->
                        submittedName = name
                        submittedNote = note
                        submittedRequiredDocs = requiredDocuments
                    },
                    initialName = "Căn cước",
                    initialNote = "Bản gốc",
                    initialRequiredDocuments = "2",
                    isEdit = true,
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithTag("item_name_input").performTextClearance()
        composeRule.onNodeWithTag("item_name_input").performTextInput("Căn cước công dân")
        composeRule.onNodeWithText("Cập nhật Item").performClick()

        composeRule.runOnIdle {
            assertEquals("Căn cước công dân", submittedName)
            assertEquals("Bản gốc", submittedNote)
            assertNull(submittedRequiredDocs)
        }
    }

    @Test
    fun itemEdit_deleteButton_opensDeleteConfirmationDialog() {
        var deleteConfirmed = false
        composeRule.setContent {
            NestoryTheme {
                DocumentKitItemFormScreen(
                    onBackClick = {},
                    onSubmit = { _, _, _, _ -> },
                    onDelete = { deleteConfirmed = true },
                    initialName = "Hộ chiếu",
                    isEdit = true,
                )
            }
        }

        composeRule.onNodeWithText("Xóa Item").performClick()
        composeRule.onNodeWithText("Xóa").performClick()

        composeRule.runOnIdle { assertTrue(deleteConfirmed) }
    }
}
