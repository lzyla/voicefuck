package com.aienglishcoach.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aienglishcoach.core.database.entity.UserErrorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserErrorDao {

    @Query("SELECT * FROM user_errors ORDER BY created_at DESC")
    fun observeAll(): Flow<List<UserErrorEntity>>

    @Query("SELECT * FROM user_errors WHERE conversation_id = :conversationId ORDER BY id ASC")
    fun observeForConversation(conversationId: Long): Flow<List<UserErrorEntity>>

    @Query(
        "SELECT * FROM user_errors WHERE resolved_at IS NULL ORDER BY created_at DESC LIMIT :limit",
    )
    suspend fun getUnresolved(limit: Int): List<UserErrorEntity>

    @Insert
    suspend fun insertAll(errors: List<UserErrorEntity>): List<Long>

    @Query("UPDATE user_errors SET resolved_at = :resolvedAtEpochMillis WHERE id = :id")
    suspend fun markResolved(id: Long, resolvedAtEpochMillis: Long)
}
