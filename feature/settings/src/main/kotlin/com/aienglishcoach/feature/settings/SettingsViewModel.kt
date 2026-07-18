package com.aienglishcoach.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.domain.model.EnglishLevel
import com.aienglishcoach.core.domain.model.LearningGoal
import com.aienglishcoach.core.domain.model.ThemeMode
import com.aienglishcoach.core.domain.model.TtsVoice
import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.usecase.settings.ObservePreferencesUseCase
import com.aienglishcoach.core.domain.usecase.settings.SetApiKeyUseCase
import com.aienglishcoach.core.domain.usecase.settings.UpdatePreferencesUseCase
import com.aienglishcoach.core.domain.usecase.settings.WipeAllDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the settings screen: streams the persisted [UserPreferences], applies
 * every change immediately and tracks the transient API-key form state.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    observePreferences: ObservePreferencesUseCase,
    private val updatePreferences: UpdatePreferencesUseCase,
    private val setApiKey: SetApiKeyUseCase,
    private val wipeAllDataUseCase: WipeAllDataUseCase,
) : ViewModel() {

    private val apiKeyState = MutableStateFlow(ApiKeyState())

    val uiState: StateFlow<SettingsUiState> = combine(
        observePreferences(),
        apiKeyState,
    ) { preferences, apiKey ->
        SettingsUiState(
            isLoading = false,
            preferences = preferences,
            apiKeyInput = apiKey.input,
            apiKeySaved = apiKey.saved,
            apiKeyError = apiKey.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SettingsUiState(),
    )

    fun onLevelSelected(level: EnglishLevel) {
        persist { it.copy(englishLevel = level) }
    }

    fun onGoalToggled(goal: LearningGoal) {
        persist {
            val goals = if (goal in it.learningGoals) {
                it.learningGoals - goal
            } else {
                it.learningGoals + goal
            }
            it.copy(learningGoals = goals)
        }
    }

    fun onDailyGoalSelected(minutes: Int) {
        persist { it.copy(dailyGoalMinutes = minutes) }
    }

    fun onVoiceSelected(voice: TtsVoice) {
        persist { it.copy(ttsVoice = voice) }
    }

    fun onSpeechRateChanged(rate: Float) {
        persist { it.copy(ttsSpeechRate = rate) }
    }

    fun onModelSelected(model: String) {
        persist { it.copy(aiModel = model) }
    }

    fun onThemeSelected(theme: ThemeMode) {
        persist { it.copy(theme = theme) }
    }

    fun onDynamicColorChanged(enabled: Boolean) {
        persist { it.copy(useDynamicColor = enabled) }
    }

    fun onRemindersChanged(enabled: Boolean) {
        persist { it.copy(revisionRemindersEnabled = enabled) }
    }

    fun onApiKeyInputChanged(value: String) {
        apiKeyState.update { it.copy(input = value, saved = false, error = false) }
    }

    /** Validates and stores the entered API key; clears the field on success. */
    fun saveApiKey() {
        viewModelScope.launch {
            val accepted = setApiKey(apiKeyState.value.input)
            apiKeyState.update {
                it.copy(
                    input = if (accepted) "" else it.input,
                    saved = accepted,
                    error = !accepted,
                )
            }
        }
    }

    /** Irreversibly erases all local user data. */
    fun wipeAllData() {
        viewModelScope.launch { wipeAllDataUseCase() }
    }

    private fun persist(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch { updatePreferences(transform) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/** Immutable UI state of the settings screen. */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val preferences: UserPreferences = UserPreferences(),
    val apiKeyInput: String = "",
    val apiKeySaved: Boolean = false,
    val apiKeyError: Boolean = false,
)

/** Transient, not-persisted state of the API-key form. */
private data class ApiKeyState(
    val input: String = "",
    val saved: Boolean = false,
    val error: Boolean = false,
)
