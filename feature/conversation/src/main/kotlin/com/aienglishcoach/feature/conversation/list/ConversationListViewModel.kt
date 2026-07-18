package com.aienglishcoach.feature.conversation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.usecase.conversation.ObserveConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    observeConversations: ObserveConversationsUseCase,
) : ViewModel() {

    val uiState: StateFlow<ConversationListUiState> = observeConversations()
        .map<List<Conversation>, ConversationListUiState> {
            ConversationListUiState.Ready(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConversationListUiState.Loading,
        )
}

sealed interface ConversationListUiState {
    data object Loading : ConversationListUiState
    data class Ready(val conversations: List<Conversation>) : ConversationListUiState
}
