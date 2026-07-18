package com.aienglishcoach.feature.learningpath

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LearningPathViewModel @Inject constructor(
    progressStore: LearningPathProgressStore,
) : ViewModel() {

    val uiState: StateFlow<LearningPathUiState> = combine(
        progressStore.completedLessonIds,
        progressStore.totalXp,
    ) { completedIds, totalXp ->
        val flatLessonIds = LEARNING_PATH.flatMap { it.lessons }.map { it.id }
        var previousCompleted = true
        val lessonStates = flatLessonIds.associateWith { lessonId ->
            val state = when {
                lessonId in completedIds -> LessonState.COMPLETED
                previousCompleted -> LessonState.UNLOCKED
                else -> LessonState.LOCKED
            }
            previousCompleted = lessonId in completedIds
            state
        }
        LearningPathUiState(units = LEARNING_PATH, lessonStates = lessonStates, totalXp = totalXp)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LearningPathUiState(),
    )
}

enum class LessonState { LOCKED, UNLOCKED, COMPLETED }

data class LearningPathUiState(
    val units: List<LearningUnit> = emptyList(),
    val lessonStates: Map<String, LessonState> = emptyMap(),
    val totalXp: Int = 0,
)
