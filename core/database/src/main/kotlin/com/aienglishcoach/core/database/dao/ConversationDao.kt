package com.aienglishcoach.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aienglishcoach.core.database.entity.ConversationEntity
import com.aienglishcoach.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY started_at DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun observeById(id: Long): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: Long): ConversationEntity?

    @Insert
    suspend fun insert(conversation: ConversationEntity): Long

    @Query(
        """
        UPDATE conversations
        SET ended_at = :endedAtEpochMillis, duration_seconds = :durationSeconds, status = :status
        WHERE id = :id
        """,
    )
    suspend fun markEnded(
        id: Long,
        endedAtEpochMillis: Long,
        durationSeconds: Int,
        status: String,
    )

    @Query("UPDATE conversations SET summary = :summary, status = :status WHERE id = :id")
    suspend fun updateSummary(id: Long, summary: String, status: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at ASC, id ASC")
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at ASC, id ASC")
    suspend fun getMessages(conversationId: Long): List<MessageEntity>

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET translation = :translation WHERE id = :messageId")
    suspend fun updateMessageTranslation(messageId: Long, translation: String)
}
