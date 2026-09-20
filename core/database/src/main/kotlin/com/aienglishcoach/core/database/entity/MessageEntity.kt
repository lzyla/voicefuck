package com.aienglishcoach.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["conversation_id", "created_at"])],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,
    /** USER or ASSISTANT. */
    val role: String,
    val content: String,
    val translation: String? = null,
    @ColumnInfo(name = "audio_path")
    val audioPath: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
)
