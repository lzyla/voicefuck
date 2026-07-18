package com.aienglishcoach.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aienglishcoach.core.database.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun observeByDate(date: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getByDate(date: String): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    suspend fun getAllDescending(): List<DailyStatsEntity>

    @Upsert
    suspend fun upsert(stats: DailyStatsEntity)
}
