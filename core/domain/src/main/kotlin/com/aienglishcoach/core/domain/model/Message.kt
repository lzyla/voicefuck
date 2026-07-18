package com.aienglishcoach.core.domain.model

import kotlinx.datetime.Instant

/** One utterance in a conversation, either the learner's or the tutor's. */
data class Message(
    val id: Long = 0,
    val conversationId: Long,
    val role: MessageRole,
    val content: String,
    /** Polish translation shown on demand (long-press), lazily fetched. */
    val translation: String? = null,
    /** Optional path to a recorded audio file for this utterance. */
    val audioPath: String? = null,
    val createdAt: Instant,
)

enum class MessageRole { USER, ASSISTANT }
