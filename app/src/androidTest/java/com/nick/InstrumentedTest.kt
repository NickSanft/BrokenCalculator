package com.nick

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalculatorInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testUnlockAllAndReset() {
        // Initial state check
        composeTestRule.onNodeWithText("+").assertIsEnabled()
        composeTestRule.onNodeWithText("-").assertIsNotEnabled()
        composeTestRule.onNodeWithText("*").assertIsNotEnabled()
        composeTestRule.onNodeWithText("/").assertIsNotEnabled()

        // Unlock subtraction
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("=").performClick()
        composeTestRule.onNodeWithText("OK").performClick() // Dismiss dialog
        composeTestRule.onNodeWithText("-").assertIsEnabled()

        // Unlock division
        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("-").performClick()
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("=").performClick()
        composeTestRule.onNodeWithText("OK").performClick() // Dismiss dialog
        composeTestRule.onNodeWithText("/").assertIsEnabled()

        // Unlock multiplication
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("/").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithText("=").performClick()
        composeTestRule.onNodeWithText("OK").performClick() // Dismiss all unlocked dialog
        composeTestRule.onNodeWithText("*").assertIsEnabled()

        // Verify all are enabled
        composeTestRule.onNodeWithText("+").assertIsEnabled()
        composeTestRule.onNodeWithText("-").assertIsEnabled()
        composeTestRule.onNodeWithText("*").assertIsEnabled()
        composeTestRule.onNodeWithText("/").assertIsEnabled()
        
        // Open hints and reset
        composeTestRule.onNodeWithContentDescription("Hints").performClick()
        composeTestRule.onNodeWithText("Reset").performClick()

        // Verify operations are reset
        composeTestRule.onNodeWithText("+").assertIsEnabled()
        composeTestRule.onNodeWithText("-").assertIsNotEnabled()
        composeTestRule.onNodeWithText("*").assertIsNotEnabled()
        composeTestRule.onNodeWithText("/").assertIsNotEnabled()
    }
}
