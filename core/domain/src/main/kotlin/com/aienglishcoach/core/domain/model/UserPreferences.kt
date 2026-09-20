package com.aienglishcoach.core.domain.model

/** User profile and application settings persisted in DataStore. */
data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val englishLevel: EnglishLevel = EnglishLevel.B1,
    val learningGoals: Set<LearningGoal> = emptySet(),
    val dailyGoalMinutes: Int = 10,
    val ttsSpeechRate: Float = 1.0f,
    val ttsVoice: TtsVoice = TtsVoice.US,
    /** OpenAI-compatible chat model id. */
    val aiModel: String = DEFAULT_AI_MODEL,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    /** Reminder notifications for due revisions. */
    val revisionRemindersEnabled: Boolean = true,
) {
    companion object {
        const val DEFAULT_AI_MODEL = "gpt-4o-mini"
    }
}

enum class EnglishLevel { A2, B1, B2, C1 }

enum class LearningGoal {
    WORK,
    TRAVEL,
    EMIGRATION,
    EXAMS,
    FLUENCY,
    SOCIAL,
}

enum class TtsVoice { US, UK }

enum class ThemeMode { SYSTEM, LIGHT, DARK }
