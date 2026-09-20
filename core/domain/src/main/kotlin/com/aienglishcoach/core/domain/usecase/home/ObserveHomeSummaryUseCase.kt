package com.aienglishcoach.core.domain.usecase.home

import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.ExerciseRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Streams everything the home dashboard needs: streak, today's progress
 * against the daily goal, due revision counts and the latest conversation.
 */
class ObserveHomeSummaryUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val exerciseRepository: ExerciseRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val conversationRepository: ConversationRepository,
    private val settingsRepository: SettingsRepository,
) {

    operator fun invoke(): Flow<HomeSummary> {
        val now = Clock.System.now()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return combine(
            statisticsRepository.observeOverview(),
            statisticsRepository.observeDay(today),
            exerciseRepository.observeDueCount(now),
            vocabularyRepository.observeDueCount(now),
            conversationRepository.observeConversations().map { it.firstOrNull() },
        ) { overview, todayStats, dueExercises, dueVocabulary, lastConversation ->
            HomeSummary(
                streakDays = overview.streakDays,
                todayMinutes = (todayStats?.conversationSeconds ?: 0) / 60,
                dueExercises = dueExercises,
                dueVocabulary = dueVocabulary,
                lastConversation = lastConversation,
            )
        }.combine(settingsRepository.preferences) { summary, preferences ->
            summary.copy(dailyGoalMinutes = preferences.dailyGoalMinutes)
        }
    }
}

data class HomeSummary(
    val streakDays: Int = 0,
    val todayMinutes: Int = 0,
    val dailyGoalMinutes: Int = 10,
    val dueExercises: Int = 0,
    val dueVocabulary: Int = 0,
    val lastConversation: Conversation? = null,
)
