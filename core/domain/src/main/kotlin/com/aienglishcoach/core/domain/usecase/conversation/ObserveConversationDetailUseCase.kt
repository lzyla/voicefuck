package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.UserErrorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Streams everything the conversation and summary screens need:
 * the conversation row, its messages and the errors detected in it.
 */
class ObserveConversationDetailUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val userErrorRepository: UserErrorRepository,
) {

    operator fun invoke(conversationId: Long): Flow<ConversationDetail?> = combine(
        conversationRepository.observeConversation(conversationId),
        conversationRepository.observeMessages(conversationId),
        userErrorRepository.observeForConversation(conversationId),
    ) { conversation, messages, errors ->
        conversation?.let { ConversationDetail(it, messages, errors) }
    }
}

data class ConversationDetail(
    val conversation: Conversation,
    val messages: List<Message>,
    val errors: List<UserError>,
)
