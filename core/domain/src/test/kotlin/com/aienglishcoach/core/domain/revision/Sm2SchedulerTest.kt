package com.aienglishcoach.core.domain.revision

import com.aienglishcoach.core.domain.model.ReviewGrade
import com.aienglishcoach.core.domain.model.SrsState
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class Sm2SchedulerTest {

    private val scheduler = Sm2Scheduler()
    private val now = Instant.parse("2026-07-17T10:00:00Z")

    private fun freshState() = SrsState(dueAt = now)

    @Test
    fun `first successful review schedules one day ahead`() {
        val next = scheduler.next(freshState(), ReviewGrade.GOOD, now)

        assertEquals(1, next.repetitionCount)
        assertEquals(1, next.intervalDays)
        assertEquals(now + 1.days, next.dueAt)
    }

    @Test
    fun `second successful review schedules six days ahead`() {
        val first = scheduler.next(freshState(), ReviewGrade.GOOD, now)
        val second = scheduler.next(first, ReviewGrade.GOOD, now)

        assertEquals(2, second.repetitionCount)
        assertEquals(6, second.intervalDays)
        assertEquals(now + 6.days, second.dueAt)
    }

    @Test
    fun `third successful review multiplies interval by ease factor`() {
        var state = freshState()
        repeat(3) { state = scheduler.next(state, ReviewGrade.GOOD, now) }

        assertEquals(3, state.repetitionCount)
        assertTrue("interval should grow beyond 6 days", state.intervalDays > 6)
    }

    @Test
    fun `failed review resets repetitions and reschedules within session`() {
        val progressed = scheduler.next(
            scheduler.next(freshState(), ReviewGrade.GOOD, now),
            ReviewGrade.GOOD,
            now,
        )

        val failed = scheduler.next(progressed, ReviewGrade.AGAIN, now)

        assertEquals(0, failed.repetitionCount)
        assertEquals(0, failed.intervalDays)
        assertEquals(now + Sm2Scheduler.RETRY_DELAY_MINUTES.minutes, failed.dueAt)
    }

    @Test
    fun `failed reviews lower ease factor but never below minimum`() {
        var state = freshState()
        repeat(20) { state = scheduler.next(state, ReviewGrade.AGAIN, now) }

        assertEquals(SrsState.MIN_EASE_FACTOR, state.easeFactor, 0.0001)
    }

    @Test
    fun `easy grade raises ease factor`() {
        val next = scheduler.next(freshState(), ReviewGrade.EASY, now)

        assertTrue(next.easeFactor > SrsState.INITIAL_EASE_FACTOR)
    }
}
