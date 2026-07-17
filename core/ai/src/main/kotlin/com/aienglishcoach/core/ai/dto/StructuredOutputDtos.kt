package com.aienglishcoach.core.ai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON shapes returned by the model for structured-output calls.
 * These are `:core:ai` internals — they never leak into other modules.
 */

@Serializable
internal data class ConversationAnalysisDto(
    val summary: String = "",
    @SerialName("fluency_score")
    val fluencyScore: Int = 0,
    @SerialName("focus_tip")
    val focusTip: String = "",
    val errors: List<DetectedErrorDto> = emptyList(),
    val vocabulary: List<DetectedVocabularyDto> = emptyList(),
)

@Serializable
internal data class DetectedErrorDto(
    val category: String = "GRAMMAR",
    val original: String,
    val corrected: String,
    val explanation: String = "",
)

@Serializable
internal data class DetectedVocabularyDto(
    val word: String,
    val translation: String = "",
    val definition: String? = null,
    val example: String? = null,
)

@Serializable
internal data class GeneratedExercisesDto(
    val exercises: List<GeneratedExerciseDto> = emptyList(),
)

@Serializable
internal data class GeneratedExerciseDto(
    @SerialName("source_error_id")
    val sourceErrorId: Long? = null,
    val type: String = "FILL_GAP",
    val question: String,
    val options: List<String> = emptyList(),
    @SerialName("correct_answer")
    val correctAnswer: String,
    val explanation: String = "",
)

@Serializable
internal data class ExtractedMemoriesDto(
    val memories: List<ExtractedMemoryDto> = emptyList(),
)

@Serializable
internal data class ExtractedMemoryDto(
    val kind: String = "FACT",
    val content: String,
    val importance: Int = 3,
)
