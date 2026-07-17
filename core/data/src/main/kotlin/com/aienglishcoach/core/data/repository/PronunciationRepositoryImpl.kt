package com.aienglishcoach.core.data.repository

import com.aienglishcoach.core.data.mapper.toDomain
import com.aienglishcoach.core.data.mapper.toEntity
import com.aienglishcoach.core.database.dao.PronunciationDao
import com.aienglishcoach.core.domain.model.PronunciationResult
import com.aienglishcoach.core.domain.repository.PronunciationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days

@Singleton
class PronunciationRepositoryImpl @Inject constructor(
    private val pronunciationDao: PronunciationDao,
) : PronunciationRepository {

    override fun observeRecent(limit: Int): Flow<List<PronunciationResult>> =
        pronunciationDao.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }

    override suspend fun insert(result: PronunciationResult): Long =
        pronunciationDao.insert(result.toEntity())

    override suspend fun averageScoreSince(daysBack: Int): Double? =
        pronunciationDao.averageScoreSince(
            (Clock.System.now() - daysBack.days).toEpochMilliseconds(),
        )
}
