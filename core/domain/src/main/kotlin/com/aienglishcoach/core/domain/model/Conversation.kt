package com.aienglishcoach.core.domain.model

import kotlinx.datetime.Instant

/** A single voice conversation session between the learner and the AI tutor. */
data class Conversation(
    val id: Long = 0,
    val title: String,
    val scenario: ConversationScenario,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val durationSeconds: Int = 0,
    val status: ConversationStatus = ConversationStatus.ACTIVE,
    /** Short AI-generated summary, filled in after post-conversation analysis. */
    val summary: String? = null,
)

enum class ConversationStatus {
    /** Conversation is in progress. */
    ACTIVE,

    /** Conversation ended, analysis has not completed yet. */
    ENDED,

    /** Conversation ended and the post-conversation analysis is stored. */
    ANALYZED,
}
