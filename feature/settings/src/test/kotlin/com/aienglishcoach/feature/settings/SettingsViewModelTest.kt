package com.aienglishcoach.feature.settings

import app.cash.turbine.test
import com.aienglishcoach.core.domain.model.EnglishLevel
import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.usecase.settings.ObservePreferencesUseCase
import com.aienglishcoach.core.domain.usecase.settings.SetApiKeyUseCase
import com.aienglishcoach.core.domain.usecase.settings.UpdatePreferencesUseCase
import com.aienglishcoach.core.domain.usecase.settings.WipeAllDataUseCase
import com.aienglishcoach.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Unit tests for [SettingsViewModel]. */
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesFlow = MutableStateFlow(UserPreferences())
    private val observePreferences = mockk<ObservePreferencesUseCase> {
        every { this@mockk.invoke() } returns preferencesFlow
    }
    private val updatePreferences = mockk<UpdatePreferencesUseCase>()
    private val setApiKey = mockk<SetApiKeyUseCase>()
    private val wipeAllData = mockk<WipeAllDataUseCase>()

    private fun createViewModel() = SettingsViewModel(
        observePreferences = observePreferences,
        updatePreferences = updatePreferences,
        setApiKey = setApiKey,
        wipeAllDataUseCase = wipeAllData,
    )

    @Test
    fun `saveApiKey success stores key, flags saved and clears input`() = runTest {
        coEvery { setApiKey(VALID_KEY) } returns true
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onApiKeyInputChanged(VALID_KEY)
            viewModel.saveApiKey()

            val state = expectMostRecentItem()
            assertTrue(state.apiKeySaved)
            assertFalse(state.apiKeyError)
            assertEquals("", state.apiKeyInput)
        }
        coVerify(exactly = 1) { setApiKey(VALID_KEY) }
    }

    @Test
    fun `saveApiKey failure flags error and keeps input`() = runTest {
        coEvery { setApiKey(INVALID_KEY) } returns false
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onApiKeyInputChanged(INVALID_KEY)
            viewModel.saveApiKey()

            val state = expectMostRecentItem()
            assertFalse(state.apiKeySaved)
            assertTrue(state.apiKeyError)
            assertEquals(INVALID_KEY, state.apiKeyInput)
        }
        coVerify(exactly = 1) { setApiKey(INVALID_KEY) }
    }

    @Test
    fun `onLevelSelected persists the selected level`() = runTest {
        coJustRun { updatePreferences(any()) }
        val viewModel = createViewModel()

        viewModel.onLevelSelected(EnglishLevel.C1)

        val transform = slot<(UserPreferences) -> UserPreferences>()
        coVerify(exactly = 1) { updatePreferences(capture(transform)) }
        assertEquals(
            EnglishLevel.C1,
            transform.captured(UserPreferences()).englishLevel,
        )
    }

    private companion object {
        const val VALID_KEY = "sk-test-key-1234567890abcdef"
        const val INVALID_KEY = "short"
    }
}
