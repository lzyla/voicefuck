package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.common.result.map
import com.aienglishcoach.core.domain.model.ConversationAnalysis
import com.aienglishcoach.core.domain.model.ConversationStatus
import com.aienglishcoach.core.domain.model.MessageRole
import com.aienglishcoach.core.domain.model.SrsState
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.repository.UserErrorRepository
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Runs the post-conversation AI analysis and persists everything it finds:
 * detected errors, new vocabulary (scheduled for SRS immediately) and the
 * conversation summary. Idempotent: re-running replaces the summary and
 * skips vocabulary duplicates.
 */
class AnalyzeConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val userErrorRepository: UserErrorRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val statisticsRepository: StatisticsRepository,
    private val settingsRepository: SettingsRepository,
    private val aiTutorService: AiTutorService,
) {

    suspend operator fun invoke(conversationId: Long): AppResult<ConversationAnalysis> {
        val history = conversationRepository.getMessages(conversationId)
        val userSpokeAtAll = history.any { it.role == MessageRole.USER }
        if (!userSpokeAtAll) {
            return AppResult.failure(AppError.Unknown("Nothing to analyze"))
        }

        val preferences = settingsRepository.preferences.first()

        return aiTutorService.analyzeConversation(history, preferences).map { analysis ->
            persist(conversationId, analysis)
            analysis
        }
    }

    private suspend fun persist(conversationId: Long, analysis: ConversationAnalysis) {
        val now = Clock.System.now()

        userErrorRepository.insertAll(
            analysis.errors.map { detected ->
                UserError(
                    conversationId = conversationId,
                    category = detected.category,
                    original = detected.original,
                    corrected = detected.corrected,
                    explanation = detected.explanation,
                    createdAt = now,
                )
            },
        )

        vocabularyRepository.upsertAll(
            analysis.vocabulary.map { detected ->
                VocabularyItem(
                    word = detected.word,
                    translation = detected.translation,
                    definition = detected.definition,
                    example = detected.example,
                    sourceConversationId = conversationId,
                    srs = SrsState(dueAt = now),
                    createdAt = now,
                )
            },
        )

        conversationRepository.updateSummary(
            id = conversationId,
            summary = analysis.summary,
            status = ConversationStatus.ANALYZED,
        )

        statisticsRepository.addToDay(
            date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            wordsLearned = analysis.vocabulary.size,
        )
    }
}
