package com.aienglishcoach.core.domain.usecase.pronunciation

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.domain.repository.PronunciationRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EvaluatePronunciationUseCaseTest {

    private val pronunciationRepository: PronunciationRepository = mockk(relaxed = true)
    private val statisticsRepository: StatisticsRepository = mockk(relaxed = true)
    private val aiTutorService: AiTutorService = mockk(relaxed = true)

    private lateinit var useCase: EvaluatePronunciationUseCase

    @Before
    fun setUp() {
        coEvery { pronunciationRepository.insert(any()) } returns 1L
        useCase = EvaluatePronunciationUseCase(
            pronunciationRepository = pronunciationRepository,
            statisticsRepository = statisticsRepository,
            aiTutorService = aiTutorService,
        )
    }

    @Test
    fun `perfect match scores 100 and skips AI feedback`() = runTest {
        val result = useCase(
            expectedText = "Thirty three things",
            recognizedText = "thirty three things",
            recognizerConfidence = 1.0f,
        )

        assertEquals(100, result.score)
        assertNull(result.feedback)
        coVerify(exactly = 0) { aiTutorService.pronunciationFeedback(any(), any()) }
    }

    @Test
    fun `poor match scores low and requests AI feedback`() = runTest {
        coEvery {
            aiTutorService.pronunciationFeedback(any(), any())
        } returns AppResult.success("Spróbuj wydłużyć samogłoskę.")

        val result = useCase(
            expectedText = "world",
            recognizedText = "word",
            recognizerConfidence = 0.4f,
        )

        assertTrue("score should drop below threshold", result.score < 85)
        assertEquals("Spróbuj wydłużyć samogłoskę.", result.feedback)
    }

    @Test
    fun `result is persisted and booked into statistics`() = runTest {
        useCase("hello", "hello", 0.9f)

        coVerify { pronunciationRepository.insert(any()) }
        coVerify { statisticsRepository.recordPronunciationScore(any(), any()) }
    }
}
