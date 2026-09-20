package com.aienglishcoach.feature.onboarding

import com.aienglishcoach.core.domain.model.EnglishLevel

/**
 * A single placement-test item: a short English grammar/vocab question with
 * one correct option. Static, on-device sample bank — there is no adaptive
 * item-response backend, so difficulty only informs scoring, not which
 * question is shown next.
 */
data class PlacementQuestion(
    val prompt: String,
    val options: List<String>,
    val correctOptionIndex: Int,
)

val PLACEMENT_QUESTIONS = listOf(
    PlacementQuestion(
        prompt = "She ___ to the office every day.",
        options = listOf("go", "goes", "going", "gone"),
        correctOptionIndex = 1,
    ),
    PlacementQuestion(
        prompt = "I ___ my keys — have you seen them?",
        options = listOf("lost", "am losing", "have lost", "was lost"),
        correctOptionIndex = 2,
    ),
    PlacementQuestion(
        prompt = "By the time we arrived, the meeting ___ already ___.",
        options = listOf("has / started", "had / started", "was / starting", "did / start"),
        correctOptionIndex = 1,
    ),
    PlacementQuestion(
        prompt = "Choose the word closest in meaning to \"reluctant\".",
        options = listOf("eager", "unwilling", "confident", "curious"),
        correctOptionIndex = 1,
    ),
    PlacementQuestion(
        prompt = "If I ___ known about the traffic, I would have left earlier.",
        options = listOf("have", "had", "would have", "was"),
        correctOptionIndex = 1,
    ),
    PlacementQuestion(
        prompt = "The report's conclusions were, ___ the delays, broadly positive.",
        options = listOf("despite", "although", "because of", "unless"),
        correctOptionIndex = 0,
    ),
)

/** Maps a raw score out of [PLACEMENT_QUESTIONS] to a level bucket. */
fun englishLevelForScore(correctCount: Int): EnglishLevel = when (correctCount) {
    0, 1 -> EnglishLevel.A2
    2, 3 -> EnglishLevel.B1
    4, 5 -> EnglishLevel.B2
    else -> EnglishLevel.C1
}
