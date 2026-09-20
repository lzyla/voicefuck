package com.aienglishcoach.core.data.mapper

import com.aienglishcoach.core.database.entity.ConversationEntity
import com.aienglishcoach.core.database.entity.MessageEntity
import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.ConversationStatus
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.MessageRole
import kotlinx.datetime.Instant

fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    title = title,
    scenario = enumOrDefault(scenario, ConversationScenario.FREE_TALK),
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis),
    endedAt = endedAtEpochMillis?.let(Instant::fromEpochMilliseconds),
    durationSeconds = durationSeconds,
    status = enumOrDefault(status, ConversationStatus.ENDED),
    summary = summary,
)

fun Conversation.toEntity(): ConversationEntity = ConversationEntity(
    id = id,
    title = title,
    scenario = scenario.name,
    startedAtEpochMillis = startedAt.toEpochMilliseconds(),
    endedAtEpochMillis = endedAt?.toEpochMilliseconds(),
    durationSeconds = durationSeconds,
    status = status.name,
    summary = summary,
)

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    conversationId = conversationId,
    role = enumOrDefault(role, MessageRole.ASSISTANT),
    content = content,
    translation = translation,
    audioPath = audioPath,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
)

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    conversationId = conversationId,
    role = role.name,
    content = content,
    translation = translation,
    audioPath = audioPath,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)

/** Defensive enum parsing: unknown values degrade gracefully, never crash. */
internal inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
    enumValues<T>().find { it.name == name } ?: default
