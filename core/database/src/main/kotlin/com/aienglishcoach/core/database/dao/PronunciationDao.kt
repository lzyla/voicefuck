package com.aienglishcoach.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aienglishcoach.core.database.entity.PronunciationResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PronunciationDao {

    @Query("SELECT * FROM pronunciation_results ORDER BY created_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<PronunciationResultEntity>>

    @Insert
    suspend fun insert(result: PronunciationResultEntity): Long

    @Query("SELECT AVG(score) FROM pronunciation_results WHERE created_at >= :sinceEpochMillis")
    suspend fun averageScoreSince(sinceEpochMillis: Long): Double?
}
