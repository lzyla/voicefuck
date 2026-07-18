package com.aienglishcoach.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pronunciation_results",
    indices = [Index(value = ["created_at"])],
)
data class PronunciationResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    @ColumnInfo(name = "expected_text")
    val expectedText: String,
    @ColumnInfo(name = "recognized_text")
    val recognizedText: String,
    /** 0–100. */
    val score: Int,
    val feedback: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
)
