package com.aienglishcoach.feature.learningpath

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Drives one lesson's flow: flashcards -> exercise -> completion. */
@HiltViewModel
class LessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progressStore: LearningPathProgressStore,
) : ViewModel() {

    val lesson: Lesson = LEARNING_PATH
        .flatMap { it.lessons }
        .first { it.id == savedStateHandle.get<String>(ARG_LESSON_ID) }

    private val _uiState = MutableStateFlow(
        LessonUiState(
            phase = if (lesson.flashcards.isEmpty()) LessonPhase.EXERCISE else LessonPhase.FLASHCARDS,
        ),
    )
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    fun nextFlashcard() {
        _uiState.update { state ->
            val next = state.flashcardIndex + 1
            if (next >= lesson.flashcards.size) {
                state.copy(phase = LessonPhase.EXERCISE)
            } else {
                state.copy(flashcardIndex = next)
            }
        }
    }

    fun answerExercise(questionIndex: Int, optionIndex: Int) {
        _uiState.update { it.copy(exerciseAnswers = it.exerciseAnswers + (questionIndex to optionIndex)) }
    }

    fun finishExercise() {
        val state = _uiState.value
        val correctCount = lesson.exercise.indices.count { index ->
            state.exerciseAnswers[index] == lesson.exercise[index].correctOptionIndex
        }
        progressStore.markCompleted(lesson.id, lesson.xpReward)
        _uiState.update { it.copy(phase = LessonPhase.COMPLETE, exerciseCorrectCount = correctCount) }
    }

    companion object {
        const val ARG_LESSON_ID = "lessonId"
    }
}

enum class LessonPhase { FLASHCARDS, EXERCISE, COMPLETE }

data class LessonUiState(
    val phase: LessonPhase,
    val flashcardIndex: Int = 0,
    val exerciseAnswers: Map<Int, Int> = emptyMap(),
    val exerciseCorrectCount: Int = 0,
)
