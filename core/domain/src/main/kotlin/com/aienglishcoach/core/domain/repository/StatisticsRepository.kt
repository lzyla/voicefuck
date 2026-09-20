package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.DailyStats
import com.aienglishcoach.core.domain.model.StatisticsOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Aggregated daily learning statistics. */
interface StatisticsRepository {

    fun observeOverview(): Flow<StatisticsOverview>

    fun observeDay(date: LocalDate): Flow<DailyStats?>

    /** Adds the given deltas to today's row, creating it when missing. */
    suspend fun addToDay(
        date: LocalDate,
        conversationSeconds: Int = 0,
        messagesSent: Int = 0,
        wordsLearned: Int = 0,
        exercisesDone: Int = 0,
        exercisesCorrect: Int = 0,
    )

    suspend fun recordPronunciationScore(date: LocalDate, score: Int)
}
