package com.aienglishcoach.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = UserErrorEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_error_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["due_at"]),
        Index(value = ["source_error_id"]),
    ],
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Name of [com.aienglishcoach.core.domain.model.ExerciseType]. */
    val type: String,
    val question: String,
    /** JSON-encoded list of options for multiple choice. */
    val options: String,
    @ColumnInfo(name = "correct_answer")
    val correctAnswer: String,
    val explanation: String,
    @ColumnInfo(name = "source_error_id")
    val sourceErrorId: Long? = null,
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
