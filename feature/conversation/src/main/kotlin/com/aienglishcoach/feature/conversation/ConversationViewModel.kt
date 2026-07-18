package com.aienglishcoach.feature.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.onFailure
import com.aienglishcoach.core.common.result.onSuccess
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.service.SpeechEvent
import com.aienglishcoach.core.domain.service.SpeechToTextService
import com.aienglishcoach.core.domain.service.TextToSpeechService
import com.aienglishcoach.core.domain.usecase.conversation.EndConversationUseCase
import com.aienglishcoach.core.domain.usecase.conversation.ObserveConversationDetailUseCase
import com.aienglishcoach.core.domain.usecase.conversation.SendMessageUseCase
import com.aienglishcoach.core.domain.usecase.conversation.StartConversationUseCase
import com.aienglishcoach.core.domain.usecase.conversation.TranslateMessageUseCase
import com.aienglishcoach.core.domain.usecase.settings.ObservePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

/**
 * State machine of the live voice conversation:
 *
 * ScenarioSelection -> (start) -> Speaking(greeting) -> Idle
 * Idle -(mic tap)-> Listening -(final STT)-> Processing -(AI reply)-> Speaking -> Idle
 *
 * Tapping the mic while Speaking interrupts TTS and starts listening.
 * Every state transition is reflected in [ConversationUiState.micState].
 */
@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startConversation: StartConversationUseCase,
    private val sendMessage: SendMessageUseCase,
    private val endConversation: EndConversationUseCase,
    private val translateMessage: TranslateMessageUseCase,
    private val observeConversationDetail: ObserveConversationDetailUseCase,
    observePreferences: ObservePreferencesUseCase,
    private val speechToText: SpeechToTextService,
    private val textToSpeech: TextToSpeechService,
) : ViewModel() {

    private val navConversationId: Long = savedStateHandle.get<Long>(ARG_CONVERSATION_ID) ?: NEW_CONVERSATION_ID

    private val _uiState = MutableStateFlow(
        ConversationUiState(
            phase = if (navConversationId == NEW_CONVERSATION_ID) {
                ConversationPhase.ScenarioSelection
            } else {
                ConversationPhase.Active
            },
        ),
    )
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var conversationId: Long = navConversationId
    private var startedAtEpochSeconds: Long = Clock.System.now().epochSeconds
    private var listeningJob: Job? = null
    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            val preferences = observePreferences().first()
            textToSpeech.configure(preferences.ttsVoice, preferences.ttsSpeechRate)
        }
        if (navConversationId != NEW_CONVERSATION_ID) {
            observeMessages()
        }
    }

    fun selectScenario(scenario: ConversationScenario) {
        _uiState.update {
            it.copy(phase = ConversationPhase.Active, micState = MicUiState.Processing)
        }
        viewModelScope.launch {
            startConversation(scenario)
                .onSuccess { started ->
                    conversationId = started.conversationId
                    startedAtEpochSeconds = Clock.System.now().epochSeconds
                    observeMessages()
                    speak(started.greeting)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(micState = MicUiState.Idle, error = error)
                    }
                }
        }
    }

    /** Single entry point for mic taps; behavior depends on current state. */
    fun onMicTapped() {
        when (_uiState.value.micState) {
            MicUiState.Idle -> startListening()
            MicUiState.Listening -> speechToText.stop()
            MicUiState.Speaking -> {
                textToSpeech.stop()
                startListening()
            }
            MicUiState.Processing -> Unit // AI in flight; ignore taps.
        }
    }

    private fun startListening() {
        if (conversationId == NEW_CONVERSATION_ID) return
        listeningJob?.cancel()
        _uiState.update {
            it.copy(micState = MicUiState.Listening, partialText = "", error = null)
        }

        listeningJob = speechToText.listen()
            .onEach { event ->
                when (event) {
                    is SpeechEvent.ReadyForSpeech -> Unit
                    is SpeechEvent.RmsChanged ->
                        _uiState.update { it.copy(voiceLevel = event.level) }
                    is SpeechEvent.Partial ->
                        _uiState.update { it.copy(partialText = event.text) }
                    is SpeechEvent.Result -> handleUserUtterance(event.text)
                    is SpeechEvent.Error -> {
                        val silentRetryable =
                            (event.error as? AppError.SpeechRecognition) != null
                        _uiState.update {
                            it.copy(
                                micState = MicUiState.Idle,
                                partialText = "",
                                voiceLevel = 0f,
                                error = if (silentRetryable) event.error else event.error,
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleUserUtterance(text: String) {
        _uiState.update {
            it.copy(micState = MicUiState.Processing, partialText = "", voiceLevel = 0f)
        }
        viewModelScope.launch {
            sendMessage(conversationId, text)
                .onSuccess { reply -> speak(reply.content) }
                .onFailure { error ->
                    _uiState.update { it.copy(micState = MicUiState.Idle, error = error) }
                }
        }
    }

    private fun speak(text: String) {
        _uiState.update { it.copy(micState = MicUiState.Speaking) }
        viewModelScope.launch {
            val spoken = textToSpeech.speak(text)
            if (!spoken) Timber.w("TTS failed or was interrupted")
            _uiState.update { current ->
                // A tap during Speaking may already have moved us to Listening.
                if (current.micState == MicUiState.Speaking) {
                    current.copy(micState = MicUiState.Idle)
                } else {
                    current
                }
            }
        }
    }

    fun onTranslateRequested(message: Message) {
        if (message.translation != null) return
        viewModelScope.launch {
            translateMessage(message.id, message.content)
                .onFailure { error -> _uiState.update { it.copy(error = error) } }
        }
    }

    fun onEndConversation(onEnded: (Long) -> Unit) {
        val id = conversationId
        if (id == NEW_CONVERSATION_ID) return
        textToSpeech.stop()
        speechToText.stop()
        viewModelScope.launch {
            val duration = (Clock.System.now().epochSeconds - startedAtEpochSeconds).toInt()
            endConversation(id, duration.coerceAtLeast(0))
            onEnded(id)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun observeMessages() {
        messagesJob?.cancel()
        messagesJob = observeConversationDetail(conversationId)
            .onEach { detail ->
                if (detail != null) {
                    _uiState.update {
                        it.copy(messages = detail.messages, title = detail.conversation.title)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        textToSpeech.stop()
        speechToText.stop()
        super.onCleared()
    }

    companion object {
        const val ARG_CONVERSATION_ID = "conversationId"
        const val NEW_CONVERSATION_ID = -1L
    }
}

enum class ConversationPhase { ScenarioSelection, Active }

enum class MicUiState { Idle, Listening, Processing, Speaking }

data class ConversationUiState(
    val phase: ConversationPhase = ConversationPhase.ScenarioSelection,
    val title: String = "",
    val messages: List<Message> = emptyList(),
    val micState: MicUiState = MicUiState.Idle,
    /** Live partial transcription while listening. */
    val partialText: String = "",
    /** Microphone input level 0..1 for the waveform halo. */
    val voiceLevel: Float = 0f,
    val error: AppError? = null,
)
