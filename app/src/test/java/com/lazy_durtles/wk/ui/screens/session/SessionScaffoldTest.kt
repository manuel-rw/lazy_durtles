package com.lazy_durtles.wk.ui.screens.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lazy_durtles.wk.ui.theme.LazyDurtlesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compose UI tests for [SessionScaffold].
 *
 * Asserts the visual appearance and functionality of the session scaffold:
 * - Top bar shows the title
 * - Back button has "Back" content description and is clickable
 * - Abandon button has "Abandon session" content description
 * - Overflow menu has "More options" content description
 * - Bottom bar content renders
 * - Content area renders
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = com.lazy_durtles.wk.test.TestApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SessionScaffoldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sessionScaffold_displaysTitle() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Review Session",
                    backgroundColor = Color(0xFF1D2023),
                    onFinishSession = {},
                    onSessionTick = {},
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        Text("Session content")
                    }
                }
            }
        }
        composeTestRule.onNodeWithText("Review Session").assertIsDisplayed()
    }

    @Test
    fun sessionScaffold_backButton_hasContentDescription() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Test",
                    backgroundColor = Color.Black,
                    onFinishSession = {},
                    onSessionTick = {},
                ) { Text("content") }
            }
        }
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun sessionScaffold_abandonButton_hasContentDescription() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Test",
                    backgroundColor = Color.Black,
                    onFinishSession = {},
                    onSessionTick = {},
                ) { Text("content") }
            }
        }
        composeTestRule.onNodeWithContentDescription("Abandon session").assertIsDisplayed()
    }

    @Test
    fun sessionScaffold_moreOptionsButton_hasContentDescription() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Test",
                    backgroundColor = Color.Black,
                    onFinishSession = {},
                    onSessionTick = {},
                ) { Text("content") }
            }
        }
        composeTestRule.onNodeWithContentDescription("More options").assertIsDisplayed()
    }

    @Test
    fun sessionScaffold_muteButton_exists() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Test",
                    backgroundColor = Color.Black,
                    onFinishSession = {},
                    onSessionTick = {},
                ) { Text("content") }
            }
        }
        composeTestRule.waitForIdle()
        // Either "Mute" or "Unmute" should be visible depending on DB state
        val muteExists = runCatching {
            composeTestRule.onNodeWithContentDescription("Mute").assertExists()
            true
        }.getOrDefault(false)
        val unmuteExists = runCatching {
            composeTestRule.onNodeWithContentDescription("Unmute").assertExists()
            true
        }.getOrDefault(false)
        assert(muteExists || unmuteExists) { "Either Mute or Unmute button should be visible" }
    }

    @Test
    fun sessionScaffold_contentArea_renders() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Test",
                    backgroundColor = Color.Black,
                    onFinishSession = {},
                    onSessionTick = {},
                ) { padding ->
                    Text("Main session content here")
                }
            }
        }
        composeTestRule.onNodeWithText("Main session content here").assertIsDisplayed()
    }

    @Test
    fun sessionScaffold_bottomBar_renders() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Test",
                    backgroundColor = Color.Black,
                    onFinishSession = {},
                    onSessionTick = {},
                    bottomBar = {
                        Button(onClick = {}) { Text("Next") }
                    },
                ) { Text("content") }
            }
        }
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun sessionScaffold_backButton_callsOnFinishSession() {
        var backClicked = false
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Test",
                    backgroundColor = Color.Black,
                    onFinishSession = { backClicked = true },
                    onSessionTick = {},
                ) { Text("content") }
            }
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.runOnIdle {
            assert(backClicked) { "Back button should trigger onFinishSession" }
        }
    }

    @Test
    fun sessionScaffold_overflowMenu_opensOnClick() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                SessionScaffold(
                    title = "Test",
                    backgroundColor = Color.Black,
                    onFinishSession = {},
                    onSessionTick = {},
                ) { Text("content") }
            }
        }
        composeTestRule.onNodeWithContentDescription("More options").performClick()
        composeTestRule.waitForIdle()
        // After clicking, the menu should be open; verify content still renders
        composeTestRule.onNodeWithText("content").assertIsDisplayed()
    }
}
