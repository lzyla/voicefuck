package com.aienglishcoach.feature.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.domain.usecase.practice.ObservePracticeSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Backs the practice hub: streams due counts so the exercise and vocabulary
 * tiles can show "N do powtórki" badges.
 */
@HiltViewModel
class PracticeHubViewModel @Inject constructor(
    observePracticeSummary: ObservePracticeSummaryUseCase,
) : ViewModel() {

    val uiState: StateFlow<PracticeHubUiState> = observePracticeSummary()
        .map { summary ->
            PracticeHubUiState(
                isLoading = false,
                dueExercises = summary.dueExercises,
                dueVocabulary = summary.dueVocabulary,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = PracticeHubUiState(),
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/** Immutable state of the practice hub screen. */
data class PracticeHubUiState(
    val isLoading: Boolean = true,
    val dueExercises: Int = 0,
    val dueVocabulary: Int = 0,
)
