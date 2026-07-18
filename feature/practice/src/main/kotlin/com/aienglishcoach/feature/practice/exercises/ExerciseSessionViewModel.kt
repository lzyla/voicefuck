package com.aienglishcoach.feature.practice.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.onFailure
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.usecase.exercise.ExerciseOutcome
import com.aienglishcoach.core.domain.usecase.exercise.GenerateExercisesUseCase
import com.aienglishcoach.core.domain.usecase.exercise.GetDueExercisesUseCase
import com.aienglishcoach.core.domain.usecase.exercise.SubmitExerciseAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives one exercise revision session: loads due exercises (generating fresh
 * ones from unresolved errors when nothing is due), presents them one at a
 * time, grades answers and finishes with an X/Y summary.
 */
@HiltViewModel
class ExerciseSessionViewModel @Inject constructor(
    private val getDueExercises: GetDueExercisesUseCase,
    private val generateExercises: GenerateExercisesUseCase,
    private val submitExerciseAnswer: SubmitExerciseAnswerUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseSessionUiState())
    val uiState: StateFlow<ExerciseSessionUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    /** Loads due exercises; when nothing is due, tries to generate new ones. */
    fun loadSession() {
        viewModelScope.launch {
            _uiState.update { ExerciseSessionUiState(isLoading = true) }
            var due = getDueExercises()
            if (due.isEmpty()) {
                generateExercises()
                    .onFailure { error -> _uiState.update { it.copy(error = error) } }
                due = getDueExercises()
            }
            _uiState.update { it.copy(isLoading = false, exercises = due) }
        }
    }

    fun onAnswerChanged(value: String) {
        _uiState.update { it.copy(answer = value) }
    }

    /** Submits the currently typed answer (FILL_GAP, TRANSLATION, SPEAKING). */
    fun submitAnswer() {
        submit(_uiState.value.answer)
    }

    /** Submits a tapped option (MULTIPLE_CHOICE). */
    fun selectOption(option: String) {
        _uiState.update { it.copy(answer = option) }
        submit(option)
    }

    private fun submit(answer: String) {
        val state = _uiState.value
        val exercise = state.currentExercise ?: return
        if (answer.isBlank() || state.outcome != null || state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val outcome = submitExerciseAnswer(exercise, answer)
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    outcome = outcome,
                    correctCount = it.correctCount + if (outcome.isCorrect) 1 else 0,
                )
            }
        }
    }

    /** Advances to the next exercise or ends the session after the last one. */
    fun nextExercise() {
        _uiState.update { state ->
            if (state.currentIndex >= state.exercises.lastIndex) {
                state.copy(isFinished = true, outcome = null, answer = "")
            } else {
                state.copy(
                    currentIndex = state.currentIndex + 1,
                    outcome = null,
                    answer = "",
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

/** Immutable state of the exercise session screen. */
data class ExerciseSessionUiState(
    val isLoading: Boolean = true,
    val exercises: List<Exercise> = emptyList(),
    val currentIndex: Int = 0,
    /** Answer typed (or option picked) for the current exercise. */
    val answer: String = "",
    /** Result of the current exercise once submitted; null while answering. */
    val outcome: ExerciseOutcome? = null,
    val isSubmitting: Boolean = false,
    val correctCount: Int = 0,
    val isFinished: Boolean = false,
    val error: AppError? = null,
) {
    val currentExercise: Exercise?
        get() = exercises.getOrNull(currentIndex)

    val isEmpty: Boolean
        get() = !isLoading && exercises.isEmpty()
}
