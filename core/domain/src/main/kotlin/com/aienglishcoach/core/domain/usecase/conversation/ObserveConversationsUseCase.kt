package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the conversation history, newest first. */
class ObserveConversationsUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
) {
    operator fun invoke(): Flow<List<Conversation>> =
        conversationRepository.observeConversations()
}
