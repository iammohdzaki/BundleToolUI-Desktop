package ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class UiComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `WindowHeader shows title and subtitle`() {
        composeTestRule.setContent {
            WindowHeader(title = "Main Title", subTitle = "Sub title here")
        }

        composeTestRule.onNodeWithText("Main Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sub title here").assertIsDisplayed()
    }

    @Test
    fun `ClickableLinkText opens uri when clicked`() {
        val handler = object : UriHandler {
            var opened: String? = null
            override fun openUri(uri: String) {
                opened = uri
            }
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUriHandler provides handler) {
                ClickableLinkText(text = "Visit", url = "https://example.com")
            }
        }

        composeTestRule.onNodeWithText("Visit").performClick()
        assertEquals("https://example.com", handler.opened)
    }

    @Test
    fun `ButtonWithLoader shows loader when loading and text when not`() {
        // Loading state
        composeTestRule.setContent {
            ButtonWithLoader(enabled = true, isLoading = true, onClick = {})
        }
        // CircularProgressIndicator has no text; assert Convert text not present
        composeTestRule.onNodeWithText("Convert").assertDoesNotExist()

        // Not loading state
        composeTestRule.setContent {
            ButtonWithLoader(enabled = true, isLoading = false, onClick = {})
        }
        composeTestRule.onNodeWithText("Convert").assertIsDisplayed()
    }

    @Test
    fun `DottedBorderBox shows content inside`() {
        composeTestRule.setContent {
            DottedBorderBox { androidx.compose.material3.Text("Inner Content") }
        }
        composeTestRule.onNodeWithText("Inner Content").assertIsDisplayed()
    }

    @Test
    fun `FilePickerField shows placeholder when empty and value when provided`() {
        composeTestRule.setContent {
            FilePickerField(
                label = "AAB",
                value = "",
                placeholder = "Select AAB",
                onPick = {}
            )
        }
        composeTestRule.onNodeWithText("Select AAB").assertIsDisplayed()

        composeTestRule.setContent {
            FilePickerField(
                label = "AAB",
                value = "C:/path/app.aab",
                placeholder = "Select AAB",
                onPick = {}
            )
        }
        composeTestRule.onNodeWithText("C:/path/app.aab").assertIsDisplayed()
    }

    @Test
    fun `LogBox shows waiting message when log blank and lines when present`() {
        composeTestRule.setContent {
            LogBox(log = "", onClearLogs = null)
        }
        composeTestRule.onNodeWithText("Waiting to start conversion...").assertIsDisplayed()

        val log = "✅ Success\n> running...\n❌ Error occurred"
        composeTestRule.setContent {
            LogBox(log = log, onClearLogs = null)
        }
        composeTestRule.onNodeWithText("✅ Success").assertIsDisplayed()
        composeTestRule.onNodeWithText("> running...").assertIsDisplayed()
        composeTestRule.onNodeWithText("❌ Error occurred").assertIsDisplayed()
    }

    @Test
    fun `OptionSelector changes selection when clicked`() {
        val options = listOf("One", "Two")
        val selected = mutableStateOf("One")

        composeTestRule.setContent {
            OptionSelector(
                title = "",
                options = options,
                selected = selected.value,
                onSelect = { selected.value = it },
                optionLabel = { it }
            )
        }

        // Initially selected 'One' should be visible
        composeTestRule.onNodeWithText("One").assertIsDisplayed()

        // Click on 'Two' and assert selection updated
        composeTestRule.onNodeWithText("Two").performClick()
        // Recompose - now selected.value should be 'Two'
        // Render OptionSelector again with updated state
        composeTestRule.setContent {
            OptionSelector(
                title = "",
                options = options,
                selected = selected.value,
                onSelect = { selected.value = it },
                optionLabel = { it }
            )
        }
        composeTestRule.onNodeWithText("Two").assertIsDisplayed()
    }
}
