package com.aienglishcoach.feature.practice.vocabulary

import com.aienglishcoach.core.domain.model.ReviewGrade
import com.aienglishcoach.core.domain.model.SrsState
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.usecase.vocabulary.GetDueVocabularyUseCase
import com.aienglishcoach.core.domain.usecase.vocabulary.ReviewVocabularyItemUseCase
import com.aienglishcoach.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VocabularyReviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDueVocabulary: GetDueVocabularyUseCase = mockk()
    private val reviewVocabularyItem: ReviewVocabularyItemUseCase = mockk()

    private fun createViewModel() = VocabularyReviewViewModel(
        getDueVocabulary = getDueVocabulary,
        reviewVocabularyItem = reviewVocabularyItem,
    )

    @Test
    fun `session loads due vocabulary on init`() = runTest {
        val due = listOf(item(id = 1), item(id = 2))
        coEvery { getDueVocabulary(any()) } returns due

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(due, state.items)
        assertEquals(due.first(), state.currentItem)
        assertFalse(state.isRevealed)
    }

    @Test
    fun `grading a revealed card updates the item and advances`() = runTest {
        val first = item(id = 1)
        val second = item(id = 2)
        coEvery { getDueVocabulary(any()) } returns listOf(first, second)
        coJustRun { reviewVocabularyItem(any(), any()) }

        val viewModel = createViewModel()
        viewModel.revealCard()
        assertTrue(viewModel.uiState.value.isRevealed)

        viewModel.grade(ReviewGrade.GOOD)

        coVerify(exactly = 1) { reviewVocabularyItem(first, ReviewGrade.GOOD) }
        with(viewModel.uiState.value) {
            assertEquals(1, currentIndex)
            assertEquals(second, currentItem)
            assertEquals(1, reviewedCount)
            assertFalse(isRevealed)
            assertFalse(isFinished)
        }
    }

    @Test
    fun `each grade is passed through to the use case`() = runTest {
        val items = listOf(item(id = 1), item(id = 2), item(id = 3), item(id = 4))
        coEvery { getDueVocabulary(any()) } returns items
        coJustRun { reviewVocabularyItem(any(), any()) }

        val viewModel = createViewModel()
        listOf(
            ReviewGrade.AGAIN,
            ReviewGrade.HARD,
            ReviewGrade.GOOD,
            ReviewGrade.EASY,
        ).forEach { grade ->
            viewModel.revealCard()
            viewModel.grade(grade)
        }

        coVerify(exactly = 1) { reviewVocabularyItem(items[0], ReviewGrade.AGAIN) }
        coVerify(exactly = 1) { reviewVocabularyItem(items[1], ReviewGrade.HARD) }
        coVerify(exactly = 1) { reviewVocabularyItem(items[2], ReviewGrade.GOOD) }
        coVerify(exactly = 1) { reviewVocabularyItem(items[3], ReviewGrade.EASY) }
        assertTrue(viewModel.uiState.value.isFinished)
        assertEquals(4, viewModel.uiState.value.reviewedCount)
    }

    @Test
    fun `grading is ignored until the card is revealed`() = runTest {
        coEvery { getDueVocabulary(any()) } returns listOf(item(id = 1))
        coJustRun { reviewVocabularyItem(any(), any()) }

        val viewModel = createViewModel()
        viewModel.grade(ReviewGrade.GOOD)

        coVerify(exactly = 0) { reviewVocabularyItem(any(), any()) }
        assertEquals(0, viewModel.uiState.value.reviewedCount)
    }

    @Test
    fun `empty due list is reported as empty session`() = runTest {
        coEvery { getDueVocabulary(any()) } returns emptyList()

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isEmpty)
        assertFalse(viewModel.uiState.value.isFinished)
    }

    private fun item(id: Long) = VocabularyItem(
        id = id,
        word = "word$id",
        translation = "słowo$id",
        srs = SrsState(dueAt = NOW),
        createdAt = NOW,
    )

    private companion object {
        val NOW: Instant = Instant.parse("2026-01-01T00:00:00Z")
    }
}
