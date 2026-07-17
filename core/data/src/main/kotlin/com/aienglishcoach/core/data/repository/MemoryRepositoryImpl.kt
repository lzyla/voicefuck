package com.aienglishcoach.core.data.repository

import com.aienglishcoach.core.data.mapper.toByteArray
import com.aienglishcoach.core.data.mapper.toDomain
import com.aienglishcoach.core.data.mapper.toEntity
import com.aienglishcoach.core.database.dao.AiMemoryDao
import com.aienglishcoach.core.domain.model.AiMemory
import com.aienglishcoach.core.domain.model.MemoryKind
import com.aienglishcoach.core.domain.repository.MemoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val aiMemoryDao: AiMemoryDao,
) : MemoryRepository {

    override suspend fun getAll(): List<AiMemory> =
        aiMemoryDao.getAll().map { it.toDomain() }

    override suspend fun getByKind(kind: MemoryKind): List<AiMemory> =
        aiMemoryDao.getByKind(kind.name).map { it.toDomain() }

    override suspend fun upsert(memory: AiMemory): Long {
        // Near-duplicate contents update the existing row instead of growing
        // the store; the AI already deduplicates, this is the safety net.
        val existing = aiMemoryDao.getAll().firstOrNull {
            it.content.equals(memory.content, ignoreCase = true)
        }
        return if (existing != null) {
            aiMemoryDao.update(
                existing.copy(
                    kind = memory.kind.name,
                    importance = maxOf(existing.importance, memory.importance),
                    updatedAtEpochMillis = memory.updatedAt.toEpochMilliseconds(),
                ),
            )
            existing.id
        } else {
            aiMemoryDao.insert(memory.toEntity())
        }
    }

    override suspend fun updateEmbedding(id: Long, embedding: FloatArray) =
        aiMemoryDao.updateEmbedding(id, embedding.toByteArray())

    override suspend fun delete(id: Long) = aiMemoryDao.deleteById(id)

    override suspend fun prune(maxCount: Int) = aiMemoryDao.prune(maxCount)
}
