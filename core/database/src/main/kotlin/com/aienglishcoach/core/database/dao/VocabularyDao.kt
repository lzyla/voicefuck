package com.aienglishcoach.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aienglishcoach.core.database.entity.VocabularyItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Query("SELECT * FROM vocabulary_items ORDER BY created_at DESC")
    fun observeAll(): Flow<List<VocabularyItemEntity>>

    @Query("SELECT COUNT(*) FROM vocabulary_items WHERE due_at <= :nowEpochMillis")
    fun observeDueCount(nowEpochMillis: Long): Flow<Int>

    @Query(
        "SELECT * FROM vocabulary_items WHERE due_at <= :nowEpochMillis ORDER BY due_at ASC LIMIT :limit",
    )
    suspend fun getDue(nowEpochMillis: Long, limit: Int): List<VocabularyItemEntity>

    @Query("SELECT * FROM vocabulary_items WHERE id = :id")
    suspend fun getById(id: Long): VocabularyItemEntity?

    @Query("SELECT id FROM vocabulary_items WHERE word = :word COLLATE NOCASE LIMIT 1")
    suspend fun findIdByWord(word: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: VocabularyItemEntity): Long

    @Update
    suspend fun update(item: VocabularyItemEntity)

    @Query("DELETE FROM vocabulary_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM vocabulary_items")
    suspend fun countAll(): Int
}
