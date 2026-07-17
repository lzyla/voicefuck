package com.aienglishcoach.core.domain.usecase.pronunciation

import com.aienglishcoach.core.common.result.getOrNull
import com.aienglishcoach.core.domain.model.PronunciationResult
import com.aienglishcoach.core.domain.repository.PronunciationRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Scores a pronunciation attempt by combining recognizer confidence with the
 * text similarity between what was expected and what was recognized, then
 * (best effort) asks the AI for a short improvement tip. Works offline —
 * the AI feedback is optional and skipped on failure.
 */
class EvaluatePronunciationUseCase @Inject constructor(
    private val pronunciationRepository: PronunciationRepository,
    private val statisticsRepository: StatisticsRepository,
    private val aiTutorService: AiTutorService,
) {

    suspend operator fun invoke(
        expectedText: String,
        recognizedText: String,
        recognizerConfidence: Float?,
    ): PronunciationResult {
        val similarity = textSimilarity(expectedText, recognizedText)
        val confidence = recognizerConfidence?.toDouble() ?: similarity
        val score = ((similarity * SIMILARITY_WEIGHT + confidence * CONFIDENCE_WEIGHT) * 100)
            .roundToInt()
            .coerceIn(0, 100)

        val feedback = if (score < FEEDBACK_THRESHOLD) {
            aiTutorService.pronunciationFeedback(expectedText, recognizedText).getOrNull()
        } else {
            null
        }

        val result = PronunciationResult(
            word = expectedText,
            expectedText = expectedText,
            recognizedText = recognizedText,
            score = score,
            feedback = feedback,
            createdAt = Clock.System.now(),
        )
        val id = pronunciationRepository.insert(result)
        statisticsRepository.recordPronunciationScore(
            date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            score = score,
        )
        return result.copy(id = id)
    }

    /** Normalized Levenshtein similarity in 0..1. */
    private fun textSimilarity(a: String, b: String): Double {
        val left = normalize(a)
        val right = normalize(b)
        if (left.isEmpty() && right.isEmpty()) return 1.0
        val distance = levenshtein(left, right)
        val longest = max(left.length, right.length)
        return 1.0 - distance.toDouble() / longest
    }

    private fun normalize(text: String): String =
        text.trim().lowercase().replace(Regex("[^a-z0-9' ]"), "").replace(Regex("\\s+"), " ")

    private fun levenshtein(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val previous = IntArray(b.length + 1) { it }
        val current = IntArray(b.length + 1)
        for (i in 1..a.length) {
            current[0] = i
            for (j in 1..b.length) {
                val substitutionCost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(
                    current[j - 1] + 1,
                    previous[j] + 1,
                    previous[j - 1] + substitutionCost,
                )
            }
            current.copyInto(previous)
        }
        return previous[b.length]
    }

    companion object {
        private const val SIMILARITY_WEIGHT = 0.7
        private const val CONFIDENCE_WEIGHT = 0.3

        /** Ask the AI for a tip only when the attempt clearly needs work. */
        const val FEEDBACK_THRESHOLD = 85
    }
}
