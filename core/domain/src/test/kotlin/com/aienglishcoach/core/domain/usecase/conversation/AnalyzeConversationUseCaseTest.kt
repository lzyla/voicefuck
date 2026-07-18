package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.domain.model.ConversationAnalysis
import com.aienglishcoach.core.domain.model.ConversationStatus
import com.aienglishcoach.core.domain.model.DetectedError
import com.aienglishcoach.core.domain.model.DetectedVocabulary
import com.aienglishcoach.core.domain.model.ErrorCategory
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.MessageRole
import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.repository.UserErrorRepository
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyzeConversationUseCaseTest {

    private val conversationRepository: ConversationRepository = mockk(relaxed = true)
    private val userErrorRepository: UserErrorRepository = mockk(relaxed = true)
    private val vocabularyRepository: VocabularyRepository = mockk(relaxed = true)
    private val statisticsRepository: StatisticsRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk()
    private val aiTutorService: AiTutorService = mockk()

    private lateinit var useCase: AnalyzeConversationUseCase

    private val now = Instant.parse("2026-07-17T09:00:00Z")

    private fun message(role: MessageRole, content: String) =
        Message(conversationId = 1, role = role, content = content, createdAt = now)

    @Before
    fun setUp() {
        coEvery { settingsRepository.preferences } returns flowOf(UserPreferences())
        useCase = AnalyzeConversationUseCase(
            conversationRepository = conversationRepository,
            userErrorRepository = userErrorRepository,
            vocabularyRepository = vocabularyRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository,
            aiTutorService = aiTutorService,
        )
    }

    @Test
    fun `analysis persists errors vocabulary and summary`() = runTest {
        coEvery { conversationRepository.getMessages(1) } returns listOf(
            message(MessageRole.ASSISTANT, "Hi! How are you?"),
            message(MessageRole.USER, "I am agree with you"),
        )
        coEvery { aiTutorService.analyzeConversation(any(), any()) } returns AppResult.success(
            ConversationAnalysis(
                summary = "Good conversation.",
                fluencyScore = 70,
                focusTip = "Mind 'agree' without 'am'.",
                errors = listOf(
                    DetectedError(
                        category = ErrorCategory.GRAMMAR,
                        original = "I am agree",
                        corrected = "I agree",
                        explanation = "'Agree' jest czasownikiem.",
                    ),
                ),
                vocabulary = listOf(
                    DetectedVocabulary(word = "to agree", translation = "zgadzać się"),
                ),
            ),
        )

        val result = useCase(1)

        assertTrue(result is AppResult.Success)
        coVerify { userErrorRepository.insertAll(match { it.size == 1 }) }
        coVerify { vocabularyRepository.upsertAll(match { it.size == 1 }) }
        coVerify {
            conversationRepository.updateSummary(1, "Good conversation.", ConversationStatus.ANALYZED)
        }
    }

    @Test
    fun `conversation without user utterances is not analyzed`() = runTest {
        coEvery { conversationRepository.getMessages(1) } returns listOf(
            message(MessageRole.ASSISTANT, "Hello!"),
        )

        val result = useCase(1)

        assertTrue(result is AppResult.Failure)
        coVerify(exactly = 0) { aiTutorService.analyzeConversation(any(), any()) }
    }
}
