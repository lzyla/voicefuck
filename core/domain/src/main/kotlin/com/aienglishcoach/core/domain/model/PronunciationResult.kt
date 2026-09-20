package com.aienglishcoach.core.domain.model

import kotlinx.datetime.Instant

/** Outcome of a single pronunciation practice attempt. */
data class PronunciationResult(
    val id: Long = 0,
    /** The word or phrase that was practiced. */
    val word: String,
    /** What the learner was asked to say. */
    val expectedText: String,
    /** What the speech recognizer heard. */
    val recognizedText: String,
    /** 0–100 score combining recognizer confidence and text similarity. */
    val score: Int,
    /** Short AI feedback on how to improve. */
    val feedback: String? = null,
    val createdAt: Instant,
)
