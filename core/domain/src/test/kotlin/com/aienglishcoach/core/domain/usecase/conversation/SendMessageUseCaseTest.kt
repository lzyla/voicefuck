package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.MessageRole
import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import com.aienglishcoach.core.domain.service.MemoryRetrievalService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTest {

    private val conversationRepository: ConversationRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk()
    private val statisticsRepository: StatisticsRepository = mockk(relaxed = true)
    private val aiTutorService: AiTutorService = mockk()
    private val memoryRetrievalService: MemoryRetrievalService = mockk()

    private lateinit var useCase: SendMessageUseCase

    private val conversation = Conversation(
        id = 5,
        title = "Free talk",
        scenario = ConversationScenario.FREE_TALK,
        startedAt = Instant.parse("2026-07-17T09:00:00Z"),
    )

    @Before
    fun setUp() {
        coEvery { conversationRepository.getConversation(5) } returns conversation
        coEvery { conversationRepository.getMessages(5) } returns emptyList()
        coEvery { conversationRepository.addMessage(any()) } returns 11L
        coEvery { settingsRepository.preferences } returns flowOf(UserPreferences())
        coEvery { memoryRetrievalService.retrieveRelevant(any(), any()) } returns emptyList()

        useCase = SendMessageUseCase(
            conversationRepository = conversationRepository,
            settingsRepository = settingsRepository,
            statisticsRepository = statisticsRepository,
            aiTutorService = aiTutorService,
            memoryRetrievalService = memoryRetrievalService,
        )
    }

    @Test
    fun `stores user message before requesting the reply`() = runTest {
        coEvery {
            aiTutorService.generateReply(any(), any(), any(), any())
        } returns AppResult.success("Nice to hear that!")

        val result = useCase(5, "I am fine")

        assertTrue(result is AppResult.Success)
        coVerifyOrder {
            conversationRepository.addMessage(
                match { it.role == MessageRole.USER && it.content == "I am fine" },
            )
            aiTutorService.generateReply(any(), any(), any(), any())
            conversationRepository.addMessage(
                match { it.role == MessageRole.ASSISTANT && it.content == "Nice to hear that!" },
            )
        }
    }

    @Test
    fun `user message survives an AI failure`() = runTest {
        coEvery {
            aiTutorService.generateReply(any(), any(), any(), any())
        } returns AppResult.failure(AppError.Network)

        val result = useCase(5, "Hello there")

        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.Network, (result as AppResult.Failure).error)
        coVerify(exactly = 1) {
            conversationRepository.addMessage(match { it.role == MessageRole.USER })
        }
    }

    @Test
    fun `missing conversation fails with storage error`() = runTest {
        coEvery { conversationRepository.getConversation(99) } returns null

        val result = useCase(99, "Hi")

        assertTrue((result as AppResult.Failure).error is AppError.Storage)
    }

    @Test
    fun `memories are retrieved with the user utterance as query`() = runTest {
        coEvery {
            aiTutorService.generateReply(any(), any(), any(), any())
        } returns AppResult.success("ok")

        useCase(5, "I love hiking in the mountains")

        coVerify {
            memoryRetrievalService.retrieveRelevant("I love hiking in the mountains", any())
        }
    }
}
