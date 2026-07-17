package com.aienglishcoach.feature.practice.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.domain.model.ReviewGrade
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.usecase.vocabulary.GetDueVocabularyUseCase
import com.aienglishcoach.core.domain.usecase.vocabulary.ReviewVocabularyItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives one flashcard review session: loads due vocabulary, flips cards on
 * tap and applies the learner's SM-2 grade to each item.
 */
@HiltViewModel
class VocabularyReviewViewModel @Inject constructor(
    private val getDueVocabulary: GetDueVocabularyUseCase,
    private val reviewVocabularyItem: ReviewVocabularyItemUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyReviewUiState())
    val uiState: StateFlow<VocabularyReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val due = getDueVocabulary()
            _uiState.update { it.copy(isLoading = false, items = due) }
        }
    }

    /** Flips the current flashcard to reveal its translation. */
    fun revealCard() {
        _uiState.update { it.copy(isRevealed = true) }
    }

    /** Applies [grade] to the current card and advances the session. */
    fun grade(grade: ReviewGrade) {
        val state = _uiState.value
        val item = state.currentItem ?: return
        if (!state.isRevealed || state.isGrading) return

        _uiState.update { it.copy(isGrading = true) }
        viewModelScope.launch {
            try {
                reviewVocabularyItem(item, grade)
                _uiState.update {
                    if (it.currentIndex >= it.items.lastIndex) {
                        it.copy(
                            isGrading = false,
                            isRevealed = false,
                            reviewedCount = it.reviewedCount + 1,
                            isFinished = true,
                        )
                    } else {
                        it.copy(
                            isGrading = false,
                            isRevealed = false,
                            reviewedCount = it.reviewedCount + 1,
                            currentIndex = it.currentIndex + 1,
                        )
                    }
                }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(isGrading = false, error = AppError.Storage(throwable.message))
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

/** Immutable state of the flashcard review screen. */
data class VocabularyReviewUiState(
    val isLoading: Boolean = true,
    val items: List<VocabularyItem> = emptyList(),
    val currentIndex: Int = 0,
    /** True once the current card has been flipped to its translation side. */
    val isRevealed: Boolean = false,
    val isGrading: Boolean = false,
    val reviewedCount: Int = 0,
    val isFinished: Boolean = false,
    val error: AppError? = null,
) {
    val currentItem: VocabularyItem?
        get() = items.getOrNull(currentIndex)

    val isEmpty: Boolean
        get() = !isLoading && items.isEmpty()
}
