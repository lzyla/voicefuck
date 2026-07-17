package com.aienglishcoach.core.data.repository

import com.aienglishcoach.core.data.mapper.toDomain
import com.aienglishcoach.core.data.mapper.toEntity
import com.aienglishcoach.core.database.dao.VocabularyDao
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepositoryImpl @Inject constructor(
    private val vocabularyDao: VocabularyDao,
) : VocabularyRepository {

    override fun observeAll(): Flow<List<VocabularyItem>> =
        vocabularyDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeDueCount(now: Instant): Flow<Int> =
        vocabularyDao.observeDueCount(now.toEpochMilliseconds())

    override suspend fun getDueItems(now: Instant, limit: Int): List<VocabularyItem> =
        vocabularyDao.getDue(now.toEpochMilliseconds(), limit).map { it.toDomain() }

    override suspend fun getItem(id: Long): VocabularyItem? =
        vocabularyDao.getById(id)?.toDomain()

    override suspend fun upsert(item: VocabularyItem): Long {
        val insertedId = vocabularyDao.insert(item.toEntity())
        if (insertedId != -1L) return insertedId
        // Duplicate word: keep the existing item and its SRS progress.
        return vocabularyDao.findIdByWord(item.word) ?: -1L
    }

    override suspend fun upsertAll(items: List<VocabularyItem>) {
        items.forEach { upsert(it) }
    }

    override suspend fun update(item: VocabularyItem) =
        vocabularyDao.update(item.toEntity())

    override suspend fun delete(id: Long) = vocabularyDao.deleteById(id)

    override suspend fun countAll(): Int = vocabularyDao.countAll()
}
