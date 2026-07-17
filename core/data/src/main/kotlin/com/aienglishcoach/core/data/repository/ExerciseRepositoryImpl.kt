package com.aienglishcoach.core.data.repository

import com.aienglishcoach.core.data.mapper.toDomain
import com.aienglishcoach.core.data.mapper.toEntity
import com.aienglishcoach.core.database.dao.ExerciseDao
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseAttempt
import com.aienglishcoach.core.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {

    override fun observeAll(): Flow<List<Exercise>> =
        exerciseDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeDueCount(now: Instant): Flow<Int> =
        exerciseDao.observeDueCount(now.toEpochMilliseconds())

    override suspend fun getDueExercises(now: Instant, limit: Int): List<Exercise> =
        exerciseDao.getDue(now.toEpochMilliseconds(), limit).map { it.toDomain() }

    override suspend fun getExercise(id: Long): Exercise? =
        exerciseDao.getById(id)?.toDomain()

    override suspend fun insertAll(exercises: List<Exercise>): List<Long> =
        exerciseDao.insertAll(exercises.map { it.toEntity() })

    override suspend fun update(exercise: Exercise) = exerciseDao.update(exercise.toEntity())

    override suspend fun recordAttempt(attempt: ExerciseAttempt) {
        exerciseDao.insertAttempt(attempt.toEntity())
    }

    override suspend fun delete(id: Long) = exerciseDao.deleteById(id)
}
