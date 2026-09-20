package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.VocabularyItem
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/** Local-first vocabulary storage with SRS scheduling fields. */
interface VocabularyRepository {

    fun observeAll(): Flow<List<VocabularyItem>>

    fun observeDueCount(now: Instant): Flow<Int>

    suspend fun getDueItems(now: Instant, limit: Int): List<VocabularyItem>

    suspend fun getItem(id: Long): VocabularyItem?

    /** Inserts the item unless the same word already exists; returns id. */
    suspend fun upsert(item: VocabularyItem): Long

    suspend fun upsertAll(items: List<VocabularyItem>)

    suspend fun update(item: VocabularyItem)

    suspend fun delete(id: Long)

    suspend fun countAll(): Int
}
