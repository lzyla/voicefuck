package com.aienglishcoach.core.ai

import com.aienglishcoach.core.ai.prompt.PromptBuilder
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.common.result.getOrNull
import com.aienglishcoach.core.domain.model.ErrorCategory
import com.aienglishcoach.core.domain.model.ExerciseType
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.MessageRole
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.network.OpenAiDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OpenAiTutorServiceTest {

    private val openAiDataSource: OpenAiDataSource = mockk()
    private lateinit var service: OpenAiTutorService

    private val now = Instant.parse("2026-07-17T10:00:00Z")
    private val history = listOf(
        Message(conversationId = 1, role = MessageRole.ASSISTANT, content = "Hi!", createdAt = now),
        Message(conversationId = 1, role = MessageRole.USER, content = "I am agree", createdAt = now),
    )

    @Before
    fun setUp() {
        service = OpenAiTutorService(openAiDataSource, PromptBuilder())
    }

    @Test
    fun `analysis JSON is parsed into domain model`() = runTest {
        coEvery {
            openAiDataSource.complete(any(), any(), any(), any(), jsonResponse = true)
        } returns AppResult.success(
            """
            {
              "summary": "Nice chat.",
              "fluency_score": 72,
              "focus_tip": "Drop 'am' before 'agree'.",
              "errors": [
                {"category": "grammar", "original": "I am agree", "corrected": "I agree", "explanation": "..."}
              ],
              "vocabulary": [
                {"word": "to agree", "translation": "zgadzać się"}
              ]
            }
            """.trimIndent(),
        )

        val analysis = service.analyzeConversation(history, UserPreferences()).getOrNull()!!

        assertEquals(72, analysis.fluencyScore)
        assertEquals(1, analysis.errors.size)
        assertEquals(ErrorCategory.GRAMMAR, analysis.errors.first().category)
        assertEquals("to agree", analysis.vocabulary.first().word)
    }

    @Test
    fun `malformed JSON maps to AiService error instead of crashing`() = runTest {
        coEvery {
            openAiDataSource.complete(any(), any(), any(), any(), jsonResponse = true)
        } returns AppResult.success("Sorry, I cannot do that")

        val result = service.analyzeConversation(history, UserPreferences())

        assertTrue((result as AppResult.Failure).error is AppError.AiService)
    }

    @Test
    fun `generated exercises drop entries with unknown source error ids`() = runTest {
        coEvery {
            openAiDataSource.complete(any(), any(), any(), any(), jsonResponse = true)
        } returns AppResult.success(
            """
            {
              "exercises": [
                {"source_error_id": 3, "type": "FILL_GAP", "question": "I ___ yesterday.", "correct_answer": "went", "explanation": "..."},
                {"source_error_id": 999, "type": "MULTIPLE_CHOICE", "question": "Pick", "options": ["a","b"], "correct_answer": "a", "explanation": "..."}
              ]
            }
            """.trimIndent(),
        )
        val errors = listOf(
            UserError(
                id = 3,
                category = ErrorCategory.GRAMMAR,
                original = "I goed",
                corrected = "I went",
                explanation = "...",
                createdAt = now,
            ),
        )

        val exercises = service.generateExercises(errors, UserPreferences()).getOrNull()!!

        assertEquals(2, exercises.size)
        assertEquals(3L, exercises[0].sourceErrorId)
        assertEquals(null, exercises[1].sourceErrorId)
        assertEquals(ExerciseType.MULTIPLE_CHOICE, exercises[1].type)
    }

    @Test
    fun `extracted memories are capped and clamped`() = runTest {
        val manyMemories = (1..10).joinToString(",") {
            """{"kind": "FACT", "content": "Fact $it", "importance": 9}"""
        }
        coEvery {
            openAiDataSource.complete(any(), any(), any(), any(), jsonResponse = true)
        } returns AppResult.success("""{"memories": [$manyMemories]}""")

        val memories = service.extractMemories(history, emptyList()).getOrNull()!!

        assertEquals(5, memories.size)
        assertTrue(memories.all { it.importance in 1..5 })
    }
}
