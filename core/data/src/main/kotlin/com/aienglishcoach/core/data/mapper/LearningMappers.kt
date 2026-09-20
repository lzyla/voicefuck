package com.aienglishcoach.core.data.mapper

import com.aienglishcoach.core.database.entity.ExerciseAttemptEntity
import com.aienglishcoach.core.database.entity.ExerciseEntity
import com.aienglishcoach.core.database.entity.UserErrorEntity
import com.aienglishcoach.core.database.entity.VocabularyItemEntity
import com.aienglishcoach.core.domain.model.ErrorCategory
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseAttempt
import com.aienglishcoach.core.domain.model.ExerciseType
import com.aienglishcoach.core.domain.model.SrsState
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.model.VocabularyStatus
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private val stringListSerializer = ListSerializer(String.serializer())

fun UserErrorEntity.toDomain(): UserError = UserError(
    id = id,
    conversationId = conversationId,
    category = enumOrDefault(category, ErrorCategory.GRAMMAR),
    original = original,
    corrected = corrected,
    explanation = explanation,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
    resolvedAt = resolvedAtEpochMillis?.let(Instant::fromEpochMilliseconds),
)

fun UserError.toEntity(): UserErrorEntity = UserErrorEntity(
    id = id,
    conversationId = conversationId,
    category = category.name,
    original = original,
    corrected = corrected,
    explanation = explanation,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
    resolvedAtEpochMillis = resolvedAt?.toEpochMilliseconds(),
)

fun VocabularyItemEntity.toDomain(): VocabularyItem = VocabularyItem(
    id = id,
    word = word,
    translation = translation,
    definition = definition,
    example = example,
    sourceConversationId = sourceConversationId,
    status = enumOrDefault(status, VocabularyStatus.NEW),
    srs = SrsState(
        easeFactor = easeFactor,
        intervalDays = intervalDays,
        repetitionCount = repetitionCount,
        dueAt = Instant.fromEpochMilliseconds(dueAtEpochMillis),
    ),
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
)

fun VocabularyItem.toEntity(): VocabularyItemEntity = VocabularyItemEntity(
    id = id,
    word = word,
    translation = translation,
    definition = definition,
    example = example,
    sourceConversationId = sourceConversationId,
    status = status.name,
    easeFactor = srs.easeFactor,
    intervalDays = srs.intervalDays,
    repetitionCount = srs.repetitionCount,
    dueAtEpochMillis = srs.dueAt.toEpochMilliseconds(),
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    type = enumOrDefault(type, ExerciseType.FILL_GAP),
    question = question,
    options = runCatching { json.decodeFromString(stringListSerializer, options) }
        .getOrDefault(emptyList()),
    correctAnswer = correctAnswer,
    explanation = explanation,
    sourceErrorId = sourceErrorId,
    srs = SrsState(
        easeFactor = easeFactor,
        intervalDays = intervalDays,
        repetitionCount = repetitionCount,
        dueAt = Instant.fromEpochMilliseconds(dueAtEpochMillis),
    ),
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    type = type.name,
    question = question,
    options = json.encodeToString(stringListSerializer, options),
    correctAnswer = correctAnswer,
    explanation = explanation,
    sourceErrorId = sourceErrorId,
    easeFactor = srs.easeFactor,
    intervalDays = srs.intervalDays,
    repetitionCount = srs.repetitionCount,
    dueAtEpochMillis = srs.dueAt.toEpochMilliseconds(),
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)

fun ExerciseAttempt.toEntity(): ExerciseAttemptEntity = ExerciseAttemptEntity(
    id = id,
    exerciseId = exerciseId,
    userAnswer = userAnswer,
    isCorrect = isCorrect,
    attemptedAtEpochMillis = attemptedAt.toEpochMilliseconds(),
)
