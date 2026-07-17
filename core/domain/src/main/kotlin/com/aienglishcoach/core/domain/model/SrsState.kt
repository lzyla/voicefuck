package com.aienglishcoach.core.domain.model

import kotlinx.datetime.Instant

/**
 * Spaced-repetition scheduling state shared by vocabulary items and exercises.
 * Values follow the SM-2 algorithm.
 */
data class SrsState(
    val easeFactor: Double = INITIAL_EASE_FACTOR,
    val intervalDays: Int = 0,
    val repetitionCount: Int = 0,
    val dueAt: Instant,
) {
    companion object {
        const val INITIAL_EASE_FACTOR = 2.5
        const val MIN_EASE_FACTOR = 1.3
    }
}

/**
 * Learner's self- or system-assessed answer quality for an SRS review,
 * mapped to the SM-2 0–5 quality scale.
 */
enum class ReviewGrade(val quality: Int) {
    AGAIN(0),
    HARD(3),
    GOOD(4),
    EASY(5),
}
