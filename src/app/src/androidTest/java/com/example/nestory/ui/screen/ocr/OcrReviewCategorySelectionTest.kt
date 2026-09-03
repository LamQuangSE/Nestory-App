package com.example.nestory.ui.screen.ocr

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nestory.domain.model.DocumentDraft
import com.example.nestory.ui.screen.category.CategoryUiModel
import com.example.nestory.ui.theme.NestoryTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OcrReviewCategorySelectionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectingCustomCategoryUpdatesReviewFieldWithChosenCategoryName() {
        var latestDraft = DocumentDraft(title = "Document")

        composeRule.setContent {
            NestoryTheme {
                var draft by remember { mutableStateOf(latestDraft) }
                latestDraft = draft

                OcrReviewScreen(
                    draft = draft,
                    bitmaps = emptyList(),
                    containers = emptyList(),
                    onDraftChange = {
                        draft = it
                        latestDraft = it
                    },
                    onBack = {},
                    onSave = {},
                    categorySelectionContent = { _, onConfirmSelection ->
                        Text(
                            text = "hihi",
                            modifier = Modifier.clickable {
                                onConfirmSelection(
                                    CategoryUiModel(
                                        id = "category-hihi",
                                        name = "hihi",
                                        color = Color(0xFF1855EE),
                                    ),
                                )
                            },
                        )
                    },
                )
            }
        }

        composeRule.onNodeWithText("Chọn danh mục").fetchSemanticsNode()

        composeRule.onNodeWithTag("ReviewField:Danh mục").performClick()
        composeRule.onNodeWithText("hihi").performClick()

        composeRule.onNodeWithText("hihi").fetchSemanticsNode()
        composeRule.runOnIdle {
            assertEquals("category-hihi", latestDraft.categoryId)
            assertEquals("hihi", latestDraft.categoryName)
        }
    }
}
