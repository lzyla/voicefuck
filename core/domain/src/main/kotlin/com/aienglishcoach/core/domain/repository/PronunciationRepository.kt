package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.PronunciationResult
import kotlinx.coroutines.flow.Flow

/** Storage for pronunciation practice results. */
interface PronunciationRepository {

    fun observeRecent(limit: Int): Flow<List<PronunciationResult>>

    suspend fun insert(result: PronunciationResult): Long

    suspend fun averageScoreSince(daysBack: Int): Double?
}
