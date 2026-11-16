package com.lazy_durtles.wk.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.lazy_durtles.wk.ui.theme.LazyDurtlesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compose UI tests for the settings area.
 *
 * Verifies that the theme wrapper renders correctly around settings-like content.
 * Full SettingsScreen testing requires extensive GlobalSettings mocking.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = com.lazy_durtles.wk.test.TestApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsArea_rendersInsideTheme() {
        composeTestRule.setContent {
            LazyDurtlesTheme {
                Text("Settings")
            }
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }
}
