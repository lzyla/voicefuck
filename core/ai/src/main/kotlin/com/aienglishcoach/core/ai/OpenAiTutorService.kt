package com.aienglishcoach.core.ai

import com.aienglishcoach.core.ai.dto.ConversationAnalysisDto
import com.aienglishcoach.core.ai.dto.ExtractedMemoriesDto
import com.aienglishcoach.core.ai.dto.GeneratedExercisesDto
import com.aienglishcoach.core.ai.prompt.PromptBuilder
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.domain.model.AiMemory
import com.aienglishcoach.core.domain.model.ConversationAnalysis
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.DetectedError
import com.aienglishcoach.core.domain.model.DetectedVocabulary
import com.aienglishcoach.core.domain.model.ErrorCategory
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseType
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.MemoryKind
import com.aienglishcoach.core.domain.model.MessageRole
import com.aienglishcoach.core.domain.model.SrsState
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.service.AiTutorService
import com.aienglishcoach.core.network.OpenAiDataSource
import com.aienglishcoach.core.network.dto.ChatMessageDto
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [AiTutorService] implementation on top of the OpenAI-compatible API.
 * Owns prompt assembly (via [PromptBuilder]) and structured-output parsing;
 * malformed model JSON is mapped to [AppError.AiService], never a crash.
 */
@Singleton
class OpenAiTutorService @Inject constructor(
    private val openAiDataSource: OpenAiDataSource,
    private val promptBuilder: PromptBuilder,
) : AiTutorService {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun generateReply(
        scenario: ConversationScenario,
        history: List<Message>,
        preferences: UserPreferences,
        memories: List<AiMemory>,
    ): AppResult<String> {
        val messages = buildList {
            add(system(promptBuilder.tutorSystemPrompt(scenario, preferences, memories)))
            history.forEach { message ->
                add(
                    ChatMessageDto(
                        role = when (message.role) {
                            MessageRole.USER -> ChatMessageDto.ROLE_USER
                            MessageRole.ASSISTANT -> ChatMessageDto.ROLE_ASSISTANT
                        },
                        content = message.content,
                    ),
                )
            }
            if (history.isEmpty()) {
                add(ChatMessageDto(ChatMessageDto.ROLE_USER, OPENING_TRIGGER))
            }
        }
        return openAiDataSource.complete(
            model = preferences.aiModel,
            messages = messages,
            temperature = CONVERSATION_TEMPERATURE,
            maxTokens = REPLY_MAX_TOKENS,
        )
    }

    override suspend fun analyzeConversation(
        history: List<Message>,
        preferences: UserPreferences,
    ): AppResult<ConversationAnalysis> = structured(
        systemPrompt = promptBuilder.analysisSystemPrompt(preferences),
        userContent = transcript(history),
        model = preferences.aiModel,
    ) { dto: ConversationAnalysisDto ->
        ConversationAnalysis(
            summary = dto.summary,
            fluencyScore = dto.fluencyScore.coerceIn(0, 100),
            focusTip = dto.focusTip,
            errors = dto.errors.map { error ->
                DetectedError(
                    category = enumOrDefault(error.category, ErrorCategory.GRAMMAR),
                    original = error.original,
                    corrected = error.corrected,
                    explanation = error.explanation,
                )
            },
            vocabulary = dto.vocabulary
                .filter { it.word.isNotBlank() }
                .map { vocab ->
                    DetectedVocabulary(
                        word = vocab.word,
                        translation = vocab.translation,
                        definition = vocab.definition,
                        example = vocab.example,
                    )
                },
        )
    }

    override suspend fun generateExercises(
        errors: List<UserError>,
        preferences: UserPreferences,
    ): AppResult<List<Exercise>> = structured(
        systemPrompt = promptBuilder.exerciseGenerationSystemPrompt(preferences),
        userContent = errors.joinToString("\n") { error ->
            "id=${error.id} | category=${error.category} | original=\"${error.original}\" | " +
                "corrected=\"${error.corrected}\" | explanation=\"${error.explanation}\""
        },
        model = preferences.aiModel,
    ) { dto: GeneratedExercisesDto ->
        val now = Clock.System.now()
        val validErrorIds = errors.map { it.id }.toSet()
        dto.exercises
            .filter { it.question.isNotBlank() && it.correctAnswer.isNotBlank() }
            .map { generated ->
                Exercise(
                    type = enumOrDefault(generated.type, ExerciseType.FILL_GAP),
                    question = generated.question,
                    options = generated.options,
                    correctAnswer = generated.correctAnswer,
                    explanation = generated.explanation,
                    sourceErrorId = generated.sourceErrorId?.takeIf { it in validErrorIds },
                    srs = SrsState(dueAt = now),
                    createdAt = now,
                )
            }
    }

    override suspend fun extractMemories(
        history: List<Message>,
        existing: List<AiMemory>,
    ): AppResult<List<AiMemory>> = structured(
        systemPrompt = promptBuilder.memoryExtractionSystemPrompt(existing),
        userContent = transcript(history),
        model = UserPreferences.DEFAULT_AI_MODEL,
    ) { dto: ExtractedMemoriesDto ->
        val now = Clock.System.now()
        dto.memories
            .filter { it.content.isNotBlank() }
            .take(MAX_MEMORIES_PER_CONVERSATION)
            .map { memory ->
                AiMemory(
                    kind = enumOrDefault(memory.kind, MemoryKind.FACT),
                    content = memory.content.trim(),
                    importance = memory.importance.coerceIn(1, 5),
                    createdAt = now,
                    updatedAt = now,
                )
            }
    }

    override suspend fun pronunciationFeedback(
        expected: String,
        recognized: String,
    ): AppResult<String> = openAiDataSource.complete(
        model = UserPreferences.DEFAULT_AI_MODEL,
        messages = listOf(
            system(promptBuilder.pronunciationFeedbackSystemPrompt()),
            ChatMessageDto(
                ChatMessageDto.ROLE_USER,
                "Expected: \"$expected\"\nRecognized: \"$recognized\"",
            ),
        ),
        temperature = 0.3,
        maxTokens = FEEDBACK_MAX_TOKENS,
    )

    override suspend fun translateToPolish(text: String): AppResult<String> =
        openAiDataSource.complete(
            model = UserPreferences.DEFAULT_AI_MODEL,
            messages = listOf(
                system(promptBuilder.translationSystemPrompt()),
                ChatMessageDto(ChatMessageDto.ROLE_USER, text),
            ),
            temperature = 0.2,
            maxTokens = TRANSLATION_MAX_TOKENS,
        )

    /** Runs a JSON-mode completion and parses it into [R] via [transform]. */
    private suspend inline fun <reified T, R> structured(
        systemPrompt: String,
        userContent: String,
        model: String,
        crossinline transform: (T) -> R,
    ): AppResult<R> {
        val completion = openAiDataSource.complete(
            model = model,
            messages = listOf(system(systemPrompt), ChatMessageDto(ChatMessageDto.ROLE_USER, userContent)),
            temperature = STRUCTURED_TEMPERATURE,
            jsonResponse = true,
        )
        return when (completion) {
            is AppResult.Failure -> completion
            is AppResult.Success -> try {
                AppResult.success(transform(json.decodeFromString<T>(completion.value)))
            } catch (exception: Exception) {
                Timber.w(exception, "Failed to parse structured AI output")
                AppResult.failure(AppError.AiService("Malformed AI response"))
            }
        }
    }

    private fun system(content: String) = ChatMessageDto(ChatMessageDto.ROLE_SYSTEM, content)

    private fun transcript(history: List<Message>): String =
        history.joinToString("\n") { message ->
            val speaker = when (message.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
            }
            "$speaker: ${message.content}"
        }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
        enumValues<T>().find { it.name.equals(name, ignoreCase = true) } ?: default

    private companion object {
        const val OPENING_TRIGGER =
            "(Start the conversation with a short, friendly opening line and a question.)"
        const val CONVERSATION_TEMPERATURE = 0.8
        const val STRUCTURED_TEMPERATURE = 0.2
        const val REPLY_MAX_TOKENS = 200
        const val FEEDBACK_MAX_TOKENS = 150
        const val TRANSLATION_MAX_TOKENS = 300
        const val MAX_MEMORIES_PER_CONVERSATION = 5
    }
}
