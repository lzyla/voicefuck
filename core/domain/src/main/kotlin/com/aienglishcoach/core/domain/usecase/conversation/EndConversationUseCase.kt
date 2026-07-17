package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Marks a conversation as ended and books its duration into daily stats.
 * Post-conversation analysis runs separately (see AnalyzeConversationUseCase),
 * so ending a conversation always succeeds offline.
 */
class EndConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val statisticsRepository: StatisticsRepository,
) {

    suspend operator fun invoke(conversationId: Long, durationSeconds: Int) {
        conversationRepository.endConversation(conversationId, durationSeconds)
        statisticsRepository.addToDay(
            date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            conversationSeconds = durationSeconds,
        )
    }
}
