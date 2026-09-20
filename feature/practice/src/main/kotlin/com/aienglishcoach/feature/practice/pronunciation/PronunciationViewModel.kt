package com.aienglishcoach.feature.practice.pronunciation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.domain.model.PronunciationResult
import com.aienglishcoach.core.domain.service.SpeechEvent
import com.aienglishcoach.core.domain.service.SpeechToTextService
import com.aienglishcoach.core.domain.service.TextToSpeechService
import com.aienglishcoach.core.domain.usecase.pronunciation.EvaluatePronunciationUseCase
import com.aienglishcoach.core.domain.usecase.pronunciation.ObservePronunciationHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
 * Pronunciation trainer: shows a target phrase, records one attempt through
 * speech recognition, scores it against the expected text and keeps a history
 * of recent results.
 */
@HiltViewModel
class PronunciationViewModel @Inject constructor(
    private val speechToText: SpeechToTextService,
    private val textToSpeech: TextToSpeechService,
    private val evaluatePronunciation: EvaluatePronunciationUseCase,
    observePronunciationHistory: ObservePronunciationHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PronunciationUiState(phrase = PRACTICE_PHRASES.first()))
    val uiState: StateFlow<PronunciationUiState> = _uiState.asStateFlow()

    private var phraseIndex = 0
    private var listeningJob: Job? = null

    init {
        observePronunciationHistory(limit = HISTORY_LIMIT)
            .onEach { history -> _uiState.update { it.copy(history = history) } }
            .launchIn(viewModelScope)
    }

    /** Starts a recording attempt, or stops the ongoing one. */
    fun onMicTapped() {
        val state = _uiState.value
        when {
            state.isEvaluating -> Unit // Scoring in flight; ignore taps.
            state.isListening -> speechToText.stop()
            else -> startListening()
        }
    }

    private fun startListening() {
        listeningJob?.cancel()
        _uiState.update {
            it.copy(isListening = true, lastResult = null, error = null, voiceLevel = 0f)
        }

        listeningJob = speechToText.listen()
            .onEach { event ->
                when (event) {
                    is SpeechEvent.ReadyForSpeech -> Unit
                    is SpeechEvent.Partial -> Unit
                    is SpeechEvent.RmsChanged ->
                        _uiState.update { it.copy(voiceLevel = event.level) }
                    is SpeechEvent.Result -> evaluate(event.text, event.confidence)
                    is SpeechEvent.Error -> _uiState.update {
                        it.copy(isListening = false, voiceLevel = 0f, error = event.error)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun evaluate(recognizedText: String, confidence: Float?) {
        val expected = _uiState.value.phrase
        _uiState.update { it.copy(isListening = false, isEvaluating = true, voiceLevel = 0f) }
        viewModelScope.launch {
            try {
                val result = evaluatePronunciation(expected, recognizedText, confidence)
                _uiState.update { it.copy(isEvaluating = false, lastResult = result) }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(isEvaluating = false, error = AppError.Unknown(throwable.message))
                }
            }
        }
    }

    /** Plays the target phrase through text-to-speech. */
    fun onListenTapped() {
        viewModelScope.launch {
            val spoken = textToSpeech.speak(_uiState.value.phrase)
            if (!spoken) Timber.w("TTS failed for pronunciation phrase")
        }
    }

    /** Rotates to the next built-in practice phrase. */
    fun nextPhrase() {
        speechToText.stop()
        phraseIndex = (phraseIndex + 1) % PRACTICE_PHRASES.size
        _uiState.update {
            it.copy(
                phrase = PRACTICE_PHRASES[phraseIndex],
                lastResult = null,
                isListening = false,
                voiceLevel = 0f,
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        textToSpeech.stop()
        speechToText.stop()
        super.onCleared()
    }

    companion object {
        const val HISTORY_LIMIT = 20

        /**
         * Phrases that are notoriously hard for Polish speakers: "th" sounds,
         * the v/w contrast and short/long vowel pairs. Recent low-score words
         * from history could be mixed in later; the MVP simply rotates here.
         */
        val PRACTICE_PHRASES = listOf(
            "I think this is the third Thursday of the month",
            "The weather was very wet on Wednesday",
            "She sells seashells by the seashore",
            "Would you like some vegetables with your sandwich",
            "I usually walk to work through the village",
            "Three free throws won the whole thing",
            "This clothes shop is closed on Thursdays",
            "The world is worth watching every day",
        )
    }
}

/** Immutable state of the pronunciation practice screen. */
data class PronunciationUiState(
    /** The phrase the learner should read out loud. */
    val phrase: String,
    val isListening: Boolean = false,
    val isEvaluating: Boolean = false,
    /** Microphone input level 0..1 for the mic halo. */
    val voiceLevel: Float = 0f,
    /** Score of the most recent attempt; null before the first one. */
    val lastResult: PronunciationResult? = null,
    val history: List<PronunciationResult> = emptyList(),
    val error: AppError? = null,
)
