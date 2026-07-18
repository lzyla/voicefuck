package com.aienglishcoach.core.domain.revision

import com.aienglishcoach.core.domain.model.ReviewGrade
import com.aienglishcoach.core.domain.model.SrsState
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

/**
 * Pure implementation of the SM-2 spaced-repetition algorithm used for both
 * vocabulary items and exercises.
 *
 * - Grade below 3 ("again"): repetition count resets, the item is due again
 *   in [RETRY_DELAY_MINUTES] so it is re-asked within the same session.
 * - Grade 3+: interval grows 1 day -> 6 days -> previous * easeFactor.
 * - Ease factor is adjusted by the standard SM-2 formula and clamped to
 *   [SrsState.MIN_EASE_FACTOR].
 */
class Sm2Scheduler @Inject constructor() {

    fun next(state: SrsState, grade: ReviewGrade, now: Instant): SrsState {
        val quality = grade.quality

        val newEase = max(
            SrsState.MIN_EASE_FACTOR,
            state.easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)),
        )

        if (quality < 3) {
            return state.copy(
                easeFactor = newEase,
                repetitionCount = 0,
                intervalDays = 0,
                dueAt = now + RETRY_DELAY_MINUTES.minutes,
            )
        }

        val newRepetition = state.repetitionCount + 1
        val newIntervalDays = when (newRepetition) {
            1 -> 1
            2 -> 6
            else -> max(1, (state.intervalDays * newEase).roundToInt())
        }

        return state.copy(
            easeFactor = newEase,
            repetitionCount = newRepetition,
            intervalDays = newIntervalDays,
            dueAt = now + newIntervalDays.days,
        )
    }

    companion object {
        const val RETRY_DELAY_MINUTES = 10
    }
}
