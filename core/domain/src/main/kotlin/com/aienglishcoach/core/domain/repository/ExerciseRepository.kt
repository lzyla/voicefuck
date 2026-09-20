package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseAttempt
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/** Storage for AI-generated exercises and the learner's attempts. */
interface ExerciseRepository {

    fun observeAll(): Flow<List<Exercise>>

    fun observeDueCount(now: Instant): Flow<Int>

    suspend fun getDueExercises(now: Instant, limit: Int): List<Exercise>

    suspend fun getExercise(id: Long): Exercise?

    suspend fun insertAll(exercises: List<Exercise>): List<Long>

    suspend fun update(exercise: Exercise)

    suspend fun recordAttempt(attempt: ExerciseAttempt)

    suspend fun delete(id: Long)
}
