package com.aienglishcoach.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aienglishcoach.core.database.entity.ExerciseAttemptEntity
import com.aienglishcoach.core.database.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY created_at DESC")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises WHERE due_at <= :nowEpochMillis")
    fun observeDueCount(nowEpochMillis: Long): Flow<Int>

    @Query("SELECT * FROM exercises WHERE due_at <= :nowEpochMillis ORDER BY due_at ASC LIMIT :limit")
    suspend fun getDue(nowEpochMillis: Long, limit: Int): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Insert
    suspend fun insertAll(exercises: List<ExerciseEntity>): List<Long>

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Insert
    suspend fun insertAttempt(attempt: ExerciseAttemptEntity): Long

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteById(id: Long)
}
