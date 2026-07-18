package com.aienglishcoach.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.common.result.onSuccess
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.usecase.conversation.ObserveConversationDetailUseCase
import com.aienglishcoach.core.domain.usecase.conversation.SendMessageUseCase
import com.aienglishcoach.core.domain.usecase.conversation.StartConversationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Text-only chat with the same tutor backend as the voice conversation
 * screen (shares [StartConversationUseCase]/[SendMessageUseCase]) — the
 * design handoff calls for Text Chat as a distinct surface from Voice
 * Conversation, but there is no separate AI backend for it, so it reuses a
 * plain [ConversationScenario.FREE_TALK] session.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val startConversation: StartConversationUseCase,
    private val sendMessage: SendMessageUseCase,
    private val observeConversationDetail: ObserveConversationDetailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversationId: Long? = null

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isStarting = true) }
            startConversation(ConversationScenario.FREE_TALK).onSuccess { started ->
                conversationId = started.conversationId
                _uiState.update { it.copy(isStarting = false) }
                observeConversationDetail(started.conversationId)
                    .onEach { detail ->
                        _uiState.update { state -> state.copy(messages = detail?.messages.orEmpty()) }
                    }
                    .launchIn(viewModelScope)
            }
        }
    }

    fun onInputChanged(value: String) {
        _uiState.update { it.copy(input = value) }
    }

    fun send() {
        val text = _uiState.value.input.trim()
        val id = conversationId
        if (text.isEmpty() || id == null) return
        _uiState.update { it.copy(input = "", isSending = true) }
        viewModelScope.launch {
            sendMessage(id, text)
            _uiState.update { it.copy(isSending = false) }
        }
    }
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val isStarting: Boolean = false,
    val isSending: Boolean = false,
)
