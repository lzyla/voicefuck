package com.aienglishcoach.core.domain.model

import kotlinx.datetime.Instant

/**
 * A language mistake detected by the AI in the learner's speech.
 * Errors are the primary input for personalized exercise generation.
 */
data class UserError(
    val id: Long = 0,
    /** Conversation the error was detected in; null for manually added ones. */
    val conversationId: Long? = null,
    val category: ErrorCategory,
    /** What the learner actually said. */
    val original: String,
    /** The corrected version. */
    val corrected: String,
    /** Short, learner-friendly explanation of the rule that was broken. */
    val explanation: String,
    val createdAt: Instant,
    /** Set when the learner has mastered exercises generated from this error. */
    val resolvedAt: Instant? = null,
)

enum class ErrorCategory { GRAMMAR, VOCABULARY, PRONUNCIATION, FLUENCY }
