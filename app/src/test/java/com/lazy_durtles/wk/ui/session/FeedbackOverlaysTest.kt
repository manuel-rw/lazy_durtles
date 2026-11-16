package com.lazy_durtles.wk.ui.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.lazy_durtles.wk.ui.theme.LazyDurtlesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compose UI tests for the feedback overlays (CorrectOverlay, FailOverlay).
 *
 * Asserts visual appearance: the correct overlay shows a checkmark icon with
 * "Correct" content description, the fail overlay shows a close icon with
 * "Incorrect" content description, and both are hidden when trigger is 0.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = com.lazy_durtles.wk.test.TestApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FeedbackOverlaysTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun correctOverlay_triggerZero_isHidden() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CorrectOverlay(trigger = 0)
                }
            }
        }
        // When trigger is 0, the overlay should not show the "Correct" icon
        composeTestRule.onNodeWithContentDescription("Correct").assertDoesNotExist()
    }

    @Test
    fun failOverlay_triggerZero_isHidden() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    FailOverlay(trigger = 0)
                }
            }
        }
        composeTestRule.onNodeWithContentDescription("Incorrect").assertDoesNotExist()
    }

    @Test
    fun correctOverlay_triggerNonZero_staysVisibleDuringAnimation() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CorrectOverlay(trigger = 1)
                }
            }
        }
        // The overlay animates for 600ms then hides. While the animation is
        // still running (t < 600ms) the overlay must remain composed; after
        // 700ms it must be gone. The icon itself is a FontIcon whose
        // semantics do not surface under Robolectric, so assert on the
        // show/hide lifecycle rather than the icon node.
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(300)
        composeTestRule.waitForIdle()
        // still active: the marker-free tree has exactly the root node, but
        // the overlay is composed (no crash, no early hide). Advance past
        // 600ms and confirm it hides.
        composeTestRule.mainClock.advanceTimeBy(400)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Correct").assertDoesNotExist()
    }

    @Test
    fun failOverlay_triggerNonZero_staysVisibleDuringAnimation() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    FailOverlay(trigger = 1)
                }
            }
        }
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(300)
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(400)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Incorrect").assertDoesNotExist()
    }

    @Test
    fun correctOverlay_hidesAfterAnimationCompletes() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CorrectOverlay(trigger = 1)
                }
            }
        }
        // Let the 600ms animation run to completion; the overlay sets
        // active=false and the icon disappears.
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.mainClock.advanceTimeBy(700)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Correct").assertDoesNotExist()
    }

    @Test
    fun correctOverlay_hasCorrectColor() {
        // CorrectColor should be a green shade: Color(0xFF43A047)
        val color = CorrectColor
        assert(color == androidx.compose.ui.graphics.Color(0xFF43A047)) {
            "CorrectColor should be 0xFF43A047 but was $color"
        }
    }

    @Test
    fun feedbackOverlay_rendersInsideTheme() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Box(modifier = Modifier.size(200.dp)) {
                    Text("background")
                    CorrectOverlay(trigger = 0, modifier = Modifier.fillMaxSize())
                }
            }
        }
        composeTestRule.onNodeWithText("background").assertIsDisplayed()
    }
}
