package com.aienglishcoach.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary_items",
    indices = [
        Index(value = ["word"], unique = true),
        Index(value = ["due_at"]),
    ],
)
data class VocabularyItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val translation: String,
    val definition: String? = null,
    val example: String? = null,
    @ColumnInfo(name = "source_conversation_id")
    val sourceConversationId: Long? = null,
    /** Name of [com.aienglishcoach.core.domain.model.VocabularyStatus]. */
    val status: String,
    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double,
    @ColumnInfo(name = "interval_days")
    val intervalDays: Int,
    @ColumnInfo(name = "repetition_count")
    val repetitionCount: Int,
    @ColumnInfo(name = "due_at")
    val dueAtEpochMillis: Long,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
)
