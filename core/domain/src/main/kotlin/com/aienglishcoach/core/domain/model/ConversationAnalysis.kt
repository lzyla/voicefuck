package com.aienglishcoach.core.domain.model

/**
 * Result of the AI post-conversation analysis: what went wrong, what new
 * vocabulary appeared and an overall summary with encouragement.
 */
data class ConversationAnalysis(
    val summary: String,
    /** 0–100 subjective fluency estimate for this conversation. */
    val fluencyScore: Int,
    val errors: List<DetectedError>,
    val vocabulary: List<DetectedVocabulary>,
    /** One concrete thing to focus on next time. */
    val focusTip: String,
)

/** An error as returned by the analyzer, before persisting as [UserError]. */
data class DetectedError(
    val category: ErrorCategory,
    val original: String,
    val corrected: String,
    val explanation: String,
)

/** New/valuable vocabulary spotted in the conversation. */
data class DetectedVocabulary(
    val word: String,
    val translation: String,
    val definition: String? = null,
    val example: String? = null,
)
