package com.aienglishcoach.feature.conversation

import androidx.lifecycle.SavedStateHandle
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.service.SpeechEvent
import com.aienglishcoach.core.domain.service.SpeechToTextService
import com.aienglishcoach.core.domain.service.TextToSpeechService
import com.aienglishcoach.core.domain.usecase.conversation.EndConversationUseCase
import com.aienglishcoach.core.domain.usecase.conversation.ObserveConversationDetailUseCase
import com.aienglishcoach.core.domain.usecase.conversation.SendMessageUseCase
import com.aienglishcoach.core.domain.usecase.conversation.StartConversationUseCase
import com.aienglishcoach.core.domain.usecase.conversation.StartedConversation
import com.aienglishcoach.core.domain.usecase.conversation.TranslateMessageUseCase
import com.aienglishcoach.core.domain.usecase.settings.ObservePreferencesUseCase
import com.aienglishcoach.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConversationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val startConversation: StartConversationUseCase = mockk()
    private val sendMessage: SendMessageUseCase = mockk()
    private val endConversation: EndConversationUseCase = mockk(relaxed = true)
    private val translateMessage: TranslateMessageUseCase = mockk()
    private val observeConversationDetail: ObserveConversationDetailUseCase = mockk()
    private val observePreferences: ObservePreferencesUseCase = mockk()
    private val speechToText: SpeechToTextService = mockk(relaxed = true)
    private val textToSpeech: TextToSpeechService = mockk(relaxed = true)

    private val speechEvents = MutableSharedFlow<SpeechEvent>()

    private fun viewModel(conversationId: Long = ConversationViewModel.NEW_CONVERSATION_ID) =
        ConversationViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(ConversationViewModel.ARG_CONVERSATION_ID to conversationId),
            ),
            startConversation = startConversation,
            sendMessage = sendMessage,
            endConversation = endConversation,
            translateMessage = translateMessage,
            observeConversationDetail = observeConversationDetail,
            observePreferences = observePreferences,
            speechToText = speechToText,
            textToSpeech = textToSpeech,
        )

    @Before
    fun setUp() {
        every { observePreferences() } returns flowOf(UserPreferences())
        every { observeConversationDetail(any()) } returns emptyFlow()
        every { speechToText.listen() } returns speechEvents
        coEvery { textToSpeech.speak(any()) } returns true
    }

    @Test
    fun `new conversation starts in scenario selection`() {
        val vm = viewModel()

        assertEquals(ConversationPhase.ScenarioSelection, vm.uiState.value.phase)
    }

    @Test
    fun `selecting a scenario starts the conversation and speaks the greeting`() = runTest {
        coEvery { startConversation(ConversationScenario.TRAVEL) } returns AppResult.success(
            StartedConversation(conversationId = 9, greeting = "Hello!", greetingMessageId = 1),
        )

        val vm = viewModel()
        vm.selectScenario(ConversationScenario.TRAVEL)

        assertEquals(ConversationPhase.Active, vm.uiState.value.phase)
        // Greeting spoken and finished -> mic returns to Idle.
        assertEquals(MicUiState.Idle, vm.uiState.value.micState)
    }

    @Test
    fun `failed start surfaces the error and returns mic to idle`() = runTest {
        coEvery { startConversation(any()) } returns AppResult.failure(AppError.MissingApiKey)

        val vm = viewModel()
        vm.selectScenario(ConversationScenario.FREE_TALK)

        assertEquals(AppError.MissingApiKey, vm.uiState.value.error)
        assertEquals(MicUiState.Idle, vm.uiState.value.micState)
    }

    @Test
    fun `final speech result sends the message and speaks the reply`() = runTest {
        coEvery { startConversation(any()) } returns AppResult.success(
            StartedConversation(9, "Hi!", 1),
        )
        coEvery { sendMessage(9, "I like coffee") } returns AppResult.success(
            com.aienglishcoach.core.domain.model.Message(
                id = 2,
                conversationId = 9,
                role = com.aienglishcoach.core.domain.model.MessageRole.ASSISTANT,
                content = "Me too!",
                createdAt = kotlinx.datetime.Clock.System.now(),
            ),
        )

        val vm = viewModel()
        vm.selectScenario(ConversationScenario.FREE_TALK)
        vm.onMicTapped()
        speechEvents.emit(SpeechEvent.Result("I like coffee", confidence = 0.9f))

        assertEquals(MicUiState.Idle, vm.uiState.value.micState)
        assertEquals(null, vm.uiState.value.error)
    }

    @Test
    fun `speech error returns mic to idle with error surfaced`() = runTest {
        coEvery { startConversation(any()) } returns AppResult.success(
            StartedConversation(9, "Hi!", 1),
        )

        val vm = viewModel()
        vm.selectScenario(ConversationScenario.FREE_TALK)
        vm.onMicTapped()
        speechEvents.emit(
            SpeechEvent.Error(
                AppError.SpeechRecognition(
                    com.aienglishcoach.core.common.result.SpeechErrorReason.NO_MATCH,
                ),
            ),
        )

        assertEquals(MicUiState.Idle, vm.uiState.value.micState)
    }
}
