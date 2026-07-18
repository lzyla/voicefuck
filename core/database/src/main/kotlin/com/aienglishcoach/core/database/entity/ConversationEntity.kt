package com.aienglishcoach.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["started_at"])],
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    /** Name of [com.aienglishcoach.core.domain.model.ConversationScenario]. */
    val scenario: String,
    @ColumnInfo(name = "started_at")
    val startedAtEpochMillis: Long,
    @ColumnInfo(name = "ended_at")
    val endedAtEpochMillis: Long? = null,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int = 0,
    /** Name of [com.aienglishcoach.core.domain.model.ConversationStatus]. */
    val status: String,
    val summary: String? = null,
)
