package com.aienglishcoach.core.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.aienglishcoach.core.designsystem.theme.CoachTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Compose UI test: the mic button stays clickable in every state. */
class MicButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun micButton_click_isDelivered_inEveryState() {
        val state = mutableStateOf(MicState.Idle)
        var clicks = 0

        composeTestRule.setContent {
            val current by state
            CoachTheme {
                MicButton(
                    state = current,
                    onClick = { clicks++ },
                    modifier = Modifier.testTag("mic"),
                )
            }
        }

        MicState.entries.forEach { micState ->
            state.value = micState
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("mic").performClick()
        }
        composeTestRule.waitForIdle()

        assertEquals(MicState.entries.size, clicks)
    }
}
