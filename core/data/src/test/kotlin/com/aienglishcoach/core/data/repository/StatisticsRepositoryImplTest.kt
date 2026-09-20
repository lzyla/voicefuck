package com.aienglishcoach.core.data.repository

import app.cash.turbine.test
import com.aienglishcoach.core.database.dao.DailyStatsDao
import com.aienglishcoach.core.database.entity.DailyStatsEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StatisticsRepositoryImplTest {

    private val dailyStatsDao: DailyStatsDao = mockk(relaxed = true)
    private lateinit var repository: StatisticsRepositoryImpl

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun day(daysAgo: Int, seconds: Int = 60) = DailyStatsEntity(
        date = today.minus(daysAgo, DateTimeUnit.DAY).toString(),
        conversationSeconds = seconds,
        messagesSent = 2,
        wordsLearned = 1,
        exercisesDone = 4,
        exercisesCorrect = 3,
    )

    @Before
    fun setUp() {
        repository = StatisticsRepositoryImpl(dailyStatsDao)
    }

    @Test
    fun `streak counts consecutive active days ending today`() = runTest {
        every { dailyStatsDao.observeRecent(any()) } returns flowOf(
            listOf(day(0), day(1), day(2), day(4)),
        )

        repository.observeOverview().test {
            val overview = awaitItem()
            assertEquals(3, overview.streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak survives a not-yet-practiced today`() = runTest {
        every { dailyStatsDao.observeRecent(any()) } returns flowOf(
            listOf(day(1), day(2)),
        )

        repository.observeOverview().test {
            assertEquals(2, awaitItem().streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `accuracy is computed from totals`() = runTest {
        every { dailyStatsDao.observeRecent(any()) } returns flowOf(
            listOf(day(0), day(1)),
        )

        repository.observeOverview().test {
            // 6 correct of 8 done -> 75%
            assertEquals(75, awaitItem().exerciseAccuracyPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addToDay accumulates deltas into the existing row`() = runTest {
        coEvery { dailyStatsDao.getByDate(today.toString()) } returns
            DailyStatsEntity(date = today.toString(), conversationSeconds = 100)

        repository.addToDay(today, conversationSeconds = 60, messagesSent = 1)

        coVerify {
            dailyStatsDao.upsert(
                match { it.conversationSeconds == 160 && it.messagesSent == 1 },
            )
        }
    }
}
