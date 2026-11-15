package com.lazy_durtles.wk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compose UI tests for [LazyDurtlesTheme].
 *
 * Asserts the visual appearance of the theme: that the MaterialTheme color scheme
 * is applied correctly for light and dark modes, that the theme provides a
 * non-default primary color (the WaniKani pink in light mode), and that
 * content inside the theme renders correctly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = com.lazy_durtles.wk.test.TestApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lazyDurtlesTheme_rendersContent() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Text("Theme test content")
            }
        }
        composeTestRule.onNodeWithText("Theme test content").assertIsDisplayed()
    }

    @Test
    fun lazyDurtlesTheme_lightMode_primaryIsWanikaniPink() {
        // In light mode (default for Robolectric), the primary color should be
        // the WaniKani pink #E80092, not the Material default.
        var capturedPrimary: Color? = null
        composeTestRule.setContent {
            LazyDurtlesTheme {
                capturedPrimary = MaterialTheme.colorScheme.primary
                Text("test")
            }
        }
        composeTestRule.runOnIdle {
            // WaniKani light primary: #E80092
            assert(capturedPrimary == Color(0xFFE80092)) {
                "Expected WaniKani pink 0xFFE80092 but got $capturedPrimary"
            }
        }
    }

    @Test
    fun lazyDurtlesTheme_lightMode_backgroundIsWhite() {
        var capturedBackground: Color? = null
        composeTestRule.setContent {
            LazyDurtlesTheme {
                capturedBackground = MaterialTheme.colorScheme.background
                Text("test")
            }
        }
        composeTestRule.runOnIdle {
            assert(capturedBackground == Color(0xFFFFFFFF)) {
                "Expected white background but got $capturedBackground"
            }
        }
    }

    @Test
    fun lazyDurtlesTheme_lightMode_onBackgroundIsBlack() {
        var capturedOnBackground: Color? = null
        composeTestRule.setContent {
            LazyDurtlesTheme {
                capturedOnBackground = MaterialTheme.colorScheme.onBackground
                Text("test")
            }
        }
        composeTestRule.runOnIdle {
            assert(capturedOnBackground == Color(0xFF000000)) {
                "Expected black onBackground but got $capturedOnBackground"
            }
        }
    }

    @Test
    fun lazyDurtlesTheme_colorScheme_hasAllRolesPopulated() {
        var scheme: androidx.compose.material3.ColorScheme? = null
        composeTestRule.setContent {
            LazyDurtlesTheme {
                scheme = MaterialTheme.colorScheme
                Text("test")
            }
        }
        composeTestRule.runOnIdle {
            val s = scheme!!
            // Verify key roles are not the Material defaults (i.e., the theme is applied)
            assert(s.primary != androidx.compose.material3.lightColorScheme().primary) {
                "Primary should differ from default light scheme"
            }
            assert(s.surface != Color.Transparent) { "Surface should not be transparent" }
            assert(s.error != Color.Transparent) { "Error should not be transparent" }
            assert(s.outline != Color.Transparent) { "Outline should not be transparent" }
        }
    }

    @Test
    fun lazyDurtlesTheme_textUsesThemeTypography() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Text("typography test", style = MaterialTheme.typography.bodyLarge)
            }
        }
        composeTestRule.onNodeWithText("typography test").assertIsDisplayed()
    }

    @Test
    fun themePreferenceState_versionStartsAtZero() {
        // ThemePreferenceState.version should be an Int that starts at 0
        // and increments on refresh()
        val initialVersion = ThemePreferenceState.version
        ThemePreferenceState.refresh()
        assert(ThemePreferenceState.version == initialVersion + 1) {
            "Version should increment after refresh()"
        }
    }
}
