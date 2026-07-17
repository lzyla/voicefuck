package com.aienglishcoach.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aienglishcoach.core.database.entity.AiMemoryEntity

@Dao
interface AiMemoryDao {

    @Query("SELECT * FROM ai_memories ORDER BY importance DESC, updated_at DESC")
    suspend fun getAll(): List<AiMemoryEntity>

    @Query("SELECT * FROM ai_memories WHERE kind = :kind ORDER BY importance DESC")
    suspend fun getByKind(kind: String): List<AiMemoryEntity>

    @Query("SELECT * FROM ai_memories WHERE embedding IS NULL")
    suspend fun getWithoutEmbedding(): List<AiMemoryEntity>

    @Insert
    suspend fun insert(memory: AiMemoryEntity): Long

    @Update
    suspend fun update(memory: AiMemoryEntity)

    @Query("UPDATE ai_memories SET embedding = :embedding WHERE id = :id")
    suspend fun updateEmbedding(id: Long, embedding: ByteArray)

    @Query("DELETE FROM ai_memories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        DELETE FROM ai_memories WHERE id IN (
            SELECT id FROM ai_memories
            ORDER BY importance ASC, updated_at ASC
            LIMIT MAX(0, (SELECT COUNT(*) FROM ai_memories) - :maxCount)
        )
        """,
    )
    suspend fun prune(maxCount: Int)
}
