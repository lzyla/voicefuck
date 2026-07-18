package com.aienglishcoach.core.domain.service

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.domain.model.AiMemory
import com.aienglishcoach.core.domain.model.ConversationAnalysis
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.core.domain.model.UserPreferences

/**
 * Port to the conversational AI. Implemented in `:core:ai` on top of the
 * OpenAI-compatible client; the domain layer knows only this contract.
 */
interface AiTutorService {

    /**
     * Produces the tutor's next reply given the conversation so far,
     * the learner's profile and the retrieved long-term memories.
     */
    suspend fun generateReply(
        scenario: ConversationScenario,
        history: List<Message>,
        preferences: UserPreferences,
        memories: List<AiMemory>,
    ): AppResult<String>

    /** Analyzes a finished conversation: errors, vocabulary, summary. */
    suspend fun analyzeConversation(
        history: List<Message>,
        preferences: UserPreferences,
    ): AppResult<ConversationAnalysis>

    /** Generates exercises targeting the given unresolved errors. */
    suspend fun generateExercises(
        errors: List<UserError>,
        preferences: UserPreferences,
    ): AppResult<List<Exercise>>

    /** Extracts new long-term memories from a finished conversation. */
    suspend fun extractMemories(
        history: List<Message>,
        existing: List<AiMemory>,
    ): AppResult<List<AiMemory>>

    /** Short feedback on a mispronounced word (expected vs recognized). */
    suspend fun pronunciationFeedback(
        expected: String,
        recognized: String,
    ): AppResult<String>

    /** Translates an English utterance into Polish (on-demand, long-press). */
    suspend fun translateToPolish(text: String): AppResult<String>
}
