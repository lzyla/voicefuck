package com.aienglishcoach.core.data.repository

import com.aienglishcoach.core.data.mapper.toDomain
import com.aienglishcoach.core.database.dao.DailyStatsDao
import com.aienglishcoach.core.database.entity.DailyStatsEntity
import com.aienglishcoach.core.domain.model.DailyStats
import com.aienglishcoach.core.domain.model.StatisticsOverview
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val dailyStatsDao: DailyStatsDao,
) : StatisticsRepository {

    override fun observeOverview(): Flow<StatisticsOverview> =
        dailyStatsDao.observeRecent(OVERVIEW_WINDOW_DAYS).map { rows ->
            buildOverview(rows)
        }

    override fun observeDay(date: LocalDate): Flow<DailyStats?> =
        dailyStatsDao.observeByDate(date.toString()).map { it?.toDomain() }

    override suspend fun addToDay(
        date: LocalDate,
        conversationSeconds: Int,
        messagesSent: Int,
        wordsLearned: Int,
        exercisesDone: Int,
        exercisesCorrect: Int,
    ) {
        val current = dailyStatsDao.getByDate(date.toString()) ?: DailyStatsEntity(date.toString())
        dailyStatsDao.upsert(
            current.copy(
                conversationSeconds = current.conversationSeconds + conversationSeconds,
                messagesSent = current.messagesSent + messagesSent,
                wordsLearned = current.wordsLearned + wordsLearned,
                exercisesDone = current.exercisesDone + exercisesDone,
                exercisesCorrect = current.exercisesCorrect + exercisesCorrect,
            ),
        )
    }

    override suspend fun recordPronunciationScore(date: LocalDate, score: Int) {
        val current = dailyStatsDao.getByDate(date.toString()) ?: DailyStatsEntity(date.toString())
        dailyStatsDao.upsert(
            current.copy(
                pronunciationScoreSum = current.pronunciationScoreSum + score,
                pronunciationAttempts = current.pronunciationAttempts + 1,
            ),
        )
    }

    private fun buildOverview(rows: List<DailyStatsEntity>): StatisticsOverview {
        val days = rows.map { it.toDomain() }
        val totalExercises = days.sumOf { it.exercisesDone }
        val totalCorrect = days.sumOf { it.exercisesCorrect }
        val pronunciationDays = days.mapNotNull { it.pronunciationAverage }

        return StatisticsOverview(
            streakDays = computeStreak(days.map { it.date }.toSet()),
            totalConversationMinutes = days.sumOf { it.conversationSeconds } / 60,
            totalWordsLearned = days.sumOf { it.wordsLearned },
            totalExercisesDone = totalExercises,
            exerciseAccuracyPercent = if (totalExercises > 0) {
                (totalCorrect * 100.0 / totalExercises).roundToInt()
            } else {
                0
            },
            averagePronunciationScore = pronunciationDays
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.roundToInt(),
            recentDays = days.sortedBy { it.date }.takeLast(CHART_WINDOW_DAYS),
        )
    }

    /**
     * Streak = consecutive active days ending today, or yesterday (so the
     * streak is not broken before the user had a chance to practice today).
     */
    private fun computeStreak(activeDates: Set<LocalDate>): Int {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        var cursor = if (today in activeDates) today else today.minus(1, DateTimeUnit.DAY)
        var streak = 0
        while (cursor in activeDates) {
            streak++
            cursor = cursor.minus(1, DateTimeUnit.DAY)
        }
        return streak
    }

    companion object {
        /** Enough history for streaks and charts without unbounded queries. */
        private const val OVERVIEW_WINDOW_DAYS = 366
        private const val CHART_WINDOW_DAYS = 30
    }
}
