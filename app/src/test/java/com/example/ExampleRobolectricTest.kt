package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun verifyAppIdentityAndBuildConfig() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Glassly", appName)
        assertEquals("0.01.10", BuildConfig.VERSION_NAME)
        assertEquals(110, BuildConfig.VERSION_CODE)
        assertEquals("com.glassly.app", BuildConfig.APPLICATION_ID)
    }

    @Test
    fun verifyMainTabsAndLiquidGlassNavigation() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("glassly_root_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_library").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_collections").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_search").assertIsDisplayed()

        // Navigate to Collections tab
        composeTestRule.onNodeWithTag("tab_collections").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("collections_lazy_column").assertIsDisplayed()

        // Navigate to Search tab
        composeTestRule.onNodeWithTag("tab_search").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bottom_thumb_search_field").assertIsDisplayed()

        // Navigate to Settings tab
        composeTestRule.onNodeWithTag("tab_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_lazy_column").assertIsDisplayed()
    }
}
