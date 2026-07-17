package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.common.result.map
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.MessageRole
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import com.aienglishcoach.core.domain.service.MemoryRetrievalService
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Persists the learner's utterance, retrieves relevant long-term memories,
 * asks the tutor for a reply and persists it — offline-first: both sides of
 * the exchange are stored locally; the user message survives an AI failure.
 */
class SendMessageUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val settingsRepository: SettingsRepository,
    private val statisticsRepository: StatisticsRepository,
    private val aiTutorService: AiTutorService,
    private val memoryRetrievalService: MemoryRetrievalService,
) {

    suspend operator fun invoke(conversationId: Long, userText: String): AppResult<Message> {
        val conversation = conversationRepository.getConversation(conversationId)
            ?: return AppResult.failure(
                com.aienglishcoach.core.common.result.AppError.Storage("Conversation $conversationId not found"),
            )

        conversationRepository.addMessage(
            Message(
                conversationId = conversationId,
                role = MessageRole.USER,
                content = userText,
                createdAt = Clock.System.now(),
            ),
        )
        statisticsRepository.addToDay(
            date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            messagesSent = 1,
        )

        val history = conversationRepository.getMessages(conversationId)
            .takeLast(CONTEXT_WINDOW_MESSAGES)
        val preferences = settingsRepository.preferences.first()
        val memories = memoryRetrievalService.retrieveRelevant(query = userText)

        return aiTutorService
            .generateReply(conversation.scenario, history, preferences, memories)
            .map { replyText ->
                val reply = Message(
                    conversationId = conversationId,
                    role = MessageRole.ASSISTANT,
                    content = replyText,
                    createdAt = Clock.System.now(),
                )
                reply.copy(id = conversationRepository.addMessage(reply))
            }
    }

    companion object {
        /** Short-term memory: how many latest messages are sent to the model. */
        const val CONTEXT_WINDOW_MESSAGES = 30
    }
}
