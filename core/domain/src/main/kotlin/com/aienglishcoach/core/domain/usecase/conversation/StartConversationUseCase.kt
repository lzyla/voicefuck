package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.common.result.map
import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.MessageRole
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import com.aienglishcoach.core.domain.service.MemoryRetrievalService
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * Creates a new conversation for the chosen scenario and asks the tutor to
 * open it with a personalized greeting (long-term memories included).
 *
 * The conversation row is written first so the session survives an AI
 * failure — the learner can retry the greeting without losing the session.
 */
class StartConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val settingsRepository: SettingsRepository,
    private val aiTutorService: AiTutorService,
    private val memoryRetrievalService: MemoryRetrievalService,
) {

    suspend operator fun invoke(scenario: ConversationScenario): AppResult<StartedConversation> {
        val now = Clock.System.now()
        val conversationId = conversationRepository.createConversation(
            Conversation(
                title = scenario.name.lowercase().replace('_', ' ')
                    .replaceFirstChar { it.uppercase() },
                scenario = scenario,
                startedAt = now,
            ),
        )

        val preferences = settingsRepository.preferences.first()
        val memories = memoryRetrievalService.retrieveRelevant(query = scenario.topicHint)

        return aiTutorService
            .generateReply(scenario, history = emptyList(), preferences, memories)
            .map { greeting ->
                val messageId = conversationRepository.addMessage(
                    Message(
                        conversationId = conversationId,
                        role = MessageRole.ASSISTANT,
                        content = greeting,
                        createdAt = Clock.System.now(),
                    ),
                )
                StartedConversation(conversationId, greeting, messageId)
            }
    }
}

data class StartedConversation(
    val conversationId: Long,
    val greeting: String,
    val greetingMessageId: Long,
)
