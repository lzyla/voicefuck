package com.aienglishcoach.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    /** ISO-8601 date, e.g. "2026-07-17"; sorts chronologically as text. */
    @PrimaryKey
    val date: String,
    @ColumnInfo(name = "conversation_seconds")
    val conversationSeconds: Int = 0,
    @ColumnInfo(name = "messages_sent")
    val messagesSent: Int = 0,
    @ColumnInfo(name = "words_learned")
    val wordsLearned: Int = 0,
    @ColumnInfo(name = "exercises_done")
    val exercisesDone: Int = 0,
    @ColumnInfo(name = "exercises_correct")
    val exercisesCorrect: Int = 0,
    /** Sum of pronunciation scores that day (for averaging). */
    @ColumnInfo(name = "pronunciation_score_sum")
    val pronunciationScoreSum: Int = 0,
    @ColumnInfo(name = "pronunciation_attempts")
    val pronunciationAttempts: Int = 0,
)
