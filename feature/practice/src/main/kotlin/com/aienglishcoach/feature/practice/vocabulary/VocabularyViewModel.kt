package com.aienglishcoach.feature.practice.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.usecase.practice.ObservePracticeSummaryUseCase
import com.aienglishcoach.core.domain.usecase.vocabulary.ObserveVocabularyUseCase
import com.aienglishcoach.core.domain.usecase.vocabulary.SaveVocabularyItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the vocabulary list: streams all saved words with their SRS status,
 * exposes the due count for the review call-to-action and lets the learner
 * add a word manually.
 */
@HiltViewModel
class VocabularyViewModel @Inject constructor(
    observeVocabulary: ObserveVocabularyUseCase,
    observePracticeSummary: ObservePracticeSummaryUseCase,
    private val saveVocabularyItem: SaveVocabularyItemUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        combine(
            observeVocabulary(),
            observePracticeSummary(),
        ) { items, summary -> items to summary.dueVocabulary }
            .onEach { (items, dueCount) ->
                _uiState.update {
                    it.copy(isLoading = false, items = items, dueCount = dueCount)
                }
            }
            .launchIn(viewModelScope)
    }

    fun showAddDialog() {
        _uiState.update { it.copy(isAddDialogVisible = true) }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(isAddDialogVisible = false) }
    }

    /** Saves a manually entered word; blank example is stored as null. */
    fun addWord(word: String, translation: String, example: String) {
        if (word.isBlank() || translation.isBlank()) return
        _uiState.update { it.copy(isAddDialogVisible = false) }
        viewModelScope.launch {
            try {
                saveVocabularyItem(
                    word = word,
                    translation = translation,
                    example = example.takeIf { it.isNotBlank() },
                )
            } catch (throwable: Throwable) {
                _uiState.update { it.copy(error = AppError.Storage(throwable.message)) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

/** Immutable state of the vocabulary list screen. */
data class VocabularyUiState(
    val isLoading: Boolean = true,
    val items: List<VocabularyItem> = emptyList(),
    val dueCount: Int = 0,
    val isAddDialogVisible: Boolean = false,
    val error: AppError? = null,
)
