package com.aienglishcoach.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.domain.model.EnglishLevel
import com.aienglishcoach.core.domain.model.LearningGoal
import com.aienglishcoach.core.domain.usecase.settings.SetApiKeyUseCase
import com.aienglishcoach.core.domain.usecase.settings.UpdatePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the multi-step onboarding: welcome -> level -> goals -> permissions
 * -> API key. Selections are held in UI state and persisted once at [finish].
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val updatePreferences: UpdatePreferencesUseCase,
    private val setApiKey: SetApiKeyUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectLevel(level: EnglishLevel) {
        _uiState.update { it.copy(selectedLevel = level) }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayName = value) }
    }

    fun answerPlacementQuestion(questionIndex: Int, optionIndex: Int) {
        _uiState.update { state ->
            state.copy(placementAnswers = state.placementAnswers + (questionIndex to optionIndex))
        }
    }

    /** Scores the placement test and stores the resulting level. */
    fun finishPlacementTest() {
        val state = _uiState.value
        val correctCount = PLACEMENT_QUESTIONS.indices.count { index ->
            state.placementAnswers[index] == PLACEMENT_QUESTIONS[index].correctOptionIndex
        }
        _uiState.update {
            it.copy(
                placementCorrectCount = correctCount,
                selectedLevel = englishLevelForScore(correctCount),
            )
        }
    }

    fun toggleGoal(goal: LearningGoal) {
        _uiState.update { state ->
            val goals = if (goal in state.selectedGoals) {
                state.selectedGoals - goal
            } else {
                state.selectedGoals + goal
            }
            state.copy(selectedGoals = goals)
        }
    }

    fun selectDailyGoal(minutes: Int) {
        _uiState.update { it.copy(dailyGoalMinutes = minutes) }
    }

    fun onApiKeyChanged(value: String) {
        _uiState.update { it.copy(apiKeyInput = value, apiKeyError = false) }
    }

    /** Persists everything and completes onboarding. */
    fun finish(onCompleted: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.apiKeyInput.isNotBlank()) {
                val accepted = setApiKey(state.apiKeyInput)
                if (!accepted) {
                    _uiState.update { it.copy(apiKeyError = true) }
                    return@launch
                }
            }
            updatePreferences { preferences ->
                preferences.copy(
                    onboardingCompleted = true,
                    englishLevel = state.selectedLevel,
                    learningGoals = state.selectedGoals,
                    dailyGoalMinutes = state.dailyGoalMinutes,
                )
            }
            onCompleted()
        }
    }
}

data class OnboardingUiState(
    val displayName: String = "",
    val placementAnswers: Map<Int, Int> = emptyMap(),
    val placementCorrectCount: Int = 0,
    val selectedLevel: EnglishLevel = EnglishLevel.B1,
    val selectedGoals: Set<LearningGoal> = emptySet(),
    val dailyGoalMinutes: Int = 10,
    val apiKeyInput: String = "",
    val apiKeyError: Boolean = false,
)
