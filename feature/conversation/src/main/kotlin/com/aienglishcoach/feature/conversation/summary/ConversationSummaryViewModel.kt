package com.aienglishcoach.feature.conversation.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.onFailure
import com.aienglishcoach.core.common.result.onSuccess
import com.aienglishcoach.core.domain.model.ConversationStatus
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.core.domain.service.BackgroundScheduler
import com.aienglishcoach.core.domain.usecase.conversation.AnalyzeConversationUseCase
import com.aienglishcoach.core.domain.usecase.conversation.ObserveConversationDetailUseCase
import com.aienglishcoach.core.domain.usecase.exercise.GenerateExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Post-conversation summary. On first open it triggers the AI analysis,
 * exercise generation and schedules memory consolidation in the background.
 * Results stream from the local database, so a re-visit works offline.
 */
@HiltViewModel
class ConversationSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeConversationDetail: ObserveConversationDetailUseCase,
    private val analyzeConversation: AnalyzeConversationUseCase,
    private val generateExercises: GenerateExercisesUseCase,
    private val backgroundScheduler: BackgroundScheduler,
) : ViewModel() {

    private val conversationId: Long = checkNotNull(savedStateHandle[ARG_CONVERSATION_ID])

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private var analysisStarted = false

    init {
        observeConversationDetail(conversationId)
            .onEach { detail ->
                if (detail == null) return@onEach
                _uiState.update {
                    it.copy(
                        title = detail.conversation.title,
                        summary = detail.conversation.summary,
                        durationSeconds = detail.conversation.durationSeconds,
                        messages = detail.messages,
                        errors = detail.errors,
                        isAnalyzed = detail.conversation.status == ConversationStatus.ANALYZED,
                    )
                }
                if (!analysisStarted && detail.conversation.status == ConversationStatus.ENDED) {
                    analysisStarted = true
                    runAnalysis()
                }
            }
            .launchIn(viewModelScope)
    }

    fun retryAnalysis() = runAnalysis()

    private fun runAnalysis() {
        _uiState.update { it.copy(isAnalyzing = true, analysisError = null) }
        viewModelScope.launch {
            analyzeConversation(conversationId)
                .onSuccess {
                    // Exercises come from freshly stored errors; failures here
                    // are non-fatal (the user can generate them in Practice).
                    generateExercises()
                        .onFailure { Timber.w("Exercise generation failed: $it") }
                    backgroundScheduler.scheduleMemoryConsolidation(conversationId)
                    _uiState.update { it.copy(isAnalyzing = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isAnalyzing = false, analysisError = error) }
                }
        }
    }

    companion object {
        const val ARG_CONVERSATION_ID = "conversationId"
    }
}

data class SummaryUiState(
    val title: String = "",
    val summary: String? = null,
    val durationSeconds: Int = 0,
    val messages: List<Message> = emptyList(),
    val errors: List<UserError> = emptyList(),
    val isAnalyzed: Boolean = false,
    val isAnalyzing: Boolean = false,
    val analysisError: AppError? = null,
)
