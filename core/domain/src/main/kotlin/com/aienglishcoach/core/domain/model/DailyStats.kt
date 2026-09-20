package com.aienglishcoach.core.domain.model

import kotlinx.datetime.LocalDate

/** Aggregated learning activity for a single calendar day. */
data class DailyStats(
    val date: LocalDate,
    val conversationSeconds: Int = 0,
    val messagesSent: Int = 0,
    val wordsLearned: Int = 0,
    val exercisesDone: Int = 0,
    val exercisesCorrect: Int = 0,
    /** Average pronunciation score for the day, null when nothing practiced. */
    val pronunciationAverage: Double? = null,
)

/** Snapshot shown on the statistics screen and the home dashboard. */
data class StatisticsOverview(
    /** Consecutive days (ending today or yesterday) with any activity. */
    val streakDays: Int,
    val totalConversationMinutes: Int,
    val totalWordsLearned: Int,
    val totalExercisesDone: Int,
    /** 0–100, share of correct exercise attempts. */
    val exerciseAccuracyPercent: Int,
    val averagePronunciationScore: Int?,
    /** Last 30 days, oldest first, for charts. */
    val recentDays: List<DailyStats>,
)
