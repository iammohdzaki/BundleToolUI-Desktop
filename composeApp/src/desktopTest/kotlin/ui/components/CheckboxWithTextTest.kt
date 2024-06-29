package ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import utils.TestTags

class CheckboxWithTextTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `check checkbox toggle and label text`() {
        rule.setContent {
            var checkedState by remember { mutableStateOf(false) }
            CheckboxWithText(
                "Checkbox Label with Text",
                checkedState,
                onCheckedChange = {
                    checkedState = it
                },
                toolTipText = "Test Tooltip"
            )
        }

        // Check For Label Text
        rule.onNodeWithTag(TestTags.TEXT_TAG).assertTextEquals("Checkbox Label with Text")

        // Verify checkbox toggle
        rule.onNodeWithTag(TestTags.CHECKBOX_TAG).performClick()
        rule.onNodeWithTag(TestTags.CHECKBOX_TAG).assertIsOn()
    }

    @Test
    fun `check checkbox toggle functionality`() {
        rule.setContent {
            var checkedState by remember { mutableStateOf(false) }
            CheckboxWithText(
                "Checkbox Label with Text",
                checkedState,
                onCheckedChange = {
                    checkedState = it
                }
            )
        }

        // Verify checkbox toggle
        val checkbox = rule.onNodeWithTag(TestTags.CHECKBOX_TAG)
        checkbox.performClick()
        checkbox.assertIsOn()
        checkbox.performClick()
        checkbox.assertIsOff()
    }
}