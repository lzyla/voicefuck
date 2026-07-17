package com.aienglishcoach.core.data.mapper

import com.aienglishcoach.core.database.entity.AiMemoryEntity
import com.aienglishcoach.core.database.entity.DailyStatsEntity
import com.aienglishcoach.core.database.entity.PronunciationResultEntity
import com.aienglishcoach.core.database.entity.VoiceNoteEntity
import com.aienglishcoach.core.domain.model.AiMemory
import com.aienglishcoach.core.domain.model.DailyStats
import com.aienglishcoach.core.domain.model.MemoryKind
import com.aienglishcoach.core.domain.model.PronunciationResult
import com.aienglishcoach.core.domain.model.VoiceNote
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun PronunciationResultEntity.toDomain(): PronunciationResult = PronunciationResult(
    id = id,
    word = word,
    expectedText = expectedText,
    recognizedText = recognizedText,
    score = score,
    feedback = feedback,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
)

fun PronunciationResult.toEntity(): PronunciationResultEntity = PronunciationResultEntity(
    id = id,
    word = word,
    expectedText = expectedText,
    recognizedText = recognizedText,
    score = score,
    feedback = feedback,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)

fun VoiceNoteEntity.toDomain(): VoiceNote = VoiceNote(
    id = id,
    title = title,
    audioPath = audioPath,
    transcription = transcription,
    durationSeconds = durationSeconds,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
)

fun VoiceNote.toEntity(): VoiceNoteEntity = VoiceNoteEntity(
    id = id,
    title = title,
    audioPath = audioPath,
    transcription = transcription,
    durationSeconds = durationSeconds,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)

fun DailyStatsEntity.toDomain(): DailyStats = DailyStats(
    date = LocalDate.parse(date),
    conversationSeconds = conversationSeconds,
    messagesSent = messagesSent,
    wordsLearned = wordsLearned,
    exercisesDone = exercisesDone,
    exercisesCorrect = exercisesCorrect,
    pronunciationAverage = if (pronunciationAttempts > 0) {
        pronunciationScoreSum.toDouble() / pronunciationAttempts
    } else {
        null
    },
)

fun AiMemoryEntity.toDomain(): AiMemory = AiMemory(
    id = id,
    kind = enumOrDefault(kind, MemoryKind.FACT),
    content = content,
    embedding = embedding?.toFloatArray(),
    importance = importance,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
    updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMillis),
)

fun AiMemory.toEntity(): AiMemoryEntity = AiMemoryEntity(
    id = id,
    kind = kind.name,
    content = content,
    embedding = embedding?.toByteArray(),
    importance = importance,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
    updatedAtEpochMillis = updatedAt.toEpochMilliseconds(),
)

/** Embeddings are stored as little-endian float32 BLOBs. */
fun FloatArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(size * Float.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
    forEach(buffer::putFloat)
    return buffer.array()
}

fun ByteArray.toFloatArray(): FloatArray {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    return FloatArray(size / Float.SIZE_BYTES) { buffer.getFloat(it * Float.SIZE_BYTES) }
}
