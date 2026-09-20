package com.aienglishcoach.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_errors",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["resolved_at"]),
    ],
)
data class UserErrorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long? = null,
    /** Name of [com.aienglishcoach.core.domain.model.ErrorCategory]. */
    val category: String,
    val original: String,
    val corrected: String,
    val explanation: String,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "resolved_at")
    val resolvedAtEpochMillis: Long? = null,
)
